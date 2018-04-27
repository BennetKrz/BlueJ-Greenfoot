/*
 This file is part of the Greenfoot program. 
 Copyright (C) 2005-2009,2010,2011,2012,2013,2014,2015,2016,2018  Poul Henriksen and Michael Kolling 
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package greenfoot.vmcomm;

import bluej.utility.Debug;
import bluej.utility.javafx.JavaFXUtil;
import greenfoot.World;
import greenfoot.WorldVisitor;
import greenfoot.core.ShadowProjectProperties;
import greenfoot.core.Simulation;
import greenfoot.core.WorldHandler;
import greenfoot.gui.DropTarget;
import greenfoot.gui.WorldRenderer;
import greenfoot.gui.input.KeyboardManager;
import greenfoot.gui.input.mouse.MousePollingManager;
import greenfoot.vmcomm.Command;
import greenfoot.vmcomm.VMCommsMain;
import javafx.scene.input.KeyCode;
import threadchecker.OnThread;
import threadchecker.Tag;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.swing.JPanel;

/**
 * Lives on the Simulation VM (aka debug VM), and handles communications with the server
 * (see {@link VMCommsMain})
 */
@OnThread(Tag.Simulation)
public class VMCommsSimulation
{
    private final WorldRenderer worldRenderer;    
        
    // These variables are shared with the remote communications thread and need synchronised access:
    /** Whether the image has been updated */
    private boolean updateImage;
    /** The world image (as most recently painted; double-buffered) */
    private BufferedImage[] worldImages = new BufferedImage[2];
    /** Index in worldImages of the most recently drawn world */
    private int drawnWorld;
    /** Whether the last drawn image is currently being transferred */
    private boolean transferringImage;
    /** The prompt for Greenfoot.ask() */
    private String pAskPrompt;
    /** The ask request identifier */
    private int pAskId;
    /** The answer received from an ask */
    private String askAnswer;

    private final ShadowProjectProperties projectProperties;
    
    /**
     * Shared memory documentation (this comment may get moved to somewhere more appropriate later).
     *
     * The shared memory consists of two successive lumps of memory. One is used by the server VM to
     * transmit data, and the other is used by the debug VM for the same purpose. File locks protect
     * both regions to prevent (in cases where it matters) either side from reading a potentially
     * incomplete data frame while the other side is still writing it. The locking protocol is
     * described in VMCommsMain.
     * 
     * Its format is as follows, where each position is an integer position (i.e. bytes times four):
     * 
     * Server area (16kb):
     * Pos 0: Reserved. Currently this region is locked independently; the "real" server area starts
     *        following this position.
     * Pos 1: When the number is negative, it indicates that the server VM has sent back
     *        information to the debug VM to read.  This includes keyboard and mouse events,
     *        as shown below.
     * Pos 2: The last consumed image frame received from the debug VM. Note that the debug VM
     *        should not update the image in the buffer until the current image is consumed
     *        (otherwise there may be paint artifacts such as tearing). 
     * Pos 3: Count of commands (C), can be zero
     * Pos 4 onwards:
     *        Commands.  Each command begins with an integer sequence ID, then has
     *        an integer length (L), followed by L integers (L >= 1).
     *        The first integer of the L integers is always the
     *        command type, and the amount of other integers depend on the command.  For example,
     *        GreenfootStage.COMMAND_RUN just has the command type integer and no more, whereas
     *        mouse events have four integers.
     *
     * Debug VM area (10M - 16kb): [Positions relative to beginning]
     * 
     * Pos 0: Sequence index when the current (included) image was painted (the image is included
     *        unchanged in subsequent frames).
     * Pos 1: Width of world image in pixels (W)
     * Pos 2: Height of world image in pixels (H)
     * Pos 3 incl to 3+(W*H) excl, if W and H are both greater than zero:
     *        W * H pixels one row at a time with no gaps, each pixel is one
     *        integer, in BGRA form, i.e. blue is highest 8 bits, alpha is lowest.
     * Pos 3+(W*H): Sequence ID of most recently processed command, or -1 if N/A.
     * Pos 4+(W*H): Stopped-with-error count.  (If this goes up, server VM will bring terminal to front)
     * Pos 5+(W*H) and 6+(W*H): Two ints (highest bits first) with value of System.currentTimeMillis()
     *                          at the point when some execution that may contain user code last started on
     *                          the simulation thread, or 0L if user code is not currently running.
     * Pos 7+(W*H): The current simulation speed (1 to 100)
     * Pos 8+(W*H): 1 if a world is currently installed, or 0 if there is no world.
     * Pos 9+(W*H): -1 if not currently awaiting a Greenfoot.ask() answer.
     *              If awaiting, it is count (P) of following codepoints which make up prompt.
     * Pos 10+(W*H) to 10+(W*H)+P excl: codepoints making up ask prompt.
     *
     */
    private final IntBuffer sharedMemory;
    private int seq = 1;
    private final FileChannel shmFileChannel;
    private FileLock putLock;
    private long lastPaintNanos = System.nanoTime();
    private int lastAckCommand = -1;
    private int lastPaintSeq = -1; // last paint sequence
    private int lastPaintSize; // number of ints last transmitted as image
    private boolean paintScheduled = false; // a paint is scheduled
    
    // How many times have we stopped with an error?  We continuously send the count to the
    // server VM, so that the server VM can observe changes in the count (only ever increases).
    private int stoppedWithErrorCount = 0;
    // When did user code last start?
    private long startOfCurExecution = 0;
    private World world;

    /**
     * Construct a VMCommsSimulation.
     * 
     * @param world The world which we are the canvas for.
     * @param shmFilePath The path to the shared-memory file to be mmap-ed for communication
     */
    @SuppressWarnings("resource")
    public VMCommsSimulation(ShadowProjectProperties projectProperties, String shmFilePath)
    {
        this.projectProperties = projectProperties;
        worldRenderer = new WorldRenderer();
        try
        {
            shmFileChannel = new RandomAccessFile(shmFilePath, "rw").getChannel();
            MappedByteBuffer mbb = shmFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 10_000_000L);
            sharedMemory = mbb.asIntBuffer();
            putLock = shmFileChannel.lock(VMCommsMain.USER_AREA_OFFSET_BYTES,
                    VMCommsMain.USER_AREA_SIZE_BYTES, false);
            
            new Thread(() -> {
                while (true)
                {
                    doInterVMComms();
                }
            }).start();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sets the world that should be visualised by this canvas.
     * Can be called from any thread.
     */
    public void setWorld(World world)
    {
        this.world = world;
        worldRenderer.setWorld(world);
    }

    /**
     * Set the last known world size. Affects the preferred size of the
     * WorldCanvas.
     */
    public void setWorldSize(int xsize, int ysize)
    {
        worldRenderer.setWorldSize(xsize, ysize);
    }

    public void setDropTargetListener(DropTarget worldHandler)
    {
        worldRenderer.setDropTargetListener(worldHandler);
    }

    public static enum PaintWhen { FORCE, IF_DUE, NO_PAINT}

    /**
     * Paints the current world into the shared memory buffer so that the server VM can
     * display it in the window there.
     *
     * @param paintWhen  If IF_DUE, painting may be skipped if it's close to a recent paint.
     *                   FORCE always paints, NO_PAINT indicates that an actual image update
     *                   is not required but other information in the frame should be sent. 
     * @return Answer from Greenfoot.ask() if available, null otherwise
     */
    @OnThread(Tag.Simulation)
    public String paintRemote(PaintWhen paintWhen)
    {
        long now = System.nanoTime();
        if (paintWhen == PaintWhen.IF_DUE && now - lastPaintNanos <= 8_333_333L)
        {
            paintScheduled = (world != null);
            return null; // No need to draw frame if less than 1/120th of sec between them,
                         // but we must schedule a paint for the next sequence we send.
        }

        // One element array to allow a reference to be set by readCommands:
        String[] answer = new String[] {null};
        
        boolean sendImage = world != null && (paintWhen != PaintWhen.NO_PAINT || paintScheduled);
        if (sendImage)
        {
            lastPaintNanos = now;
            int imageWidth = WorldVisitor.getWidthInPixels(world);
            int imageHeight = WorldVisitor.getHeightInPixels(world);
            BufferedImage worldImage;
            
            synchronized (this)
            {
                int toDrawWorld = 1 - drawnWorld; // invert 0/1
                worldImage = worldImages[toDrawWorld];
                if (worldImage == null || worldImage.getHeight() != imageHeight
                        || worldImage.getWidth() != imageWidth)
                {
                    worldImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                    worldImages[toDrawWorld] = worldImage;
                }
            }
            
            worldRenderer.renderWorld(worldImage);
            
            synchronized (this)
            {
                // If a world image is currently being transferred, we mustn't overwrite it.
                // Therefore, alter drawnWorld only if that's not the case:
                if (! transferringImage)
                {
                    drawnWorld = 1 - drawnWorld;
                }
                updateImage = true;
            }
        }
        
        return answer[0];
    }

    @OnThread(Tag.Simulation)
    public synchronized String doAsk(int askId, String askPrompt)
    {
        pAskPrompt = askPrompt;
        pAskId = askId;
        askAnswer = null;
        
        try
        {
            do
            {
                wait();
            }
            while (askAnswer == null);
        }
        catch (InterruptedException ie)
        {
            return "";
        }
        
        return askAnswer;
    }
    
    /**
     * Perform communications exchange with the other VM.
     */
    private void doInterVMComms()
    {
        // One element array to allow a reference to be set by readCommands:
        String[] answer = new String[] {null};
        
        FileLock fileLock = null;
        
        try
        {
            // Get lock for our read area:
            fileLock = shmFileChannel.lock(VMCommsMain.SERVER_AREA_OFFSET_BYTES,
                    VMCommsMain.SERVER_AREA_SIZE_BYTES, false);

            boolean doUpdateImage = updateImage;
            
            sharedMemory.position(1);
            int recvSeq = sharedMemory.get();
            if (recvSeq < 0 && Simulation.getInstance() != null)
            {
                int lastConsumedImg = sharedMemory.get();
                // Only update the image if the previous one was consumed:
                doUpdateImage &= (lastConsumedImg >= lastPaintSeq);
                int latest = readCommands(answer);
                if (latest != -1)
                {
                    lastAckCommand = latest;
                }
            }
            
            BufferedImage img;
            synchronized (this)
            {
                img = doUpdateImage ? worldImages[drawnWorld] : null;
                transferringImage = (img != null);
                if (img != null)
                {
                    // We want to clear the updateImage flag nice and early, so that any new image
                    // generated in the meantime can correctly set it back to true:
                    updateImage = false;
                }
            }
            
            int [] raw = (img == null) ? null : ((DataBufferInt) img.getData().getDataBuffer()).getData();

            int imageWidth = 0;
            int imageHeight = 0;
            if (img != null)
            {
                imageWidth = img.getWidth();
                imageHeight = img.getHeight();
            }
            
            sharedMemory.position(VMCommsMain.USER_AREA_OFFSET);
            sharedMemory.put(this.seq++);
            if (img == null)
            {
                sharedMemory.put(lastPaintSeq);
                sharedMemory.get(); // skip width
                sharedMemory.get(); // skip height
                sharedMemory.position(sharedMemory.position() + lastPaintSize);
            }
            else
            {
                lastPaintSeq = (seq - 1);
                sharedMemory.put(lastPaintSeq);
                sharedMemory.put(imageWidth);
                sharedMemory.put(imageHeight);
                for (int i = 0; i < raw.length; i++)
                {
                    sharedMemory.put(raw[i]);
                }
                lastPaintSize = raw.length;
                paintScheduled = false;
                synchronized (this)
                {
                    transferringImage = false;
                    // If another world image has been painted in the meantime, make sure that
                    // drawnWorld indexes the correct image in the array (updateImage will have
                    // been set true in paintRemote()):
                    if (updateImage)
                    {
                        drawnWorld = 1 - drawnWorld;
                    }
                }
            }
            sharedMemory.put(lastAckCommand);
            sharedMemory.put(stoppedWithErrorCount);
            sharedMemory.put((int)(startOfCurExecution >> 32));
            sharedMemory.put((int)(startOfCurExecution & 0xFFFFFFFFL));
            if (Simulation.getInstance() != null)
            {
                sharedMemory.put(Simulation.getInstance().getSpeed());
            }
            else
            {
                sharedMemory.put(0);
            }
            sharedMemory.put(world == null ? 0 : 1);
            
            // If not asking, put -1
            synchronized (this)
            {
                if (pAskPrompt == null || answer[0] != null)
                {
                    sharedMemory.put(-1);
                }
                else
                {
                    // Asking, so put the ask ID, and the prompt string:
                    int[] codepoints = pAskPrompt.codePoints().toArray();
                    sharedMemory.put(pAskId);
                    sharedMemory.put(codepoints.length);
                    sharedMemory.put(codepoints);
                }
            }
            
            putLock.release();

            // Lock the synchronisation area (C) to make sure that the server has acquired our put area:
            FileLock syncLock = shmFileChannel.lock(VMCommsMain.SYNC_AREA_OFFSET_BYTES,
                    VMCommsMain.SYNC_AREA_SIZE_BYTES, false);
            
            fileLock.release();
            putLock = shmFileChannel.lock(VMCommsMain.USER_AREA_OFFSET_BYTES,
                    VMCommsMain.USER_AREA_SIZE_BYTES, false);
            syncLock.release();
        }
        catch (IOException ex)
        {
            try
            {
                putLock.release();
            }
            catch (Exception e) {}
            Debug.reportError(ex);
        }
            
        if (answer[0] != null)
        {
            gotAskAnswer(answer[0]);
        }
    }
    
    /**
     * An "ask" answer has been received from the other VM; record it and signal the simulation
     * thread.
     */
    private synchronized void gotAskAnswer(String answer)
    {
        askAnswer = answer;
        notifyAll();
    }

    /**
     * Read commands from the server VM.  Eventually, at the end of the Greenfoot
     * rewrite, this should live elsewhere (probably in WorldHandler or similar).
     *
     * @param answer A one-element array in which to store an ask-answer, if received
     * @return The command acknowledge to write back to the buffer
     */
    private int readCommands(String[] answer)
    {
        int lastSeqID = -1;
        int commandCount = sharedMemory.get();
        for (int i = 0; i < commandCount; i++)
        {
            lastSeqID = sharedMemory.get();
            int commandLength = sharedMemory.get();
            int data[] = new int[commandLength];
            sharedMemory.get(data);
            if (Command.isKeyEvent(data[0]))
            {
                int awtCode = JavaFXUtil.fxKeyCodeToAWT(KeyCode.values()[data[1]]);
                if (awtCode != -1)
                {
                    KeyboardManager keyboardManager = WorldHandler.getInstance().getKeyboardManager();
                    switch(data[0])
                    {
                        case Command.KEY_DOWN:
                            keyboardManager.pressKey(awtCode);
                            break;
                        case Command.KEY_UP:
                            keyboardManager.releaseKey(awtCode);
                            break;
                        // KEY_TYPED is not processed
                    }
                }
            }
            else if (Command.isMouseEvent(data[0]))
            {
                MouseEvent fakeEvent = new MouseEvent(new JPanel(), 1, 0, 0, data[1], data[2], data[3], false, data[4]);
                MousePollingManager mouseManager = WorldHandler.getInstance().getMouseManager();
                switch (data[0])
                {
                    case Command.MOUSE_CLICKED:
                        mouseManager.mouseClicked(fakeEvent);
                        break;
                    case Command.MOUSE_PRESSED:
                        mouseManager.mousePressed(fakeEvent);
                        break;
                    case Command.MOUSE_RELEASED:
                        mouseManager.mouseReleased(fakeEvent);
                        break;
                    case Command.MOUSE_DRAGGED:
                        mouseManager.mouseDragged(fakeEvent);
                        break;
                    case Command.MOUSE_MOVED:
                        mouseManager.mouseMoved(fakeEvent);
                        break;
                }
            }
            else
            {
                // Commands which are not keyboard or mouse events:
                switch (data[0])
                {
                    case Command.COMMAND_RUN:
                        Simulation.getInstance().setPaused(false);
                        break;
                    case Command.COMMAND_PAUSE:
                        Simulation.getInstance().setPaused(true);
                        break;
                    case Command.COMMAND_ACT:
                        Simulation.getInstance().runOnce();
                        break;
                    case Command.COMMAND_INSTANTIATE_WORLD:
                        String className = new String(data, 1, data.length - 1);
                        WorldHandler.getInstance().instantiateNewWorld(className);
                        break;
                    case Command.COMMAND_DISCARD_WORLD:
                        WorldHandler.getInstance().discardWorld();
                        break;
                    case Command.COMMAND_CONTINUE_DRAG:
                        // Will be drag-ID, X, Y:
                        WorldHandler.getInstance().continueDragging(data[1], data[2], data[3]);
                        break;
                    case Command.COMMAND_END_DRAG:
                        // Will be drag-ID:
                        WorldHandler.getInstance().finishDrag(data[1]);
                        break;
                    case Command.COMMAND_ANSWERED:
                        // Store the codepoints we received:
                        answer[0] = new String(data, 1, data.length - 1);
                        break;
                    case Command.COMMAND_PROPERTY_CHANGED:
                        int keyLength = data[1];
                        String key = new String(data, 2, keyLength);
                        int valueLength = data[2+keyLength];
                        String value = valueLength < 0 ? null : new String(data, 3 + keyLength, valueLength);
                        projectProperties.propertyChangedOnServerVM(key, value);
                        break;
                    case Command.COMMAND_SET_SPEED:
                        Simulation.getInstance().setSpeed(data[1]);
                        break;
                }
            }
        }
        return lastSeqID;
    }

    /**
     * Gets a suitable ask ID.  This needs to be a sequence number which will be
     * newer than the last answer, so we just use the last command we received.
     */
    public int getAskId()
    {
        return lastAckCommand;
    }

    /**
     * The simulation thread has stopped with an error; need to let the server VM know. 
     */
    @OnThread(Tag.Simulation)
    public void notifyStoppedWithError()
    {
        stoppedWithErrorCount += 1;
        paintRemote(PaintWhen.NO_PAINT);
    }

    /**
     * User code has begun executing.  Note this in the shared memory area so that the server VM can know.
     */
    @OnThread(Tag.Simulation)
    public void userCodeStarting()
    {
        // If the other side already think we're running, not much cause to update them, so
        // only bother if we've already been going for over a second:
        long now = System.currentTimeMillis();
        boolean recentlyRunning = now - startOfCurExecution < 1000L;
        startOfCurExecution = now;
        if (!recentlyRunning)
        {
            paintRemote(PaintWhen.NO_PAINT);
        }
    }

    /**
     * User code has finished executing.  Note this in the shared memory area so that the server VM can know.
     * Each userCodeStopped() event should follow one call to userCodeStarting().
     */
    @OnThread(Tag.Simulation)
    public void userCodeStopped()
    {
        startOfCurExecution = 0L;
        paintRemote(PaintWhen.NO_PAINT);
    }
}
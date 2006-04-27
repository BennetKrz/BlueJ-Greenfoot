package greenfoot.gui;

import greenfoot.World;
import greenfoot.actions.AboutGreenfootAction;
import greenfoot.actions.CloseProjectAction;
import greenfoot.actions.CompileAllAction;
import greenfoot.actions.CompileClassAction;
import greenfoot.actions.EditClassAction;
import greenfoot.actions.NYIAction;
import greenfoot.actions.NewClassAction;
import greenfoot.actions.NewProjectAction;
import greenfoot.actions.OpenProjectAction;
import greenfoot.actions.PauseSimulationAction;
import greenfoot.actions.RemoveSelectedClassAction;
import greenfoot.actions.RunOnceSimulationAction;
import greenfoot.actions.RunSimulationAction;
import greenfoot.actions.SaveProjectAction;
import greenfoot.actions.ShowCopyrightAction;
import greenfoot.actions.ShowWebsiteAction;
import greenfoot.core.GClass;
import greenfoot.core.GPackage;
import greenfoot.core.GProject;
import greenfoot.core.Greenfoot;
import greenfoot.core.LocationTracker;
import greenfoot.core.Simulation;
import greenfoot.core.WorldHandler;
import greenfoot.event.CompileListener;
import greenfoot.gui.classbrowser.ClassBrowser;
import greenfoot.gui.classbrowser.ClassView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import rmiextension.wrappers.RBlueJ;
import rmiextension.wrappers.event.RCompileEvent;
import bluej.Config;
import bluej.extensions.PackageAlreadyExistsException;
import bluej.extensions.PackageNotFoundException;
import bluej.extensions.ProjectNotOpenException;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * The main frame for a Greenfoot project (one per project)
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @author mik
 *
 * @version $Id: GreenfootFrame.java 4034 2006-04-27 11:20:45Z mik $
 */
public class GreenfootFrame extends JFrame
    implements WindowListener, CompileListener
{
    private static final String readMeIconFile = "readme.png";
    private static final String compileIconFile = "compile.png";
    private static final int WORLD_MARGIN = 40;

    private transient final static Logger logger = Logger.getLogger("greenfoot");
    
    private static final int accelModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    private static final int shiftAccelModifier = accelModifier | KeyEvent.SHIFT_MASK;

    private ClassBrowser classBrowser;
    private ControlPanel controlPanel;
    
    /**
     * Creates a new top level frame with all the GUI components
     * and an open project.
     */
    public GreenfootFrame(RBlueJ blueJ, final GProject project)
        throws HeadlessException, ProjectNotOpenException, RemoteException
    {
        super("Greenfoot: " + project.getName());
        try {
            //HACK to avoid error in class diagram (getPreferredSize stuff) on
            // windows, we use cross platform look and feel
            if (Config.isWinOS()) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LocationTracker.instance(); //force initialisation
        
        URL iconFile = this.getClass().getClassLoader().getResource("greenfoot-icon.gif");
        ImageIcon icon = new ImageIcon(iconFile);
        setIconImage(icon.getImage());

        makeFrame(project);
        addWindowListener(this);
        Greenfoot.getInstance().addCompileListener(this);
        
        prepareMacOSApp();
        
//        projectOpenThread = new Thread() {        // to be removed?
//            public void run() {
//                createFrameComponents(project);
//            }
//        };
//        projectOpenThread.start();
    }
    
//    public void waitForProjectOpen() {
//        try {
//            projectOpenThread.join();
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Prepare MacOS specific behaviour (About menu, Preferences menu, Quit
     * menu)
     */
    private Application prepareMacOSApp()
    {
        Application macApp = new Application();
        macApp.setEnabledPreferencesMenu(true);
        macApp.addApplicationListener(new ApplicationAdapter() {
            public void handleAbout(ApplicationEvent e)
            {
                AboutGreenfootAction.getInstance(GreenfootFrame.this).actionPerformed(null);
                e.setHandled(true);
            }

            public void handlePreferences(ApplicationEvent e)
            {
               // PreferencesAction.getInstance().actionPerformed(getMostRecent());
               // e.setHandled(true);
            }

            public void handleQuit(ApplicationEvent e)
            {
                exit();
                e.setHandled(true);
            }
        });

        return macApp;
    }
    
    /**
     * Create the GUI components for this project in the top level frame.
     * This includes opening the project and displaying the project classes.
     */
    private void makeFrame(GProject project)
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Build the class browser before building the menu, because
        // some menu actions work on the class browser.
        classBrowser = buildClassBrowser();
        setJMenuBar(buildMenu());
        setGlassPane(DragGlassPane.getInstance());

        // build the centre panel. this includes the world and the controls
        
        JPanel centrePanel = new JPanel(new BorderLayout(4, 4));

        // the world panel. this includes the world title and world
        
        JPanel worldPanel = new JPanel(new BorderLayout(12, 12));
        worldPanel.setBorder(BorderFactory.createEtchedBorder());        

        WorldCanvas worldCanvas = new WorldCanvas(null);
        worldCanvas.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        WorldHandler.initialise(project, worldCanvas, null);
        WorldHandler worldHandler = WorldHandler.instance();
        Simulation.initialize(worldHandler);
        Simulation sim = Simulation.getInstance();
        
        JPanel canvasPanel = new JPanel(new CenterLayout()); // this panel is needed for resize behaviour
        canvasPanel.add(worldCanvas, BorderLayout.CENTER);
        JScrollPane worldScrollPane = new JScrollPane(canvasPanel);
        worldScrollPane.setOpaque(false);
        worldScrollPane.getViewport().setOpaque(false);
        worldScrollPane.setBorder(null);
        
        worldPanel.add(worldHandler.getWorldTitle(), BorderLayout.NORTH);
        worldPanel.add(worldScrollPane, BorderLayout.CENTER);

        centrePanel.add(worldPanel, BorderLayout.CENTER);
        
        // the control panel
        
        controlPanel = new ControlPanel(sim);
        controlPanel.setBorder(BorderFactory.createEtchedBorder());        
        worldHandler.addWorldListener(controlPanel);

        centrePanel.add(controlPanel, BorderLayout.SOUTH);

        
        // EAST side: project info button and class browser
        
        JPanel eastPanel = new JPanel(new BorderLayout(12, 12));

        JButton readMeButton = new JButton("Project Information", 
                                   new ImageIcon(getClass().getClassLoader().getResource(readMeIconFile)));
        eastPanel.add(readMeButton, BorderLayout.NORTH);
        
        // the class browser 
        
        JScrollPane classScrollPane = new JScrollPane(classBrowser);
        classScrollPane.setOpaque(false);
        classScrollPane.getViewport().setOpaque(false);
        classScrollPane.setBorder(BorderFactory.createEtchedBorder());
        eastPanel.add(classScrollPane, BorderLayout.CENTER);

        // the compile button at the bottom
        
        JButton button = new JButton(CompileAllAction.getInstance());
        // set the icon image: currently empty, but used to force same button look as readme button
        button.setIcon(new ImageIcon(getClass().getClassLoader().getResource(compileIconFile)));
        eastPanel.add(button, BorderLayout.SOUTH);

        
        // arrange the major components in the content pane
        
        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setLayout(new BorderLayout(12, 12));
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        contentPane.add(centrePanel, BorderLayout.CENTER);
        contentPane.add(eastPanel, BorderLayout.EAST);

        instantiateNewWorld(classBrowser);
        pack();

        worldHandler.setSelectionManager(classBrowser.getSelectionManager());
    }

    /**
     * Show a "Not Yet Implemented" message.
     */
    public void showNYIMessage()
    {
        JOptionPane.showMessageDialog(this, "Not Yet Implemented - sorry.");
    } 

    /**
     * Pack the components in this frame.
     * As part of this, try to make sure that the frame does not get too big.
     * If necessary, make it smaller to fit on screen.
     */
    public void pack()
    {
        super.pack();
        super.pack();   // this seems a bug: if not called twice, it gets the size wrong...
        
        int width = getSize().width + WORLD_MARGIN;
        int height = getSize().height + WORLD_MARGIN;
        
        if (width > getMaximumSize().width) {
            width = getMaximumSize().width;
        }
        if (height > getMaximumSize().height) {
            height = getMaximumSize().height;
        }
        setSize(width, height);
    }

    /**
     * Tries to instantiate a new world. This may fail (if, for example, the
     * world is not compiled. If multiple world classes exist, a random one
     * will be instantiated.
     */
    private World instantiateNewWorld(ClassBrowser classBrowser)
    {
        //init a random world
        Iterator worldClasses = classBrowser.getWorldClasses();

        while (worldClasses.hasNext()) {
            ClassView classView = (ClassView) worldClasses.next();
            if (!classView.getClassName().equals("World")) {
                classView.reloadClass();
                Object o = classView.createInstance();
                if (o instanceof World) {
                    World world = (World) o;
                    if (world != null) {
                        WorldHandler.instance().setWorld(world);
                    }
                    return world;
                }
            }
        }

        return null;
    }


    /**
     * Build the class browser.
     */
    private ClassBrowser buildClassBrowser()
    {
        ClassBrowser classBrowser = new ClassBrowser();
        classBrowser.addCompileClassAction(CompileClassAction.getInstance());
        classBrowser.addEditClassAction(EditClassAction.getInstance());

        try {
            //pkg = Greenfoot.getInstance().getCurrentPackage();
            GProject project = Greenfoot.getInstance().getProject();
            //	TODO when project is empty (a new project) the systemclasses get
            // loaded twice
            GPackage pkg = project.getDefaultPackage();

            GClass[] classes = pkg.getClasses();
            //add the system classes
            GPackage sysPkg = project.getGreenfootPackage();
            if (sysPkg == null) {
                sysPkg = project.newPackage("greenfoot");
            }

            GClass[] gClasses = sysPkg.getClasses();
            for (int i = 0; i < gClasses.length; i++) {
                GClass gClass = gClasses[i];
                classBrowser.quickAddClass(gClass);
            }

            for (int i = 0; i < classes.length; i++) {
                GClass gClass = classes[i];
                classBrowser.quickAddClass(gClass);
            }
            
            classBrowser.updateLayout();
            
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (ProjectNotOpenException e) {
            e.printStackTrace();
        }
        catch (PackageNotFoundException e) {
            e.printStackTrace();
        }
        catch (PackageAlreadyExistsException e) {
            e.printStackTrace();
        }

        return classBrowser;
    }

    /**
     * Build the menu bar.
     */
    private JMenuBar buildMenu()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu projectMenu = addMenu("Project", menuBar, 'p');
        
        addMenuItem(NewProjectAction.getInstance(), projectMenu, KeyEvent.VK_N, false, KeyEvent.VK_N);
        addMenuItem(OpenProjectAction.getInstance(), projectMenu, KeyEvent.VK_O, false, KeyEvent.VK_O);
//        addMenuItem(new NYIAction("Open Recent...", this), projectMenu, -1, false, -1);
        addMenuItem(CloseProjectAction.getInstance(), projectMenu, KeyEvent.VK_W, false, KeyEvent.VK_C);
        addMenuItem(SaveProjectAction.getInstance(), projectMenu, KeyEvent.VK_S, false, KeyEvent.VK_S);
        addMenuItem(new NYIAction("Save As...", this), projectMenu, KeyEvent.VK_S, true, -1);
        projectMenu.addSeparator();
        addMenuItem(new NYIAction("Page Setup...", this), projectMenu, KeyEvent.VK_P, true, -1);
        addMenuItem(new NYIAction("Print...", this), projectMenu, KeyEvent.VK_P, false, KeyEvent.VK_P);
        
        JMenu editMenu = addMenu("Edit", menuBar, 'e');
        
        addMenuItem(new NewClassAction(classBrowser), editMenu, KeyEvent.VK_C, false, KeyEvent.VK_N);
        RemoveSelectedClassAction removeClassAction = new RemoveSelectedClassAction();
        classBrowser.getSelectionManager().addSelectionChangeListener(removeClassAction);
        addMenuItem(removeClassAction, editMenu, KeyEvent.VK_D, false, KeyEvent.VK_R);
        
        JMenu ctrlMenu = addMenu("Controls", menuBar, 'c');
        
        addMenuItem(RunOnceSimulationAction.getInstance(), ctrlMenu, KeyEvent.VK_A, false, KeyEvent.VK_A);
        addMenuItem(RunSimulationAction.getInstance(), ctrlMenu, KeyEvent.VK_R, false, KeyEvent.VK_R);
        addMenuItem(PauseSimulationAction.getInstance(), ctrlMenu, KeyEvent.VK_R, true, KeyEvent.VK_P);
        addMenuItem(new NYIAction("Increase Speed", this), ctrlMenu, KeyEvent.VK_PLUS, false, KeyEvent.VK_PLUS);
        addMenuItem(new NYIAction("Decrease Speed", this), ctrlMenu, KeyEvent.VK_MINUS, false, KeyEvent.VK_MINUS);
        ctrlMenu.addSeparator();
        addMenuItem(CompileAllAction.getInstance(), ctrlMenu, KeyEvent.VK_K, false, -1);
        
        JMenu helpMenu = addMenu("Help", menuBar, 'h');
        
        addMenuItem(AboutGreenfootAction.getInstance(this), helpMenu, -1, false, KeyEvent.VK_A);
        addMenuItem(ShowCopyrightAction.getInstance(this), helpMenu, -1, false, -1);
        helpMenu.addSeparator();
        addMenuItem(new ShowWebsiteAction("Greenfoot Web Site", "http://www.greenfoot.org"), helpMenu, -1, false, -1);
        addMenuItem(new ShowWebsiteAction("Greenfoot Tutorial", "http://www.greenfoot.org/doc/tutorial.html"), helpMenu, -1, false, -1);
        
        return menuBar;
    }

    /** 
     * Add a menu to a menu bar.
     */
    private JMenu addMenu(String name, JMenuBar menubar, char mnemonic)
    {
        JMenu menu = new JMenu(name);
        if(!Config.isMacOS())
            menu.setMnemonic(mnemonic);
        menubar.add(menu);
        return menu;
    }

    /** 
     * Add a menu item to a menu.
     */
    private void addMenuItem(Action action, JMenu menu, int accelKey, boolean shift, int mnemonicKey)
    {
        if(accelKey != -1) {
            if(shift)
                action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelKey, shiftAccelModifier));
            else
                action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelKey, accelModifier));
        }
        if(!Config.isMacOS() && mnemonicKey != -1)
            action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonicKey));
        menu.add(action);
    }


    /**
     * Close this project.
     */
    private void exit()
    {
        super.dispose();
        Greenfoot.getInstance().closeThisInstance();
    }


    /**
     * 
     * This frame should never be disposed (at least not until the program is
     * closed). BlueJ disposes all windows when compiling, so dispose is
     * overridden to avoid it for this frame. Be aware of this when it should
     * really shut down!
     * 
     * @see java.awt.Window#dispose()
     * @see #exit()
     */
    public void dispose()
    {
        // I will not close :-)
    }

    // ----------- WindowListener interface -----------
    
    public void windowOpened(WindowEvent e)
    {}

    public void windowClosing(WindowEvent e)
    {
        logger.info("WindowClosing");
        exit();
    }

    public void windowClosed(WindowEvent e)
    {
        logger.info("WindowClosed");
    }

    public void windowIconified(WindowEvent e)
    {}

    public void windowDeiconified(WindowEvent e)
    {}

    public void windowActivated(WindowEvent e)
    {}

    public void windowDeactivated(WindowEvent e)
    {}

    // ----------- CompileListener interface -----------
    
    public void compileStarted(RCompileEvent event)
    {        
        WorldHandler.instance().reset();
    }

    public void compileError(RCompileEvent event)
    {

    }

    public void compileWarning(RCompileEvent event)
    {

    }

    public void compileSucceeded(RCompileEvent event)
    {
        instantiateNewWorld(classBrowser);
        classBrowser.rebuild();
        pack();
    }

    public void compileFailed(RCompileEvent event)
    {}

    
    // ----------- end of WindowListener interface -----------
    
    /**
     * Returns the maximum size, which is the size of the screen.
     */
    public Dimension getMaximumSize()
    {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}
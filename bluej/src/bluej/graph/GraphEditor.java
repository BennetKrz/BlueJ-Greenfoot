package bluej.graph;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JComponent;

import bluej.Config;
import bluej.pkgmgr.graphPainter.GraphPainterStdImpl;

/**
 * Component to allow editing of general graphs.
 * 
 * @author Michael Cahill
 * @author Michael Kolling
 * @version $Id: GraphEditor.java 2793 2004-07-13 16:59:39Z mik $
 */
public class GraphEditor extends JComponent
    implements MouseMotionListener
{
    protected static final Color background = Config.getItemColour("colour.graph.background");

    private final static Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    private final static Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private final static Cursor resizeCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);

    /**  The grid resolution for graph layout. */
    public static final int GRID_SIZE = 10;

    private Graph graph;
    private GraphPainter graphPainter;
    private MarqueePainter marqueePainter;

    private SelectionController selectionController;

    private Cursor currentCursor = defaultCursor;  // currently shown cursor
    
    /**
     * Create a graph editor.
     * @param graph The graph being edited by this editor.
     */
    public GraphEditor(Graph graph)
    {
        this.graph = graph;
        marqueePainter = new MarqueePainter();
        graphPainter = GraphPainterStdImpl.getInstance();
        selectionController = new SelectionController(this);
    }

    /**
     * Start our mouse listener. This is not done in the constructor, because we want 
     * to give others (the PkgMgrFrame) the chance to listen first.
     */
    public void startMouseListening()
    {
        addMouseMotionListener(this);
        addMouseMotionListener(selectionController);
        addMouseListener(selectionController);
        addKeyListener(selectionController);
    }
    
    
    /**
     * Tell how big we would like to be. The preferred size of the graph editor
     * the the size of the edited graph.
     */
    public Dimension getPreferredSize()
    {
        return graph.getMinimumSize();
    }

    /**
     * Tell how big we would like to be. The minimum size of the graph editor
     * the the size of the edited graph.
     */
    public Dimension getMinimumSize()
    {
        return graph.getMinimumSize();
    }

    /**
     * Paint this graph editor (this may be on screen or on a printer).
     */
    public void paint(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        //draw background
        if (!(g2D instanceof PrintGraphics)) {
            Dimension d = getSize();
            g2D.setColor(background);
            g2D.fillRect(0, 0, d.width, d.height);
        }

        graphPainter.paint(g2D, this);
        marqueePainter.paint(g2D, selectionController.getMarquee());

        super.paint(g); // for border
    }

    // ---- MouseMotionListener interface: ----

    /**
     * The mouse was dragged.
     */
    public void mouseDragged(MouseEvent evt)
    {
    }

    /**
     * The mouse was moved - check whether we should adjust the cursor.
     */
    public void mouseMoved(MouseEvent evt)
    {
        int x = evt.getX();
        int y = evt.getY();
        SelectableGraphElement element = graph.findGraphElement(x, y);
        Cursor newCursor = defaultCursor;
        if (element != null) {
            if (element.isResizable() && element.isHandle(x, y)) {
                newCursor = resizeCursor;
            }
            else {
                newCursor = handCursor;                
            }
        }
        if(currentCursor != newCursor) {
            setCursor(newCursor);
            currentCursor = newCursor;
        }
    }

    // ---- end of MouseMotionListener interface ----

    /**
     * Process mouse events. This is a bug work-around: we prefer to handle the 
     * mouse events in the mouse listener methods in the selection controller, 
     * but on Windows the isPopupTrigger flag is not correctly set in the 
     * mousePressed event. This method seems to be the only place to reliably get 
     * it. So unfortunately, we need to process the popup trigger here.
     * 
     * This method is called after the corresponding mousePressed method.
     */
    protected void processMouseEvent(MouseEvent evt)
    {
        super.processMouseEvent(evt);
        if (evt.isPopupTrigger())
            selectionController.handlePopupTrigger(evt);
    }


    /**
     * Clear the set of selected classes. (Nothing will be selected after this.)
     */
    public void clearSelection()
    {
        selectionController.clearSelection();
    }

    /**
     * Clear the current selection.
     */
    public void removeFromSelection(SelectableGraphElement element)
    {
        selectionController.removeFromSelection(element);
    }

   
    /**
     * Return the rubber band information.
     */
    public RubberBand getRubberBand()
    {
        return selectionController.getRubberBand();
    }

    /**
     * Return the graph currently being edited.
     */
    public Graph getGraph()
    {
        return graph;
    }
}
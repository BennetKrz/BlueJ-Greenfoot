package bluej.graph;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.JComponent;

import bluej.Config;

/**
 * Canvas to allow editing of general graphs
 *
 * @author  Michael Cahill
 * @version $Id: GraphEditor.java 1954 2003-05-15 06:06:01Z ajp $
 */
public class GraphEditor extends JComponent
    implements MouseListener, MouseMotionListener, KeyListener
{
    protected static final Color background = Config.getItemColour("colour.graph.background");

    private Graph graph;
    private Vertex activeVertex;
    private boolean motionListening;

    private boolean readOnly = false;

    public GraphEditor(Graph graph)
    {
        this.graph = graph;

        activeVertex = null;
        motionListening = false;
    }

    public Dimension getPreferredSize()
    {
        return graph.getMinimumSize();
    }

    public Dimension getMinimumSize()
    {
        return graph.getMinimumSize();
    }

    public void paint(Graphics g)
    {
        if(!(g instanceof PrintGraphics)) {
            Dimension d = getSize();
            g.setColor(background);
            g.fillRect(0, 0, d.width, d.height);
        }

        graph.draw(g);
    }

    public Graphics2D getGraphics2D()
    {
        return (Graphics2D) super.getGraphics();
    }

	public boolean isFocusTraversable()
	{
		return false;
	}
	
	// ---- KeyListener interface ----
	
    public void keyPressed(KeyEvent evt)
    {
/**  kay activation of popup menu -no ready
    	char key = evt.getKeyChar();
    	switch(key) {
    	case 'm': 
            if(activeVertex != null)
                activeVertex.popupMenu(10, 10, this);
        }
 */
    }
    
    public void keyReleased(KeyEvent evt)
    {
    }
    
    public void keyTyped(KeyEvent evt)
    {
    }
    
    // ---- MouseListener interface ----
	
    public void mouseClicked(MouseEvent evt)
    {
        if(activeVertex != null) {
            if(evt.getClickCount() > 1 && ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
                activeVertex.doubleClick(evt, evt.getX(), evt.getY(), this);
            }
            else
                activeVertex.singleClick(evt, evt.getX(), evt.getY(), this);
        }
    }

    public void mouseEntered(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}

    public void mousePressed(MouseEvent evt)
    {
        int x = evt.getX();
        int y = evt.getY();

        activeVertex = null;

        // Try to find a vertex containing the point
        // Rather than breaking when we find the vertex we keep searching
        // which will therefore find the LAST vertex containing the point
        // This turns out to be the vertex which is rendered at the front
        for(Iterator it = graph.getVertices(); it.hasNext(); ) {
            Vertex v = (Vertex)it.next();

            if((v.getX() <= x) && (x < v.getX() + v.getWidth()) && (v.getY() <= y) && (y < v.getY() + v.getHeight())) {
                activeVertex = v;
            }
        }

        graph.setActiveVertex(activeVertex);

        if((activeVertex != null) && !evt.isPopupTrigger() &&
            ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
            activeVertex.mousePressed(evt, x, y, this);
            if (!motionListening) {
                startMotionListening();
            }
        }
    }

    public void mouseReleased(MouseEvent evt)
    {
        if(activeVertex != null && ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)) {
            activeVertex.mouseReleased(evt, evt.getX(), evt.getY(), this);

            if (motionListening) {
                stopMotionListening();
            }
        }
    }

    // ---- end of MouseListener interface ----

    // ---- MouseMotionListener interface: ----
	
    public void mouseDragged(MouseEvent evt)
    {
        if (readOnly)
            return;

        if(activeVertex != null)
            activeVertex.mouseDragged(evt, evt.getX(), evt.getY(), this);
    }

    public void mouseMoved(MouseEvent evt)
    {
        if(activeVertex != null)
            activeVertex.mouseMoved(evt, evt.getX(), evt.getY(), this);
    }

    // ---- end of MouseMotionListener interface ----
	

    public void startMotionListening()
    {
		addMouseMotionListener(this);
		motionListening = true;
    }

    public void stopMotionListening()
    {
        // if we're not choosing anymore, remove listener
        removeMouseMotionListener(this);
        motionListening = false;
    }

    protected void processMouseEvent(MouseEvent evt)
    {
        super.processMouseEvent(evt);

        if(evt.isPopupTrigger())
            if((activeVertex != null)) {
                activeVertex.popupMenu(evt.getX(), evt.getY(), this);
            }
    }

    public void setReadOnly(boolean state)
    {
        readOnly = state;
    }
}

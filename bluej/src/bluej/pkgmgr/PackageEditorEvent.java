package bluej.pkgmgr;

import java.util.EventObject;

import bluej.debugger.DebuggerObject;
import bluej.testmgr.record.InvokerRecord;
import bluej.views.CallableView;

/**
 * The event which occurs while editing a package
 *
 * @author  Andrew Patterson
 * @version $Id: PackageEditorEvent.java 2227 2003-10-28 02:08:15Z ajp $
 */
public class PackageEditorEvent extends EventObject
{
    public final static int TARGET_CALLABLE = 1;
    public final static int TARGET_REMOVE = 2;
    public final static int TARGET_OPEN = 3;
    public final static int TARGET_RUN = 4;
    public final static int TARGET_BENCHTOFIXTURE = 5; // only for unit tests
    public final static int TARGET_FIXTURETOBENCH = 6; // only for unit tests
    public final static int TARGET_MAKETESTCASE = 7;    // only for unit tests

    public final static int OBJECT_PUTONBENCH = 8;

    protected int id;
    protected CallableView cv;
    protected DebuggerObject obj;
	protected InvokerRecord ir;
    protected String name;

    public PackageEditorEvent(Object source, int id)
    {
        super(source);
        this.id = id;
    }

    public PackageEditorEvent(Object source, int id, String packageName)
    {
        super(source);

        this.id = id;
        this.name = packageName;
    }

    public PackageEditorEvent(Object source, int id, CallableView cv)
    {
        super(source);

        if (id != TARGET_CALLABLE)
            throw new IllegalArgumentException();

        this.id = id;
        this.cv = cv;
    }

    public PackageEditorEvent(Object source, int id, DebuggerObject obj, InvokerRecord ir)
    {
        super(source);

        if (id != OBJECT_PUTONBENCH)
            throw new IllegalArgumentException();

        this.id = id;
        this.obj = obj;
		this.ir = ir;
    }

    public int getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public CallableView getCallable()
    {
        return cv;
    }

    public DebuggerObject getDebuggerObject()
    {
        return obj;
    }
    
    public InvokerRecord getInvokerRecord()
    {
    	return ir;	
    }
}

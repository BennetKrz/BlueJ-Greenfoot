package bluej.pkgmgr;

import bluej.Config;
import bluej.utility.Debug;
import bluej.utility.Utility;
import bluej.utility.DialogManager;
import bluej.utility.BlueJFileReader;
import bluej.utility.FileUtility;
import bluej.prefmgr.PrefMgr;
import bluej.debugger.*;
import bluej.editor.*;
import bluej.parser.*;
import bluej.parser.ast.*;
import bluej.parser.symtab.*;
import bluej.testmgr.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import junit.swingui.TestRunner;
import junit.framework.*;

import antlr.collections.*;
import antlr.*;

/**
 * A role object for Junit unit tests.
 *
 * @author  Andrew Patterson based on AppletClassRole
 * @version $Id: UnitTestClassRole.java 1697 2003-03-11 19:58:20Z mik $
 */
public class UnitTestClassRole extends ClassRole
{
    public static final String UNITTEST_ROLE_NAME = "UnitTestTarget";
    
    /**
     * Create the unit test class role.
     */
    public UnitTestClassRole()
    {
    }

    public String getRoleName()
    {
        return UNITTEST_ROLE_NAME;
    }

    public String getStereotypeLabel()
    {
        return "unit test";
    }

    public Color getBackgroundColour()
    {
        return Config.getItemColour("colour.class.bg.unittest");
    }

    /**
     * Generate a popup menu for this TestClassRole.
     * @param cl the class object that is represented by this target
     * @param editorFrame the frame in which this targets package is displayed
     * @return the generated JPopupMenu
     */
    protected boolean createRoleMenu(JPopupMenu menu, ClassTarget ct, int state)
    {
        // add run all tests option
        addMenuItem(menu, new TestAction("Test All", ct.getPackage().getEditor(),ct),(state == Target.S_NORMAL));
        menu.addSeparator();

        return true;
    }

    /**
     * creates a class menu containing any constructors and static methods etc.
     *
     * @param menu the popup menu to add the class menu items to
     * @param cl Class object associated with this class target
     */
    protected boolean createClassConstructorMenu(JPopupMenu menu, ClassTarget ct, Class cl)
    {
        boolean hasEntries = false;

        Method[] allMethods = cl.getMethods();

        for (int i=0; i < allMethods.length; i++) {
            Method m = allMethods[i];

            String name = m.getName();
            // look for reasons to not include this method as a test case
            if (!name.startsWith("test"))
                continue;
            if (!Modifier.isPublic(m.getModifiers()))
                continue;
            if (m.getParameterTypes().length != 0)
                continue;
            if (!m.getReturnType().equals(Void.TYPE))
                continue;

            Action testAction = new TestAction("Test " + name.substring(4), ct.getPackage().getEditor(), ct, name);

            JMenuItem item = new JMenuItem();
            item.setAction(testAction);
            item.setFont(PrefMgr.getPopupMenuFont());
            menu.add(item);
            hasEntries = true;
        }
        return hasEntries;
    }

    /**
     * creates a class menu containing any constructors and static methods etc.
     *
     * @param menu the popup menu to add the class menu items to
     * @param cl Class object associated with this class target
     */
    protected boolean createClassStaticMenu(JPopupMenu menu, ClassTarget ct, Class cl)
    {
        addMenuItem(menu, new MakeTestCaseAction("Create Test Method...",
                                                    ct.getPackage().getEditor(), ct), true);
        addMenuItem(menu, new BenchToFixtureAction("Object Bench to Test Fixture",
                                                    ct.getPackage().getEditor(), ct), true);
        addMenuItem(menu, new FixtureToBenchAction("Test Fixture to Object Bench",
                                                    ct.getPackage().getEditor(), ct), true);

        return true;
    }

    public void run(PkgMgrFrame pmf, ClassTarget ct, String param)
    {
        DebuggerTestResult dtr;

        if (param != null) {
            // Test a single method
            dtr = Debugger.debugger.runTestMethod(pmf.getProject().getRemoteClassLoader().getId(),
                        pmf.getProject().getUniqueId(), ct.getQualifiedName(), param);

            if (dtr.errorCount() == 0 && dtr.failureCount() == 0)
                pmf.setStatus(param + " succeeded");
            else
                TestDisplayFrame.getTestDisplay().setResult(dtr);

        }
        else {
            // Test the whole class
            dtr = Debugger.debugger.runTestClass(pmf.getProject().getRemoteClassLoader().getId(),
                            pmf.getProject().getUniqueId(), ct.getQualifiedName());

            TestDisplayFrame.getTestDisplay().setResult(dtr);

        }            
    }

    public void doMakeTestCase(PkgMgrFrame pmf, ClassTarget ct)
    {
        String newTestName = DialogManager.askString(pmf, "unittest-new-test-method");

        if (newTestName == null)
            return;

        pmf.getProject().removeLocalClassLoader();

        pmf.getPackage().getProject().setTestMode(true);
        
        pmf.endTestButton.setEnabled(true);
        pmf.cancelTestButton.setEnabled(true);
        pmf.testStatusMessage.setEnabled(true);
        pmf.testStatusMessage.setText("constructing " + ct.getBaseName() + ".test" + newTestName + "()");
 
        Editor ed = ct.getEditor();

        Map dobs = Debugger.debugger.runTestSetUp(
                            pmf.getProject().getRemoteClassLoader().getId(),
                            pmf.getProject().getUniqueId(),
                            ct.getQualifiedName());

        Iterator it = dobs.entrySet().iterator();
        
        while(it.hasNext()) {
            Map.Entry mapent = (Map.Entry) it.next();

            pmf.putObjectOnBench((DebuggerObject)mapent.getValue(),(String) mapent.getKey());
        }
        
        pmf.getObjectBench().resetRecordingInteractions();
        
        pmf.setTestInfo(newTestName, ct);
     }

    public void doEndMakeTestCase(PkgMgrFrame pmf, ClassTarget ct, String name)
    {
        Editor ed = ct.getEditor();

        try {
            BaseAST ast = (BaseAST) bluej.parser.ast.JavaParser.parseFile(new java.io.FileReader(ct.getSourceFile()));
            BaseAST firstClass = (BaseAST) ast.getFirstChild();

            LocatableAST methodInsert = null;

            methodInsert = (LocatableAST) firstClass.getFirstChild().getNextSibling();

            if (methodInsert != null) {
                ed.setSelection(methodInsert.getLine(), methodInsert.getColumn(), 1);
                
                ed.insertText("\n\tpublic void test" + name + "()\n\t{\n" + pmf.getObjectBench().getTestMethod() + "\t}\n}\n", false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void doFixtureToBench(PkgMgrFrame pmf, ClassTarget ct)
    {
        Editor ed = ct.getEditor();

        Map dobs = Debugger.debugger.runTestSetUp(
                            pmf.getProject().getRemoteClassLoader().getId(),
                            pmf.getProject().getUniqueId(),
                            ct.getQualifiedName());

        Iterator it = dobs.entrySet().iterator();
        
        while(it.hasNext()) {
            Map.Entry mapent = (Map.Entry) it.next();

            pmf.putObjectOnBench((DebuggerObject)mapent.getValue(),(String) mapent.getKey());
        }
    }   
    
    /**
     * Convert the objects on the object bench into a test fixture.
     */
    public void doBenchToFixture(PkgMgrFrame pmf, ClassTarget ct)
    {
        Editor ed = ct.getEditor();

        try {
            BaseAST ast = (BaseAST) bluej.parser.ast.JavaParser.parseFile(new java.io.FileReader(ct.getSourceFile()));
            BaseAST firstClass = (BaseAST) ast.getFirstChild();

            java.util.List variables = null;
            java.util.List setup = null;
            LocatableAST openingBracket = null;
            LocatableAST methodInsert = null;

            openingBracket = (LocatableAST) firstClass.getFirstChild();
            methodInsert = (LocatableAST) firstClass.getFirstChild().getNextSibling();
            
            BaseAST childAST = (BaseAST) methodInsert.getNextSibling();

            while(childAST != null) {
                if(childAST.getType() == UnitTestParserTokenTypes.OBJBLOCK) {
                    
                    variables = bluej.parser.ast.JavaParser.getVariableSelections(childAST);
                    setup = bluej.parser.ast.JavaParser.getSetupMethodSelections(childAST);
                    break;
                }               
                childAST = (BaseAST) childAST.getNextSibling();            
            }            

            if (variables != null) {
                Iterator it = variables.iterator();
                
                while(it.hasNext()) {
                    LocatableAST firstAST = (LocatableAST) it.next();
                    LocatableAST secondAST = (LocatableAST) it.next();
                    
                    ed.setSelection(firstAST.getLine(), firstAST.getColumn(),
                                        secondAST.getLine(), secondAST.getColumn() + 1);
                    ed.insertText("", false);
                }
            }

            if (setup != null) {
                Iterator it = setup.iterator();
                
                if(it.hasNext()) {
                    LocatableAST firstAST = (LocatableAST) it.next();
                    LocatableAST secondAST = (LocatableAST) it.next();
                    
                    ed.setSelection(firstAST.getLine(), firstAST.getColumn(),
                                        secondAST.getLine(), secondAST.getColumn() + 1);
                    ed.insertText("{\n" + pmf.getObjectBench().getFixtureSetup()
                                     + "\t}", false);
                }
            }

            if (openingBracket != null) {
                ed.setSelection(openingBracket.getLine(), openingBracket.getColumn(), 1);
                
                ed.insertText("{\n" + pmf.getObjectBench().getFixtureDeclaration(), false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A base class for all our actions that run on targets.
     */
    private abstract class TargetAbstractAction extends AbstractAction
    {
        protected Target t;
        protected PackageEditor ped;

        public TargetAbstractAction(String name, PackageEditor ped, Target t)
        {
            super(name);
            this.ped = ped;
            this.t = t;
        }
    }

    /**
     * A TestAction is an action that causes a JUnit test to be run on a class.
     * If testName is not provided, it is set to null which means that the whole
     * test class is run. Else it refers to a test method that should be run
     * individually.
     */
    private class TestAction extends TargetAbstractAction
    {
        private String testName;

        public TestAction(String actionName, PackageEditor ped, Target t)
        {
            super(actionName, ped, t);
            this.testName = null;
        }
                    
        public TestAction(String actionName, PackageEditor ped, Target t, String testName)
        {
            super(actionName, ped, t);
            this.testName = testName;
        }

        public void actionPerformed(ActionEvent e)
        {
            ped.raiseRunTargetEvent(t, testName);
        }
    }

    private class MakeTestCaseAction extends TargetAbstractAction
    {
        public MakeTestCaseAction(String name, PackageEditor ped, Target t)
        {
            super(name, ped, t);
        }

        public void actionPerformed(ActionEvent e)
        {
            ped.raiseMakeTestCaseEvent(t);
        }
    }

    private class BenchToFixtureAction extends TargetAbstractAction
    {
        public BenchToFixtureAction(String name, PackageEditor ped, Target t)
        {
            super(name, ped, t);
        }

        public void actionPerformed(ActionEvent e)
        {
            ped.raiseBenchToFixtureEvent(t);
        }
    }

    private class FixtureToBenchAction extends TargetAbstractAction
    {
        public FixtureToBenchAction(String name, PackageEditor ped, Target t)
        {
            super(name, ped, t);
        }

        public void actionPerformed(ActionEvent e)
        {
            ped.raiseFixtureToBenchEvent(t);
        }
    }

}

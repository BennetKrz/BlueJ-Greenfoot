package bluej.extensions;

import bluej.pkgmgr.Project;
import bluej.pkgmgr.Package;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

/**
 * A wrapper for a BlueJ project.
 *
 * @version $Id: BProject.java 1982 2003-05-23 08:08:34Z damiano $
 */

/*
 * Author Clive Mille, Univeristy of Kent at Canterbury, 2002
 * Author Damiano Bolla, University of Kent at Canterbury, 2003
 */

public class BProject
{
    private Identifier projectId;
  
    /**
     * Constructor for a BProject.
     */
    BProject (Identifier i_projectId)
    {
        projectId = i_projectId;
    }

    /**
     * Returns the name of this project. 
     * This is what is displayed in the title bar of the frame after 'BlueJ'.
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public String getName() throws ProjectNotOpenException
    {
        Project thisProject = projectId.getBluejProject();
        
        return thisProject.getProjectName();
    }
    
    /**
     * Returns the directory in which this project is stored. 
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public File getDir() throws ProjectNotOpenException
    {
        Project thisProject = projectId.getBluejProject();

        return thisProject.getProjectDir();
    }
    
    /**
     * Requests a "save" of all open files in this project. 
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public void save() throws ProjectNotOpenException
    {
        Project thisProject = projectId.getBluejProject();

        thisProject.saveAll();
    }
    
    /**
     * Saves any open files, then closes all frames belonging to this project.
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public void close() throws ProjectNotOpenException
    {
        Project thisProject = projectId.getBluejProject();

        thisProject.saveAll();
        Project.closeProject (thisProject);
    }
    
    
    /**
     * Get a package belonging to this project.
     * 
     * @param the fully-qualified name of the package
     * @return the requested package, or null if it wasn't found
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public BPackage getPackage (String name) throws ProjectNotOpenException
    {
        Project bluejProject = projectId.getBluejProject();

        Package pkg = bluejProject.getPackage (name);
        if ( pkg == null ) return null;

        return new BPackage (new Identifier (bluejProject,pkg));
    }
    
    /**
     * Returns all packages belonging to this project.
     * @return The array of this project packages, if none exist an empty array is returned.
     * @throws ProjectNotOpenException if the project has been closed by the user.
     */
    public BPackage[] getPackages() throws ProjectNotOpenException
    {
        Project thisProject = projectId.getBluejProject();

        List names = thisProject.getPackageNames();
        BPackage[] packages = new BPackage [names.size()];
        for (ListIterator li=names.listIterator(); li.hasNext();) {
            int i=li.nextIndex();
            String name = (String)li.next();
            packages [i] = getPackage (name);
        }
        return packages;
    }

    /**
     * Returns a string representation of the Object
     */
    public String toString ()
      {
      try
        {
        Project thisProject = projectId.getBluejProject();
        return "BProject: "+thisProject.getProjectName();
        }
      catch ( ExtensionException exc )
        {
        return "BProject: INVALID";  
        }
      }
}

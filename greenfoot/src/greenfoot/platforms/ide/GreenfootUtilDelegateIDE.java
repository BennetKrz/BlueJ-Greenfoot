package greenfoot.platforms.ide;

import greenfoot.platforms.GreenfootUtilDelegate;
import greenfoot.util.FileChoosers;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.Hashtable;

import bluej.Config;
import bluej.runtime.ExecServer;
import bluej.utility.BlueJFileReader;
import bluej.utility.FileUtility;

public class GreenfootUtilDelegateIDE implements GreenfootUtilDelegate
{
    /**
     * 
     * Creates the skeleton for a new class
     * 
     */
    public void createSkeleton(String className, String superClassName, File file, String templateFileName) throws IOException   {
        Dictionary<String, String> translations = new Hashtable<String, String>();
        translations.put("CLASSNAME", className);
        if(superClassName != null) {
            translations.put("EXTENDSANDSUPERCLASSNAME", " extends " + superClassName);
        } else {
            translations.put("EXTENDSANDSUPERCLASSNAME", "");
        }
        String baseName = "greenfoot/templates/" +  templateFileName;
        File template = Config.getLanguageFile(baseName);
        
        BlueJFileReader.translateFile(template, file, translations, Charset.forName("UTF-8"));
    }
    
    /**
     * Brings up a file browser that lets the user select an existing Greenfoot scenario.
     * 
     * @return Returns a File pointing to the scenario directory, or null if none selected.
     */
    public File getScenarioFromFileBrowser(Component parent) {
       return FileChoosers.getScenario(parent);
    }    
    
    public String getNewProjectName(Component parent)
    {
        return FileUtility.getFileName(parent, Config.getString("greenfoot.utilDelegate.newScenario"), Config.getString("pkgmgr.newPkg.buttonLabel"), false, null, true);
    }


    public ClassLoader getCurrentClassLoader() 
    {
        return ExecServer.getCurrentClassLoader();
    }
    
    /**
     * Returns the path to a small version of the greenfoot logo.
     */
    public  String getGreenfootLogoPath()
    {        
        File libDir = Config.getGreenfootLibDir();
        return libDir.getAbsolutePath() + "/imagelib/other/greenfoot.png";        
    }


}

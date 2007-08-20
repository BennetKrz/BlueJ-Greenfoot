package greenfoot.platforms;

import greenfoot.GreenfootImage;
import greenfoot.World;

/**
 * Interface to classes that contain specialized behaviour for the Actors
 * depending on where and how the greenfoot project is running.
 * 
 * @author Poul Henriksen
 * 
 */
public interface ActorDelegate
{
    /**
     * Get the default image for objects of this class. May return null.
     */
    public GreenfootImage getImage(String name);

    /**
     * Get the active world. This method should return the instantiated world,
     * even if the object is not yet added to the world.
     */
    public World getWorld();

}

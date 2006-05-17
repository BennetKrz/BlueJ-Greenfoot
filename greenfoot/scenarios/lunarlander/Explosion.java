import greenfoot.World;
import greenfoot.Actor;
import greenfoot.GreenfootImage;
import greenfoot.Greenfoot;

import java.util.*;

/**
 * An explosion. It starts by expanding and then collapsing. 
 * The explosion will explode other obejcts that the explosion intersects.
 * 
 * @author Poul Henriksen
 * @version 1.0
 */
public class Explosion extends Actor
{
    /** How many images should be used in the animation of the explostion */
    private final static int IMAGE_COUNT= 8;
    
    /** 
     * The images in the explosion. This is static so the images are not
     * recreated for every object (improves performance significantly).
     */
    private static GreenfootImage[] images;
    
    /** Current size of the explosion */
    private int size=0;
    
    /** How much do we increment the index in the explosion animation. */
    private int increment=1;    
    
    public Explosion() {
        initialiseImages();
        setImage(images[0]);
    }    
    
    /** 
     * Create the images for explosion.
     */
    public static void initialiseImages() {
        if(images == null) {
            GreenfootImage baseImage = new GreenfootImage("images/explosion.png");
            int maxSize = baseImage.getWidth()/2;
            int delta = maxSize / (IMAGE_COUNT+1);
            int size = 0;
            images = new GreenfootImage[IMAGE_COUNT];
            for(int i=0; i < IMAGE_COUNT; i++) {
                size = size + delta;
                images[i] = new GreenfootImage(baseImage);
                images[i].scale(size, size);
            }
        }
    }
    
    /**
     * EXPLODE!
     */
    public void act()
    { 
        setImage(images[size]);

        size += increment;
        if(size>=IMAGE_COUNT) {
            increment = -increment;
            size += increment;
        }
        
        explodeOthers();
        if(size <= 0) {
            getWorld().removeObject(this);
        }
    }
    
    /**
     * Explodes all intersecting objects.
     */
    private void explodeOthers() 
    {        
        List explodeEm = getIntersectingObjects(null);        
        Iterator i = explodeEm.iterator();
        while(i.hasNext()) {
            Actor a = (Actor) i.next();
            if( ! (a instanceof Explosion)) { //Don't explode other explosions
                int x = a.getX();
                int y = a.getY();
                //Replace other object with an explosion
                getWorld().removeObject(a);
                getWorld().addObject(new Explosion(), x, y);
            }
        }
    }
}
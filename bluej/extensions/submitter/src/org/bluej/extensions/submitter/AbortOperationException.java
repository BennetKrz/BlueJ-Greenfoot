package org.bluej.extensions.submitter;

import java.io.FileNotFoundException;

/**
 * Used to indicate that an operation has been aborted due to some kind of 
 * anticipated incident. The user should be informed that an error has
 * occurred, but in a neat and tidy way.
 *
 * @author Clive Miller
 * @version $Id: AbortOperationException.java 1463 2002-10-23 12:40:32Z jckm $
 */
public class AbortOperationException extends Exception
{
    private String message;
    private Exception exception;
    
    public AbortOperationException (String s)
    {
        message = s;
    }
    
    /**
     * This is another useful constructor, since the user needs to be
     * informed of the message.
     * @param s the message to be passed back to the user.
     */
    public AbortOperationException (Exception ex)
    {
        exception = ex;
        if (ex instanceof FileNotFoundException) {
            message = "File Not Found: "+ex.getMessage();
        } else {
            message = ex.toString();
        }
    }
    
    public String toString()
    {
        return message;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Exception getException()
    {
        return exception;
    }
}
package bluej.testmgr;

import java.awt.Color;

import javax.swing.JProgressBar;

/**
 * A progress bar showing the green/red status.
 *
 * @author  Andrew Patterson (derived from JUnit src)
 * @version $Id: ProgressBar.java 1882 2003-04-24 06:28:11Z ajp $
 */
class ProgressBar extends JProgressBar
{
	boolean fError = false;
	
	public ProgressBar()
	{
		super(); 
		setForeground(getStatusColor());
	}
	
	private Color getStatusColor()
	{
		if (fError)
			return Color.red;
		return Color.green;
	}
		
	public void reset()
	{
		fError= false;
		setForeground(getStatusColor());
		setValue(0);
	}
	
	public void setmaximum(int total)
	{
		setMaximum(total);
	}

	public void step(int value, boolean successful)
	{
		setValue(value);
		if (!fError && !successful) {
			fError= true;
			setForeground(getStatusColor());
		}
	}
}
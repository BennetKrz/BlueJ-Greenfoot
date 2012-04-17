/*
 This file is part of the BlueJ program. 
 Copyright (C) 2011  Michael Kolling and John Rosenberg 
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package bluej.terminal;

import bluej.Config;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

/**
 * A View implementation for the terminal. Styles lines representing recorded method
 * calls (and their results) differently to regular text.
 * 
 * @author Davin McCall
 */
public class TerminalView extends PlainView
{
    public static final String METHOD_RECORD = "method-record";
    public static final String SOURCE_LOCATION = "source-location";
    private static final Color METHOD_RECORD_COLOR = Config.ENV_COLOUR;
    private Color defaultColor;
    
    public TerminalView(Element el, Color defaultColor)
    {
        super(el);
        this.defaultColor = defaultColor;
    }
    
    @Override
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1)
        throws BadLocationException
    {
        try {
        
            Document doc = getDocument();
            int elementIndex = doc.getDefaultRootElement().getElementIndex(p0);
            Element el = doc.getDefaultRootElement().getElement(elementIndex);
            
            AttributeSet attrs = el.getAttributes();
            if (attrs != null && attrs.getAttribute(METHOD_RECORD) != null) {
                g.setColor(METHOD_RECORD_COLOR);
                Segment s = new Segment();
                doc.getText(p0, p1 - p0, s);
                return Utilities.drawTabbedText(s, x, y, g, this, p0);
            } else if (attrs != null && attrs.getAttribute(SOURCE_LOCATION) != null) {
                ExceptionSourceLocation esl = (ExceptionSourceLocation) attrs.getAttribute(SOURCE_LOCATION);
                Segment s = new Segment();
                g.setColor(defaultColor);
                
                // So we have some underline on this line, and we want to draw a portion of the line,
                // which may or may not overlap the underline in any fashion, i.e.
                
                //  at Foo.foo2(Foo.java:26)
                //              ^ esl.getStart()
                //                         ^ esl.getEnd()
                // and p0 and p1 will be anywhere on the line, with p0 < p1.
                //
                // So we will form three groups of text:
                // 1. Pre-underline.  The part from p0, up to the earliest of: the beginning of the underline (esl.getStart()), or p1
                //      (which should be ignored, if p0 >= esl.getStart())
                // 2. Underlined.  The part from the (the latest of: beginning of the underline, or p0) up to (the earliest of: the end of the underline, or p1)
                //      (which should be ignored, if p1 <= esl.getStart() or p0 >= esl.getEnd())
                // 3. Post-underline.  The part from (the latest of: the end of the underline, or p0) up to p1
                //      (which should be ignored, if p1 <= esl.getEnd())
    
                int startUnderline = Math.max(p0, Math.min(esl.getStart(), p1));
                int endUnderline = Math.max(p0, Math.min(esl.getEnd(), p1));
                
                // Group 1, pre-underline:            
                if (p0 < esl.getStart())
                {
                    doc.getText(p0, startUnderline - p0, s);
                    x = Utilities.drawTabbedText(s, x, y, g, this, p0);
                }
                // Group 2, underline:
                if (p0 < esl.getEnd() && p1 > esl.getStart())
                {
                    int startX = x;
                    doc.getText(startUnderline, endUnderline - startUnderline, s);
                    x = Utilities.drawTabbedText(s, x, y, g, this, startUnderline);
                    g.drawLine(startX, y + 1, x, y + 1);
                }
                // Group 3, post-underline:
                if (p1 > esl.getEnd())
                {
                    doc.getText(endUnderline, p1 - endUnderline, s);
                    x = Utilities.drawTabbedText(s, x, y, g, this, endUnderline);
                }
                return x;
            }
            else {
                return super.drawUnselectedText(g, x, y, p0, p1);
            }
        
        }
        catch (BadLocationException e)
        {
            // Print the stack trace, because (as I found out due to bitter experience)
            // the JDK won't print it, it will display a different problem
            // (javax.swing.text.StateInvariantError):
            e.printStackTrace();
            throw e;
        }
    }
    
}

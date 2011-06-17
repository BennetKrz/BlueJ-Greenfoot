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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;

/**
 * Document implementation for the terminal editor pane.
 * 
 * <p>This is mainly necessary to override PlainDocument's slightly brain-damaged
 * implementation of the insertUpdate() method, which can clear line attributes
 * unexpectedly.
 * 
 * @author Davin McCall
 */
public class TerminalDocument extends AbstractDocument
{
    Element root;
    
    public TerminalDocument()
    {
        super(new GapContent());
        root = createDefaultRoot();
    }
    
    /**
     * Mark a line as displaying method output.
     * 
     * @param line  The line number (0..N)
     */
    public void markLineAsMethodOutput(int line)
    {
        writeLock();
        
        Element el = root.getElement(line);
        MutableAttributeSet attr = (MutableAttributeSet) el.getAttributes();
        attr.addAttribute(TerminalView.METHOD_RECORD, Boolean.valueOf(true));
        
        writeUnlock();
    }
    
    @Override
    public Element getDefaultRootElement()
    {
        return root;
    }
    
    @Override
    public Element getParagraphElement(int pos)
    {
        int index = root.getElementIndex(pos);
        return root.getElement(index);
    }
    
    protected AbstractElement createDefaultRoot()
    {
        BranchElement map = (BranchElement) createBranchElement(null, null);
        Element[] lines = new Element[1];
        lines[0] = new LeafElement(map, null, 0, 1);;
        map.replace(0, 0, lines);
        return map;
    }
    
    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr)
    {
        BranchElement lineMap = (BranchElement) getDefaultRootElement();
        int offset = chng.getOffset();
        int length = chng.getLength();
        
        Segment s = new Segment();
        try {
            getText(offset, length, s);
        }
        catch (BadLocationException ble) {
            throw new RuntimeException(ble);
        }
        
        int index = lineMap.getElementIndex(offset);
        LeafElement firstAffected = (LeafElement) lineMap.getElement(index);
        
        int lindex = lineMap.getElementIndex(offset + length);
        LeafElement nextLine = (LeafElement) lineMap.getElement(lindex);
        
        if (offset > 0 && (offset + length) == nextLine.getStartOffset()) {
            // Inserting at a position moves the position, unless the position is 0.
            // So inserting at the beginning of a line moves the line start position,
            // and the previous line end position, which need to be reset:
            firstAffected.setEndOffset(offset);
            nextLine.setStartOffset(offset);
            firstAffected = nextLine;
            nextLine = (LeafElement) lineMap.getElement(lindex + 1);
        }
        
        ArrayList<LeafElement> added = new ArrayList<LeafElement>();
        
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                // line break!
                int origEnd = firstAffected.getEndOffset();
                firstAffected.setEndOffset(i + offset + 1);
                LeafElement newFirst = new LeafElement(root, attr, i + offset + 1, origEnd);
                added.add(newFirst);
                firstAffected = newFirst;
            }
        }
        
        if (! added.isEmpty()) {
            Element [] removed = new Element[0];
            Element [] addedArr = new Element[added.size()];
            added.toArray(addedArr);
            lineMap.replace(lindex + 1, 0, addedArr);
            ElementEdit ee = new ElementEdit(lineMap, lindex + 1, removed, addedArr);
            chng.addEdit(ee);
        }

        super.insertUpdate(chng, attr);
    }

    /**
     * Special purposed leaf element which allows resetting the start and end
     * positions.
     */
    public class LeafElement extends AbstractElement
    {
        Position start;
        Position end;
        
        public LeafElement(Element parent, AttributeSet attrs, int startOffs, int endOffs)
        {
            super(parent, attrs);
            try {
                start = createPosition(startOffs);
                end = createPosition(endOffs);
            }
            catch (BadLocationException ble) {
                throw new RuntimeException(ble);
            }
        }
        
        @Override
        public int getStartOffset()
        {
            return start.getOffset();
        }
        
        @Override
        public int getEndOffset()
        {
            return end.getOffset();
        }
        
        public void setStartOffset(int offset)
        {
            try {
                start = createPosition(offset);
            }
            catch (BadLocationException ble) {
                throw new RuntimeException();
            }
        }

        public void setEndOffset(int offset)
        {
            try {
                end = createPosition(offset);
            }
            catch (BadLocationException ble) {
                throw new RuntimeException();
            }
        }
        
        @Override
        public int getElementCount()
        {
            return 0;
        }
        
        @Override
        public Element getElement(int index)
        {
            return null;
        }
        
        @Override
        public int getElementIndex(int offset)
        {
            return 0;
        }
        
        @Override
        public boolean isLeaf()
        {
            return true;
        }
        
        @Override
        public Enumeration children()
        {
            return new Vector().elements();
        }
        
        @Override
        public boolean getAllowsChildren()
        {
            return false;
        }
    }
}

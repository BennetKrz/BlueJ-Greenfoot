package bluej.classmgr;

import java.io.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;

import bluej.utility.Debug;
import bluej.Config;

/**
 * Given a list of ClassPathEntry returns a table model which allows them to
 * be edited in a JTable.
 *
 * The model implements a form of rollback which allows the table to be
 * edited and then changes can be reverted or committed.
 *
 * @author  Andrew Patterson
 * @cvs     $Id: ClassPathTableModel.java 280 1999-11-18 01:10:21Z ajp $
 */
public class ClassPathTableModel extends AbstractTableModel
{
    static final String locationLabel = Config.getString("classmgr.locationcolumn");
    static final String descriptionLabel = Config.getString("classmgr.descriptioncolumn");

    private ClassPath origcp;
    private ClassPath cp;

    /**
     * Construct a table model of a class path
     *
     * @param origcp    the class path to model
     */
    public ClassPathTableModel(ClassPath origcp)
    {
        this.origcp = origcp;
        this.cp = new ClassPath(origcp);
    }
    
    /**
     * Return the name of a particular column
     *
     * @param col   the column we are naming
     * @return      a string of the columns name
     */
    public String getColumnName(int col)
    {
        if (col == 0)
            return locationLabel;
        else
            return descriptionLabel;
    }

    /**
     * Return the number of rows in the table
     *
     * @return      the number of rows in the table
     */
    public int getRowCount()
    {
        return cp.getEntries().size();
    }
    
    /**
     * Return the number of columns in the table
     *
     * @return      the number of columns in the table
     */
    public int getColumnCount()
    {
        return 2;
    }
    
    /**
     * Find the table entry at a particular row and column
     *
     * @param   row     the table row
     * @param   col     the table column
     * @return          the Object at that location in the table
     */
    public Object getValueAt(int row, int col)
    {
        ClassPathEntry entry = (ClassPathEntry)cp.getEntries().get(row);

        if(col == 0)
            return entry.getCanonicalPathNoException();
        else
            return entry.getDescription(); 
    }

    /**
     * Indicate that only our location column is edititable
     */
    public boolean isCellEditable(int row, int col)
    {
        return (col == 1);
    }

    /**
     * Set the table entry at a particular row and column (only
     * valid for the location column)
     *
     * @param   value   the Object at that location in the table
     * @param   row     the table row
     * @param   col     the table column
     */
    public void setValueAt(Object value, int row, int col)
    {
        if (col == 1) {
            ClassPathEntry entry = (ClassPathEntry)cp.getEntries().get(row);

            entry.setDescription((String)value);

            fireTableCellUpdated(row, col);
        }
    }

    public void addEntry(ClassPathEntry cpe)
    {
        int s = cp.getEntries().size();
        cp.getEntries().add(cpe);
        fireTableRowsInserted(s,s);
    }

    public void deleteEntry(int index)
    {
        if(index < cp.getEntries().size() && index >= 0) {
            cp.getEntries().remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void commitEntries()
    {
        origcp.removeAll();
        origcp.addClassPath(cp);
    }

    public void revertEntries()
    {
        cp = new ClassPath(origcp);

        fireTableDataChanged();
    }
}

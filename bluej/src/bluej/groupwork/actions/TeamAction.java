/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009,2014,2016,2017  Michael Kolling and John Rosenberg 
 
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
package bluej.groupwork.actions;

import bluej.Config;
import bluej.pkgmgr.PkgMgrFrame;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import threadchecker.OnThread;
import threadchecker.Tag;

/**
 * An abstract class for team actions.
 *
 * This is similar to FXAbstractAction but is different in that each
 * button or menu item is not constructed here, but is instead
 * adapted with an explicit reference to the PkgMgrFrame
 * (see useButton, useMenuItem)
 * 
 * @author fisker
 */
@OnThread(Tag.FXPlatform)
public abstract class TeamAction
{
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);
    protected String shortDescription;

    /**
     * Constructor for a team action which shows a dialog. An ellipsis
     * is added to the action text.
     * 
     * @param label   The key for action text
     * @param showsDialog  True if an ellipsis should be appended
     */
    public TeamAction(String label, boolean showsDialog)
    {
        setName(Config.getString(label), showsDialog);
    }

    /**
     * changes the name of the action.
     * @param name 
     */
    public void setName(String name, boolean showsDialog)
    {
    	this.name.set(showsDialog ? (name + "...") : name);
    }

    public void setEnabled(boolean enabled)
    {
        disabled.set(!enabled);
    }

    public boolean isDisabled()
    {
        return disabled.get();
    }

    public void setShortDescription(String shortDescription)
    {
        this.shortDescription = shortDescription;
    }

    /**
     * Sets the given button to activate this action on click,
     * and use this action's title and disabled state
     */
    public void useButton(PkgMgrFrame pmf, ButtonBase button)
    {
        button.textProperty().unbind();
        button.textProperty().bind(name);
        button.disableProperty().unbind();
        button.disableProperty().bind(disabled);
        button.setOnAction(e -> actionPerformed(pmf));
        if (shortDescription != null)
        {
            Tooltip.install(button, new Tooltip(shortDescription));
        }
    }

    /**
     * Sets the given menu item to activate this action,
     * and use this action's title and disabled state
     */
    public void useMenuItem(PkgMgrFrame pmf, MenuItem menuItem)
    {
        menuItem.textProperty().unbind();
        menuItem.textProperty().bind(name);
        menuItem.disableProperty().unbind();
        menuItem.disableProperty().bind(disabled);
        menuItem.setOnAction(e -> actionPerformed(pmf));
    }

    protected abstract void actionPerformed(PkgMgrFrame pmf);
}

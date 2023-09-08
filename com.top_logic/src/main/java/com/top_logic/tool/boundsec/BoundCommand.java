/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.Serializable;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Representation of a command that is bound to certain security restrictions.
 * A command usually belongs to a command group, it should only belong to one
 * command group.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface BoundCommand extends Serializable {

	/** Convenient constant to use with {@link #needsConfirm()}. */
    public static final boolean NEEDS_CONFIRM = true;

	/** Convenient constant to use with {@link #needsConfirm()}. */
	public static final boolean NEEDS_NO_CONFIRM = false;

    /**
     * Return the command name of this handler.
     * 
     * @return    The name of the command processed by this handler, must 
     *            not be <code>null</code>.
     */
    public String getID();
    
    /**
     * Get the command group responsible for this command. A
     * 
     * @return the command group, may be null if command
     *         does not belong to any group
     */
    public BoundCommandGroup getCommandGroup();
    
    /**
     * Indicates to some gui that the User must confirm the command.
     * <br />
     * "Do you really want to shut down this application?"<br />
     * "Do you really want to destroy the world?"<br />
     * "Exporting may take several hours, is your coffe-machine ready?"<br />
     * 
     * @return true when this is some "dangerous" or "important" command
     */
    public boolean needsConfirm();

    /**
	 * The resource key used for translation of GUI elements for this command (when available).
	 * 
	 * @param component
	 *        The context component displaying this command, <code>null</code> if the context
	 *        component is not known.
	 */
	public ResKey getResourceKey(LayoutComponent component);
    
	/**
	 * The icon for this command, if displayed as image button.
	 * 
	 * @param component
	 *        The context component displaying this command, <code>null</code> if the context
	 *        component is not known.
	 * 
	 * @see CommandHandler.Config#getImage()
	 * @see CommandModel#getImage()
	 * @see #getNotExecutableImage(LayoutComponent)
	 */
	public ThemeImage getImage(LayoutComponent component);

	/**
	 * The disabled icon for this command, if displayed as image button.
	 * 
	 * @param component
	 *        The context component displaying this command, <code>null</code> if the context
	 *        component is not known.
	 * 
	 * @see CommandHandler.Config#getDisabledImage()
	 * @see CommandModel#getNotExecutableImage()
	 * @see #getImage(LayoutComponent)
	 */
	public ThemeImage getNotExecutableImage(LayoutComponent component);

	/**
	 * The CSS classes for this command.
	 * 
	 * @param component
	 *        The context component displaying this command, <code>null</code> if the context
	 *        component is not known.
	 * 
	 * @see CommandModel#setCssClasses(String)
	 */
	public default String getCssClasses(LayoutComponent component) {
		return null;
	}

    /**
     * True if the command is a concurrent command.
     */
    public boolean isConcurrent();
    
    /**
	 * Return the command script writer that renders the JavaScriptFunction for this command
	 * 
	 * @param component
	 *            the component at which this {@link BoundCommand} is registered.
	 * @return a {@link CommandScriptWriter}
	 */
    public CommandScriptWriter getCommandScriptWriter(LayoutComponent component);
}

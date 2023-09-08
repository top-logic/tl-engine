/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu;

import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * Application-level provider of a context menu.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContextMenuProvider {

	/**
	 * Quickly decides whether a context menu should be provided for the given object.
	 *
	 * @param obj
	 *        The object for which a context menu is offered.
	 * @return Whether the given object has some context menu commands.
	 */
	boolean hasContextMenu(Object obj);

	/**
	 * Called when a context menu for the given object is opened.
	 * 
	 * <p>
	 * A call to {@link #hasContextMenu(Object)} must have returned <code>true</code> before.
	 * </p>
	 *
	 * @param obj
	 *        The object for which the context menu is opened.
	 * @return The {@link Menu} to display.
	 */
	Menu getContextMenu(Object obj);

}

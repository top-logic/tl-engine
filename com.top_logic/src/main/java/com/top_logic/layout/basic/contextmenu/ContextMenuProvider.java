/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu;

import java.util.Collection;

import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.tree.model.TLTreeNode;

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
	 * @param directTarget
	 *        The raw object, on which the context menu was opened. In case of a tree, this might be
	 *        the {@link TLTreeNode}.
	 * @param model
	 *        The model object for which the context menu is opened. In case of a tree, this might
	 *        be the {@link TLTreeNode#getBusinessObject() business object} of the tree.
	 *
	 * @return The {@link Menu} to display.
	 */
	Menu getContextMenu(Object directTarget, Object model);

	/**
	 * Whether the context menu should be opened for the entire selection or just the single object
	 * on which the user right-clicked.
	 */
	static Object getContextMenuTarget(Object directTarget, Collection<?> selection) {
		if (selection == null) {
			return directTarget;
		}
		if (selection.contains(directTarget)) {
			// The user opened the context menu on the selection.
			if (selection.size() == 1) {
				/* Use the object itself. No need of having it wrapped into a set. */
				return directTarget;
			}
			return selection;
		} else {
			// The user opened the context menu on an unselected object.
			return directTarget;
		}
	}

}

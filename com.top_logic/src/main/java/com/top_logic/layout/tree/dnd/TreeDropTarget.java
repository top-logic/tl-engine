/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.layout.tree.TreeData;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Behavior that implements reactions on a drop operation over a tree.
 * 
 * @see TreeData#getDropTargets()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeDropTarget {

	/**
	 * Kind of drop operation possible in a {@link TreeDropTarget}.
	 * 
	 * @see TreeDropTarget#getDropType()
	 */
	enum DropType {
		/**
		 * A drop is specified as "before", "after", or "within" a {@link TreeDropEvent#getRefNode()
		 * reference node}.
		 * 
		 * @see TreeDropEvent#getPos()
		 */
		ORDERED,

		/**
		 * A drop is only possible "within" a {@link TreeDropEvent#getRefNode() reference node}.
		 */
		CHILD,

		;
	}

	/**
	 * HTML data attribute that transports the {@link #getDropType()} to the client.
	 */
	String TL_DROPTYPE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "droptype";

	/**
	 * The {@link DropType}, this {@link TreeDropTarget} can interpret.
	 * 
	 * @see #TL_DROPTYPE_ATTR
	 */
	default DropType getDropType() {
		return DropType.ORDERED;
	}

	/**
	 * Whether drop is possible over the given target.
	 * 
	 * @param model
	 *        The potential target {@link TreeData}.
	 */
	default boolean dropEnabled(TreeData model) {
		return true;
	}

	/**
	 * Announces a drop operation on a tree.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see TreeDropEvent#getTarget()
	 */
	void handleDrop(TreeDropEvent event);

	/**
	 * Checking if a drop at the current position can be performed.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see TreeDropEvent#getTarget()
	 */
	default boolean canDrop(TreeDropEvent event) {
		return true;
	}

}

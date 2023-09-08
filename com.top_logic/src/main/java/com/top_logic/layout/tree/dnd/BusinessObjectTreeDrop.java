/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import java.util.Collection;

import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * Base class for {@link TreeDropTarget} implementations handling with business objects.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class BusinessObjectTreeDrop implements TreeDropTarget {

	@Override
	public void handleDrop(TreeDropEvent event) {
		handleDrop(event, TLTreeModelUtil.getInnerBusinessObjects(event.getData()));
	}

	@Override
	public boolean canDrop(TreeDropEvent event) {
		return canDrop(event, TLTreeModelUtil.getInnerBusinessObjects(event.getData()));
	}

	/**
	 * Handles the drop operation for the given objects.
	 * 
	 * @implSpec The implementor has to care about commit handling.
	 */
	protected abstract void handleDrop(TreeDropEvent event, Collection<?> droppedObjects);

	/**
	 * True if the dragged objects can be dropped at the current position.
	 * 
	 * @param event
	 *        {@link TreeDropEvent} containing informations about the drop.
	 * @param draggedObjects
	 *        Collection of objects to be dropped.
	 */
	public boolean canDrop(TreeDropEvent event, Collection<?> draggedObjects) {
		return true;
	}

}

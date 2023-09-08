/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd.handlers;

import java.util.Collection;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.table.dnd.BusinessObjectTableDrop;
import com.top_logic.layout.tree.dnd.TreeDropTarget;

/**
 * {@link TreeDropTarget} that demonstrates dropping over a tree with detailed position information.
 * 
 * @see DemoSimpleTreeDrop
 * @see DemoComponentDrop
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoRowTableDrop extends BusinessObjectTableDrop {

	@Override
	public DropType getDropType() {
		return DropType.ROW;
	}

	@Override
	public void handleDrop(Collection<?> droppedObjects, Object referenceRow) {
		InfoService.showInfo(I18NConstants.INSERT_TREE_DEFAULT__DATA_REF.fill(droppedObjects, referenceRow));
	}

	@Override
	public boolean canDrop(Collection<?> draggedObjects, Object referenceRow) {
		return true;
	}

}

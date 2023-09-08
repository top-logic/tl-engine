/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd.handlers;

import java.util.Collection;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.tree.dnd.BusinessObjectTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropEvent.Position;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TreeDropTarget} that demonstrates dropping over a tree with detailed position information.
 * 
 * @see DemoSimpleTreeDrop
 * @see DemoComponentDrop
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoOrderedTreeDrop extends BusinessObjectTreeDrop {

	@Override
	public DropType getDropType() {
		return DropType.ORDERED;
	}

	@Override
	public void handleDrop(TreeDropEvent event, Collection<?> droppedObjects) {
		TLTreeNode<?> refNode = (TLTreeNode<?>) event.getRefNode();
		TLTreeNode<?> refModel = (TLTreeNode<?>) refNode.getBusinessObject();
		Position pos = event.getPos();

		InfoService.showInfo(
			I18NConstants.INSERT_TREE_ORDERED__DATA_POS_REF.fill(droppedObjects, pos.ordinal(),
				refModel.getBusinessObject()));
	}

}

/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.util.List;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * An {@link AbstractSearchSelectableNodeHandler}, which selects the nearest parent node of a
 * given nnn-selectable node, in case the non-selectable node is not in path to root of the
 * last selected node.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectNearestParentHandler extends AbstractSearchSelectableNodeHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchSelectableNode(TLTreeModel tree,
			SingleSelectionModel selectionModel, Object node) {
		List<?> nodes = tree.createPathToRoot(selectionModel.getSingleSelection());
		if(!nodes.contains(node)) {
			Object parentNode = findParentNode(nodes, selectionModel);
			if(parentNode != null) {
				selectionModel.setSingleSelection(parentNode);
			}
		}
	}
	
	/**
	 * This method searches the path to root, to select the nearest parent
	 * 
	 * @param nodes - the path to root
	 * @return valid selectable parent node
	 */
	private Object findParentNode(List<?> nodes, SingleSelectionModel selectionModel) {
		Object validParent = null;
		for (Object node : nodes) {
			if(selectionModel.isSelectable(node)) {
				validParent = node;
				break;
			}
		}
		return validParent;
	}
}

/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * An {@link AbstractSearchSelectableNodeHandler}, which selects any selectable sub-node of a
 * given nnn-selectable node, in case the non-selectable node is not in path to root of the
 * last selected node.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectAnySubnodeHandler extends AbstractSearchSelectableNodeHandler {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void searchSelectableNode(TLTreeModel tree,
			SingleSelectionModel selectionModel, Object node) {
		List<?> nodes = tree.createPathToRoot(selectionModel.getSingleSelection());
		if(!nodes.contains(node)) {
			Object subNode = findSubNode(tree, selectionModel, node);
			if(subNode != null) {
				selectionModel.setSingleSelection(subNode);
			}
		}
	}
	
	/**
	 * This method performs depth-first search for a selectable sub-node 
	 * 
	 * @param tree - the TL-tree
	 * @param selectionModel - the selectionModel
	 * @param node - current node
	 * @return valid selectable subnode
	 */
	private Object findSubNode(TLTreeModel tree, SingleSelectionModel selectionModel, Object node) {
		Object validChildNode = null;
		if(selectionModel.isSelectable(node)) {
			validChildNode = node;
		}
		
		if(validChildNode == null) {
			List<?> children = tree.getChildren(node);
			for (Iterator<?> it = children.iterator(); validChildNode == null && it.hasNext();) {
                validChildNode = findSubNode(tree, selectionModel, it.next());
            }
		}
		
		return validChildNode;
	}
}

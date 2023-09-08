/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Base class for {@link InvalidSelectionHandler}s, which try to select another node
 * of a {@link TLTreeModel}, in case the current one is non-selectable.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractSearchSelectableNodeHandler implements InvalidSelectionHandler {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void handleInvalidSelection(TLTreeModel tree,
			SingleSelectionModel selectionModel, Object node) {
		searchSelectableNode(tree, selectionModel, node);
	}
	
	/**
	 * This method searches and selects another node, in case the selection of the current
	 * node is forbidden.
	 * 
	 * @param tree - the {@link TLTreeModel tree}, which contains the unselectable node
	 * @param selectionModel - the {@link SingleSelectionModel selection model}, which denies the node selection
	 * @param node - the node, which cannot be selected
	 */
	public abstract void searchSelectableNode(TLTreeModel tree, SingleSelectionModel selectionModel,
									 		  Object node);
}

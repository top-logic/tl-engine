/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * A handler designed to manage selection attempts of non-selectable nodes of a
 * {@link TLTreeModel}.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface InvalidSelectionHandler {
	
	/**
	 * This method will be called, if an non-selectable node
	 * 
	 * @param tree - the {@link TLTreeModel tree}, which contains the unselectable node
	 * @param selectionModel - the {@link SingleSelectionModel selection model}, which denies the node selection
	 * @param node - the node, which cannot be selected
	 */
	public void handleInvalidSelection(TLTreeModel tree, SingleSelectionModel selectionModel,
									   Object node);
}

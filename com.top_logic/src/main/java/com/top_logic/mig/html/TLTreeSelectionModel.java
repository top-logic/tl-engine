/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.mig.html;

import java.util.List;

import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TreeSelectionModel} with {@link TLTreeNode} as node type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLTreeSelectionModel extends TreeSelectionModel<TLTreeNode<?>> {

	/**
	 * Creates a new {@link TLTreeSelectionModel}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TLTreeSelectionModel(SelectionModelOwner owner) {
		super(owner, (Class) TLTreeNode.class);
	}

	@Override
	protected TLTreeNode<?> parent(TLTreeNode<?> node) {
		return node.getParent();
	}

	@Override
	protected List<? extends TLTreeNode<?>> children(TLTreeNode<?> node) {
		return node.getChildren();
	}

}


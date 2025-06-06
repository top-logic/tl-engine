/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.table.tree;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * {@link TreeTableNodeLabelNaming} for {@link DefaultTreeTableNode} whose model was created by a
 * {@link TreeTableComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeTableComponentNodeLabelNaming extends TreeTableNodeLabelNaming<DefaultTreeTableNode> {

	/**
	 * Creates a new {@link TreeTableComponentNodeLabelNaming}.
	 */
	public TreeTableComponentNodeLabelNaming() {
		super(DefaultTreeTableNode.class);

	}

	@Override
	protected TreeTableDataOwner getOwner(DefaultTreeTableNode node) {
		return TreeTableComponent.getOwner(node.getModel());
	}

}


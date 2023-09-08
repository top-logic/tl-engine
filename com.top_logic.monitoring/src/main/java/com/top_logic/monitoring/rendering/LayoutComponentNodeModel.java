/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link AbstractMutableTLTreeModel} for {@link ViewPerformanceTreeBuilder}.
 */
public class LayoutComponentNodeModel extends AbstractMutableTLTreeModel<LayoutComponentNode> {

	public LayoutComponentNodeModel(TreeBuilder<LayoutComponentNode> builder, Object rootUserObject) {
		super(builder, rootUserObject);
	}
}
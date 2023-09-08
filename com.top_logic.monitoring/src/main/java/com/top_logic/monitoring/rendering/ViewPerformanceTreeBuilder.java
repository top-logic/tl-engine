/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.component.TreeNodeBasedTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Tree builder for a {@link TreeComponent}.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class ViewPerformanceTreeBuilder extends TreeNodeBasedTreeModelBuilder<LayoutComponentNode> {

	/**
	 * Singleton {@link ViewPerformanceTreeBuilder} instance.
	 */
	public static final ViewPerformanceTreeBuilder INSTANCE = new ViewPerformanceTreeBuilder();

	private ViewPerformanceTreeBuilder() {
		// Singleton constructor.
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#getModel(Object,
	 *      com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		MainLayout mainLayout = aComponent.getMainLayout();
		LayoutComponentNodeModel model = new LayoutComponentNodeModel(new LayoutComponentNodeBuilder(), mainLayout);
		return model.getRoot();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, LayoutComponentNode node) {
		return null;
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof LayoutComponentNode;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}
}
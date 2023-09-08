/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link AbstractTreeUINodeModel} with type parameters bound.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTreeUINodeModel extends AbstractTreeUINodeModel<DefaultTreeUINodeModel.DefaultTreeUINode> {

	/**
	 * Creates a {@link DefaultTreeUINodeModel}.
	 * 
	 * @see AbstractTreeUINodeModel#AbstractTreeUINodeModel(TreeBuilder, Object)
	 */
	public DefaultTreeUINodeModel(TreeBuilder<DefaultTreeUINodeModel.DefaultTreeUINode> builder, Object rootUserObject) {
		super(builder, rootUserObject);
	}

	/**
	 * {@link TreeBuilder} that creates an empty {@link DefaultTreeUINodeModel}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DefaultTreeUIBuilder implements TreeBuilder<DefaultTreeUINode> {

		@Override
		public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
				DefaultTreeUINode parent, Object userObject) {
			return new DefaultTreeUINode(model, parent, userObject);
		}

		@Override
		public List<DefaultTreeUINodeModel.DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
			return new ArrayList<>();
		}

		@Override
		public boolean isFinite() {
			return true;
		}

	}

	/**
	 * Default {@link AbstractTreeUINodeModel.TreeUINode} implementation.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DefaultTreeUINode extends AbstractTreeUINodeModel.TreeUINode<DefaultTreeUINode> {

		/**
		 * Creates a {@link DefaultTreeUINode}.
		 * 
		 * @see AbstractTreeUINodeModel.TreeUINode#TreeUINode(AbstractMutableTLTreeModel,
		 *      com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode, Object)
		 */
		public DefaultTreeUINode(AbstractMutableTLTreeModel<DefaultTreeUINode> model, DefaultTreeUINode parent,
				Object businessObject) {
			super(model, parent, businessObject);
		}

	}

}

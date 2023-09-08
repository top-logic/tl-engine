/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Default {@link AbstractTreeTableModel} with type parameters bound.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTreeTableModel extends AbstractTreeTableModel<DefaultTreeTableModel.DefaultTreeTableNode> {

	/**
	 * Creates a {@link DefaultTreeTableModel}.
	 * 
	 * @see AbstractTreeTableModel#AbstractTreeTableModel(TreeBuilder, Object, List,
	 *      TableConfiguration)
	 */
	public DefaultTreeTableModel(TreeBuilder<DefaultTreeTableNode> builder, Object rootUserObject,
			List<String> columnNames, TableConfiguration config) {
		super(builder, rootUserObject, columnNames, config);
	}

	/**
	 * {@link TreeBuilder} that creates an empty {@link DefaultTreeTableModel}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DefaultTreeTableBuilder implements TreeBuilder<DefaultTreeTableNode> {

		@Override
		public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
				DefaultTreeTableNode parent, Object userObject) {
			return new DefaultTreeTableNode(model, parent, userObject);
		}

		@Override
		public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
			return new ArrayList<>();
		}

		@Override
		public boolean isFinite() {
			return true;
		}

	}

	/**
	 * Default {@link AbstractTreeTableModel.AbstractTreeTableNode} implementation.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DefaultTreeTableNode extends
			AbstractTreeTableModel.AbstractTreeTableNode<DefaultTreeTableNode> {

		/**
		 * Creates a {@link DefaultTreeTableNode}.
		 * 
		 * @see AbstractTreeTableModel.AbstractTreeTableNode#AbstractTreeTableNode(AbstractMutableTLTreeModel,
		 *      AbstractTreeTableNode, Object)
		 */
		public DefaultTreeTableNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
				DefaultTreeTableNode parent, Object businessObject) {
			super(model, parent, businessObject);
		}

	}

}

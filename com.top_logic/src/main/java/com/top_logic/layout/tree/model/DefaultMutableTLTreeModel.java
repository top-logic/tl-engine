/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * Mutable {@link TLTreeModel} implementation that is based on {@link DefaultMutableTLTreeNode}
 * implementations.
 * 
 * @see #getRoot() The starting point for building a tree.
 * @see StructureTreeModel An alternative implementation with nodes that implement
 *      {@link #equals(Object)} based on {@link #getBusinessObject(DefaultMutableTLTreeNode)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMutableTLTreeModel extends
		AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> implements
		MutableTLTreeModel {

	/**
	 * Creates a {@link AbstractMutableTLTreeModel} with
	 * {@link DefaultMutableTreeNodeBuilder#INSTANCE} as builder and <code>null</code> as root user
	 * object.
	 * 
	 * @see AbstractMutableTLTreeModel#AbstractMutableTLTreeModel(TreeBuilder, Object)
	 */
	public DefaultMutableTLTreeModel() {
		this(null);
	}

	/**
	 * Creates a {@link DefaultMutableTLTreeModel}.
	 *
	 * @see AbstractMutableTLTreeModel#AbstractMutableTLTreeModel(TreeBuilder, Object)
	 */
	public DefaultMutableTLTreeModel(Object rootUserObject) {
		this(DefaultMutableTreeNodeBuilder.INSTANCE, rootUserObject);
	}

	/**
	 * Creates a {@link DefaultMutableTLTreeModel}.
	 *
	 * @see AbstractMutableTLTreeModel#AbstractMutableTLTreeModel(TreeBuilder, Object)
	 */
	public DefaultMutableTLTreeModel(TreeBuilder<DefaultMutableTLTreeNode> builder,
			Object rootUserObject) {
		super(builder, rootUserObject);
	}

}

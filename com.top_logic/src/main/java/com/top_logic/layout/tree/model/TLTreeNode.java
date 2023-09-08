/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

import javax.swing.tree.TreeNode;

import com.top_logic.basic.col.TypedAnnotatable;

/**
 * Interface for nodes in a {@link AbstractMutableTLTreeModel}.
 * 
 * <p>
 * This interface is a modern version of {@link TreeNode}.
 * </p>
 * 
 * @param <N>
 *        The concrete node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTreeNode<N extends TLTreeNode<N>> extends TypedAnnotatable {

	/**
	 * The parent of this node.
	 */
	public N getParent();
	
	/**
	 * The unmodifiable list of children of this node.
	 */
	public List<? extends N> getChildren();

	/**
	 * The child at the the given index.
	 * 
	 * @param childIndex
	 *        must be &gt;= 0 and &lt; {@link #getChildCount() child count}
	 */
	public N getChildAt(int childIndex);
	
	/**
	 * The number of children of this node.
	 */
	public int getChildCount();
	
	/**
	 * The index of the given node among the {@link #getChildren()} of this
	 * node.
	 * 
	 * @return The index, where the given node can be found using the
	 *         {@link #getChildAt(int)} method, or <code>-1</code>, if the
	 *         given node is not among this node's children.
	 */
	public int getIndex(Object node);

	/**
	 * Whether this node is a leaf node and cannot have children.
	 */
	public boolean isLeaf();
	
	/**
	 * This node's business object.
	 * 
	 * @see #get(Property) for type-safe and more general annotations.
	 */
	public Object getBusinessObject();
}

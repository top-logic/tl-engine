/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * Algorithm for lazily building a nodes of an {@link AbstractMutableTLTreeModel}.
 * 
 * @see AbstractTLTreeNodeBuilder Generating {@link DefaultMutableTLTreeNode}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeBuilder<T extends AbstractMutableTLTreeNode<T>> {

	/**
	 * Create a new node object.
	 * 
	 * @param model
	 *        The tree model.
	 * @param parent
	 *        The parent node. <code>null</code> when the root node is created.
	 * @param userObject
	 *        The application object that should be wrapped into the new node.
	 * 
	 * @return The new tree node object. The returned node can be <code>null</code> in case it is
	 *         (for business or security reasons) not possible to build a node for the user object.
	 *         It is not <code>null</code> if the root node is created.
	 */
	T createNode(AbstractMutableTLTreeModel<T> model, T parent, Object userObject);

	/**
	 * Create the list of child nodes of the given node.
	 * 
	 * @param node
	 *        The node whose children are to be created.
	 * @return The list of children nodes of the given node.
	 */
	List<T> createChildList(T node);

	/**
	 * Whether the tree is guaranteed to be finite.
	 */
	boolean isFinite();

}

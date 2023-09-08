/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

import com.top_logic.basic.col.TreeView;

/**
 * Base for <i>TopLogic</i> tree models.
 * 
 * <p>
 * A concrete implementation either must implement {@link TLTreeModel} or {@link TreeAspectModel}
 * depending on whether the business node objects have a unique position in the tree or the business
 * node objects may occur multiple times as nodes of the tree.
 * </p>
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public interface TreeModelBase<N> extends TreeView<N> {

    /** 
     * The root node of this tree model.
     */
	N getRoot();

    /**
	 * The list of children of the given node.
	 * 
	 * <p>
	 * The resulting list contains the same elements as returned from
	 * {@link #getChildIterator(Object)}.
	 * </p>
	 * 
	 * @param parent
	 *        The node to get the children from. Must not be <code>null</code>.
	 * 
	 * @return the list of child nodes of the given parent. The result is never <code>null</code>.
	 *         The caller must not try to modify the resulting list. It is not guaranteed that the
	 *         returned list stays constant after this tree model is modified.
	 * 
	 * @see TreeView#getChildIterator(Object)
	 * @see TreeModelBase#childrenInitialized(Object) Check whether children are initialised.
	 */
	List<? extends N> getChildren(N parent);

	/**
	 * Whether the children of the given parent has initialised its children.
	 * 
	 * <p>
	 * This method is typically called before calling {@link #getChildIterator(Object)}, or
	 * {@link #getChildren(Object)} to avoid unnecessary initialisation of children.
	 * </p>
	 * 
	 * <p>
	 * If value is <code>false</code>, a subsequent call to {@link #getChildren(Object)} may be
	 * expensive if the children are built lazily.
	 * </p>
	 * 
	 * @param parent
	 *        The node to check for initialisation.
	 * 
	 * @return Whether the children of the node are already initialised.
	 * 
	 * @see #getChildren(Object)
	 * @see #resetChildren(Object)
	 */
	boolean childrenInitialized(N parent);

	/**
	 * Drops potentially initialized children of the given parent node.
	 * 
	 * @see #childrenInitialized(Object)
	 */
	void resetChildren(N parent);
    
    /**
     * Decide, whether the given node is part of this tree model.
     * 
     * <p>
     * If {@link #isLeaf(Object)} is <code>true</code>, the result is the empty list.
     * </p>
     * 
     * @param aNode
     *        the node in question. Must not be <code>null</code>.
     * @return whether the given node can be (transitively) reached through
     *         {@link #getRoot()}.{@link #getChildren(Object)}.
     */
	boolean containsNode(N aNode);
    
    /**
	 * Whether the given node is a direct child of the given parent node.
	 * 
	 * @param parent
	 *        The parent node.
	 * @param node
	 *        The potential child node.
	 * 
	 * @return <code>{@link #getChildren(Object) getChildren(parent)}.{@link List#contains(Object)
	 *          contains(node)}</code>
	 */
	boolean hasChild(N parent, Object node);
    
    /**
     * Whether the given node has any children.
     * 
     * @param aNode
     *        The node to check.
     * @return <code>{@link #getChildren(Object) getChildren(aNode)}.{@link List#size() size())} > 0</code>
     */
	boolean hasChildren(N aNode);

    /**
     * true when listener was registered (duplicates are suppressed).
     */
	boolean addTreeModelListener(TreeModelListener listener);
	
    /**
     * true when listener was actually registered.
     */
	boolean removeTreeModelListener(TreeModelListener listener);
    
}

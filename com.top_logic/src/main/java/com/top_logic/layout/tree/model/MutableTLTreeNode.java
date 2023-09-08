/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

import com.top_logic.layout.IndexPosition;

/**
 * Nodes of a {@link MutableTLTreeModel}.
 * 
 * <p>
 * Mutable aspect of {@link TLTreeNode}s.
 * </p>
 * 
 * @param <N>
 *        The concrete node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MutableTLTreeNode<N extends MutableTLTreeNode<N>> extends TLTreeNode<N> {

	/**
	 * The {@link MutableTLTreeModel} this node belongs to.
	 */
	public TLTreeModel<N> getModel();

	/**
	 * Method to determine whether this node is initialized, i.e whether some time before the list
	 * of children was accessed. It can be used to avoid useless initialization of this node.
	 * 
	 * @return    <code>true</code> if the list of children of this node was requested before.
	 */
	public boolean isInitialized();

	/**
	 * Creates a new child with the given user object and appends it to the list of children of this
	 * node.
	 * 
	 * @param businessObject
	 *        The {@link #getBusinessObject()} of the newly created node.
	 * 
	 * @return The newly created node, or <code>null</code> if no node could be created for the
	 *         given business object.
	 */
	public N createChild(Object businessObject);
	
	/**
	 * Creates a new child with the given user object and inserts it at the given index to the list
	 * of children of this node.
	 * 
	 * @param position
	 *        The {@link IndexPosition} where to add the new child.
	 * @param businessObject
	 *        The {@link #getBusinessObject()} of the newly created node.
	 * 
	 * @return The newly created node, or <code>null</code> if no node could be created for the
	 *         given business object.
	 */
	public N createChild(IndexPosition position, Object businessObject);

	/**
	 * Adds the given child at the end of the child list of this node.
	 * 
	 * @param child
	 *        The child to add, which must have been {@link #removeChild(int)
	 *        removed} from another node of the same {@link #getModel()}
	 */
	public void addChild(N child);

	/**
	 * Adds the given child at the given index to the list of children of this
	 * node.
	 * 
	 * @param position
	 *        The given child is added before the node with the given index. If
	 *        the index is {@link #getChildCount()}, the given child is appended
	 *        to the {@link #getChildren()}.
	 * @param child
	 *        The child to add, which must have been {@link #removeChild(int)
	 *        removed} from another node of the same {@link #getModel()}
	 */
	public void addChild(IndexPosition position, N child);

	/**
	 * Creates a new child for each user object in the given list and append them to the list of
	 * children of this node.
	 * 
	 * <p>
	 * It is possible that for some business object no node was created (for business reasons).
	 * Therefore the input list may contain more elements than the returned list.
	 * </p>
	 * 
	 * @return The newly created nodes.
	 */
	public List<? extends N> createChildren(List<?> businessObjects);

	/**
	 * Creates a new child for each user object in the given list and insert them at the given index
	 * into the list of children of this node.
	 * 
	 * <p>
	 * It is possible that for some business object no node was created (for business reasons).
	 * Therefore the input list may contain more elements than the returned list.
	 * </p>
	 * 
	 * @return The newly created nodes.
	 */
	public List<N> createChildren(IndexPosition position, List<?> userObjects);

	/**
	 * Removes the child at the given index.
	 * 
	 * @param index
	 *        The index in the {@link #getChildren()} list that should be
	 *        removed.
	 * @return The removed child node.
	 */
	public N removeChild(int index);

	/**
	 * Moves this node within the same {@link #getModel()} to the given parent.
	 * 
	 * <p>
	 * This node is removed from its current parent and added to the new parent
	 * node as last child.
	 * </p>
	 * 
	 * @param newParent
	 *        The new parent node to add this node as child to.
	 */
	public void moveTo(N newParent);

	/**
	 * Moves this node within the same {@link #getModel()} to the given parent.
	 * 
	 * <p>
	 * This node is removed from its current parent and inserted into the
	 * children list of the new parent node at the given index.
	 * </p>
	 * 
	 * @param newParent
	 *        The new parent node to add this node as child to.
	 * @param index
	 *        The index in the new parent's child list to add this node to.
	 */
	public void moveTo(N newParent, int index);

	/**
	 * Removes all children of this node.
	 */
	public void clearChildren();
	
	/**
	 * Updates the business object of this node. 
	 * 
	 * @return The old business object.
	 */
	public Object setBusinessObject(Object businessObject);
	
}

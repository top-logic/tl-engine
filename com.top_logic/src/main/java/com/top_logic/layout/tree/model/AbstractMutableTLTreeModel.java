/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import static java.util.Objects.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.IndexPosition;

/**
 * Mutable {@link TLTreeModel} implementation that is based on {@link DefaultMutableTLTreeNode}
 * implementations.
 * 
 * @param <N>
 *        The node type.
 * 
 * @see #getRoot() The starting point for building a tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AbstractMutableTLTreeModel<N extends AbstractMutableTLTreeNode<N>> extends AbstractTLTreeNodeModel<N> {

	/**
	 * Root node of this model
	 * 
	 * @see #getRoot()
	 */
	private N _root;

	/**
	 * The builder to create new nodes
	 * 
	 * @see #getBuilder()
	 */
	private final TreeBuilder<N> _builder;

	private final Object _rootUserObject;

	/**
	 * Creates a {@link AbstractMutableTLTreeModel}.
	 * 
	 * @param builder
	 *        The {@link TreeBuilder} to use for creating node objects.
	 * @param rootUserObject
	 *        The application object of the root node.
	 */
	public AbstractMutableTLTreeModel(TreeBuilder<N> builder, Object rootUserObject) {
		_builder = requireNonNull(builder);

		// Note: The root node cannot be initialized from the constructor,
		// because this would trigger the node added hook before the constructor
		// of the subclass had a chance to initialize this instance completely.
		_rootUserObject = rootUserObject;

	}

	@Override
	public N getRoot() {
		if (!isRootInitialized()) {
			_root = _builder.createNode(this, null, _rootUserObject);
			handleInitRoot(_root);
		}
		return _root;
	}

	/**
	 * Whether {@link #getRoot()} was already accessed.
	 */
	protected final boolean isRootInitialized() {
		return _root != null;
	}

	@Override
	public boolean childrenInitialized(N parent) {
		return parent.isInitialized();
	}

	@Override
	public void resetChildren(N parent) {
		parent.resetChildren();
	}

	@Override
	public boolean containsNode(N aNode) {
		if (aNode == null) {
			return false;
		}
		if (aNode.getModel() != this) {
			return false;
		}
		return aNode._partOfModel;
	}

	@Override
	public List<N> createPathToRoot(N aNode) {
		if (!containsNode(aNode)) {
			return Collections.emptyList();
		}
		return super.createPathToRoot(aNode);
	}

	/**
	 * Hook that informs that a subtree rooted at the given <code>subtreeRoot</code> was actively
	 * added to this model.
	 * 
	 * <p>
	 * Note: This method is not called, if a node was lazily initialized be the
	 * {@link #getBuilder()}.
	 * </p>
	 * 
	 * @param parent
	 *        the parent of the subtreeRoot. must not be <code>null</code> (as root can not be
	 *        added).
	 * @param position
	 *        The position, where the new subtree root was inserted into the children of the given
	 *        parent node.
	 * @param subtreeRoots
	 *        the root nodes of the added subtrees.
	 * @see #handleInitNode(AbstractMutableTLTreeNode)
	 */
	protected void handleAddSubtrees(N parent, IndexPosition position, List<N> subtreeRoots) {
		for (N subtreeRoot : subtreeRoots) {
			initSubtree(parent, subtreeRoot);
		}
	}

	/**
	 * Dispatch {@link #handleInitNode(AbstractMutableTLTreeNode)} to each initialized descendant of
	 * the given subtree root.
	 * 
	 * @param parent
	 *        the parent of the subtreeRoot. must not be <code>null</code> (as root can not be
	 *        added).
	 * @param subtreeRoot
	 *        the root of the added subtree. must not be <code>null</code>
	 * 
	 * @see #handleInitNode(AbstractMutableTLTreeNode)
	 */
	final void initSubtree(N parent, N subtreeRoot) {
		// If the parent is not part of this model, don't care about adding of some child.
		if (!containsNode(parent)) {
			return;
		}
		internalInitSubtree(subtreeRoot);
	}

	private void internalInitSubtree(N subtreeRoot) {
		handleInitNode(subtreeRoot);
		if (subtreeRoot.isInitialized()) {
			for (N child : subtreeRoot.getChildren()) {
				internalInitSubtree(child);
			}
		}
	}

	/**
	 * Informs that a subtree rooted by the given <code>subtreeRoot</code> was removed from this
	 * model.
	 * 
	 * For each initialized descendant, the method
	 * {@link #handleRemoveNode(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)} is called.
	 * 
	 * @param parent
	 *        the former parent of the subtreeRoot before the subtree was removed. must not be
	 *        <code>null</code> (as root can not be removed).
	 * @param subtreeRoot
	 *        the root of the removed subtree. must not be <code>null</code>
	 * 
	 * @see #handleRemoveNode(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)
	 */
	protected void handleRemoveSubtree(N parent, N subtreeRoot) {
		// If the parent is not part of this model, don't care about removal of some child.
		if (containsNode(parent)) {
			dropSubtree(parent, subtreeRoot);
		}
	}

	private void dropSubtree(N subtreeRootParent, N dropedNode) {
		if (dropedNode.isInitialized()) {
			for (N child : dropedNode.getChildren()) {
				dropSubtree(subtreeRootParent, child);
			}
		}
		handleRemoveNode(subtreeRootParent, dropedNode);
	}

	/**
	 * Hook called, when the root node of this model is initialized.
	 * 
	 * @param root
	 *        The {@link #getRoot()} node.
	 */
	protected void handleInitRoot(N root) {
		handleInitNode(root);
	}

	/**
	 * Hook called, whenever an node is initialized in this {@link AbstractMutableTLTreeModel}.
	 * 
	 * <p>
	 * Note: A node is initialized, if it is actively added to the model, or lazily created by the
	 * {@link #getBuilder()}.
	 * </p>
	 * 
	 * @param node
	 *        The new node.
	 * 
	 * @see #handleAddSubtrees(AbstractMutableTLTreeNode, IndexPosition, List)
	 */
	protected void handleInitNode(N node) {
		node._partOfModel = true;
	}

	/**
	 * Called, whenever a node is removed from this {@link AbstractMutableTLTreeModel}.
	 * 
	 * Is called by {@link #handleRemoveSubtree(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)}
	 * 
	 * @param subtreeRootParent
	 *        Parent of the removed subtree. A removed node whose parent is <code>null</code> was
	 *        previously a child of the given subtree root parent.
	 * @param node
	 *        The removed node.
	 * @see #handleRemoveSubtree(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)
	 */
	protected void handleRemoveNode(N subtreeRootParent, N node) {
		node._partOfModel = false;
	}

	/**
	 * Returns the builder to create new child nodes and the children of a given node.
	 * 
	 * @return must not be <code>null</code>
	 */
	public final TreeBuilder<N> getBuilder() {
		return _builder;
	}

	@Override
	public boolean isFinite() {
		return getBuilder().isFinite();
	}

}

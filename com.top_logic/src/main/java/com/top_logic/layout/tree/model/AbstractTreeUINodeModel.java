/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * {@link TreeUIModel} that keeps its display state in node objects.
 * 
 * @see AbstractTreeUINodeModel.TreeUINode
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTreeUINodeModel<N extends AbstractTreeUINodeModel.TreeUINode<N>> extends
		AbstractMutableTLTreeModel<N> implements TreeUIModel<N> {

	private boolean _rootVisible;

	/**
	 * Creates a {@link AbstractTreeUINodeModel}.
	 * 
	 * @see AbstractMutableTLTreeModel#AbstractMutableTLTreeModel(TreeBuilder, Object)
	 */
	public AbstractTreeUINodeModel(TreeBuilder<N> builder, Object rootUserObject) {
		super(builder, rootUserObject);
	}

	@Override
	public boolean isRootVisible() {
		return _rootVisible;
	}

	/**
	 * Setter for {@link #isRootVisible()}.
	 */
	public final void setRootVisible(boolean newVisibility) {
		if (newVisibility == _rootVisible) {
			return;
		}
		_rootVisible = newVisibility;

		if (isRootInitialized()) {
			fireTreeModelEvent(TreeModelEvent.BEFORE_STRUCTURE_CHANGE, getRoot());

			if (!newVisibility) {
				getRoot().setExpanded(true);
			}
		}

		handleRootVisible(newVisibility);

		if (isRootInitialized()) {
			fireTreeModelEvent(TreeModelEvent.AFTER_STRUCTURE_CHANGE, getRoot());
		}
	}

	/**
	 * Hook that informs about a change of {@link #isRootVisible()}
	 * 
	 * @param newVisibility
	 *        The new state of {@link #isRootVisible()}.
	 */
	protected void handleRootVisible(boolean newVisibility) {
		// Noting to do.
	}

	/**
	 * Node implementation of a {@link AbstractTreeUINodeModel}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class TreeUINode<N extends TreeUINode<N>> extends AbstractMutableTLTreeNode<N> {

		private boolean _expanded;

		private boolean _displayed;

		/**
		 * Creates a {@link AbstractTreeUINodeModel.TreeUINode}.
		 * 
		 * @see AbstractMutableTLTreeNode#AbstractMutableTLTreeNode(AbstractMutableTLTreeModel,
		 *      AbstractMutableTLTreeNode, Object)
		 */
		public TreeUINode(AbstractMutableTLTreeModel<N> model, N parent, Object businessObject) {
			super(model, parent, businessObject);
		}

		/**
		 * @see TreeUIModel#isExpanded(Object)
		 */
		public boolean isExpanded() {
			return _expanded;
		}

		/**
		 * @see TreeUIModel#setExpanded(Object, boolean)
		 */
		public boolean setExpanded(boolean expanded) {
			if (expanded == _expanded) {
				return false;
			}
			if (!expanded && this == getModel().getRoot() && !((TreeUIModel<?>) getModel()).isRootVisible()) {
				// Do not collapse invisible root node.
				return false;
			}

			getModel().fireTreeModelEvent(expanded ? TreeModelEvent.BEFORE_EXPAND : TreeModelEvent.BEFORE_COLLAPSE,
				this);
			_expanded = expanded;

			if (isDisplayed()) {
				displayChildren(expanded);
			}
			getModel().fireTreeModelEvent(expanded ? TreeModelEvent.AFTER_EXPAND : TreeModelEvent.AFTER_COLLAPSE, this);
			return true;
		}

		/**
		 * @see TreeUIModel#isDisplayed(Object)
		 */
		public boolean isDisplayed() {
			return _displayed;
		}

		/**
		 * Whether {@link #getChildren()} of this node are {@link #isDisplayed()}.
		 */
		public final boolean areChildrenDisplayed() {
			return isDisplayed() && isExpanded();
		}

		/**
		 * Adjusts the {@link #isDisplayed()} state of children of this node after changing the
		 * expansion state of an ancestor node.
		 * 
		 * @param displayed
		 *        Whether the parent displays its children.
		 */
		protected void displayChildren(boolean displayed) {
			if (isInitialized()) {
				for (N child : getChildren()) {
					child.setDisplayed(displayed);
				}
			}
		}

		void setDisplayed(boolean displayed) {
			if (displayed == _displayed) {
				return;
			}

			_displayed = displayed;

			if (isExpanded()) {
				displayChildren(displayed);
			}
		}
	}

	@Override
	protected void handleInitRoot(N root) {
		super.handleInitRoot(root);

		root.setDisplayed(true);

		if (!isRootVisible()) {
			root.setExpanded(true);
		}
	}

	@Override
	protected void handleInitNode(N node) {
		super.handleInitNode(node);
		N parent = node.getParent();
		if (parent != null && parent.areChildrenDisplayed()) {
			node.setDisplayed(true);
		}
	}

	@Override
	protected void handleRemoveNode(N subtreeRootParent, N node) {
		node.setDisplayed(false);
		super.handleRemoveNode(subtreeRootParent, node);
	}

	@Override
	public boolean isExpanded(N node) {
		return node.isExpanded();
	}

	@Override
	public boolean setExpanded(N node, boolean expanded) {
		return node.setExpanded(expanded);
	}

	@Override
	public boolean isDisplayed(N node) {
		return node.isDisplayed();
	}

	@Override
	public Object getUserObject(N node) {
		return node.getBusinessObject();
	}

}

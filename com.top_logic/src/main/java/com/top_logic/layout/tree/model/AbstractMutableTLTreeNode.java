/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.IndexPosition;
import com.top_logic.util.Utils;

/**
 * {@link MutableTLTreeNode} which interacts with its {@link AbstractMutableTLTreeModel model}.
 * 
 * @param <N>
 *        The concrete node type.
 * 
 * @see AbstractMutableTLTreeModel
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMutableTLTreeNode<N extends AbstractMutableTLTreeNode<N>> extends AbstractTLTreeNode<N>
		implements MutableTLTreeNode<N> {

	/**
	 * marker whether this node is part of its model.
	 */
	boolean _partOfModel = false;

	/**
	 * The model of this {@link AbstractMutableTLTreeNode}. never <code>null</code> and returned by
	 * {@link #getModel()}
	 * 
	 * @see #getModel()
	 */
	private final AbstractMutableTLTreeModel<N> _model;

	/**
	 * The list of children of this node.
	 */
	@Inspectable
	private List<N> _children;

	/**
	 * An unmodifiable view to the {@link #_children}.
	 * 
	 * @see #getChildren()
	 */
	private List<N> _childrenView;

	/**
	 * Constructs a new {@link AbstractMutableTLTreeNode}.
	 * 
	 * @param model
	 *        The model of this node. See {@link #getModel()}.
	 * @param parent
	 *        The parent node. See {@link #getParent()}.
	 * @param businessObject
	 *        The user object of this node. See {@link #getBusinessObject()}.
	 * 
	 * @see AbstractTLTreeNode#AbstractTLTreeNode(AbstractTLTreeNode, Object)
	 */
	public AbstractMutableTLTreeNode(AbstractMutableTLTreeModel<N> model, N parent, Object businessObject) {
		super(parent, businessObject);
		_model = model;
	}

	@Override
	public AbstractMutableTLTreeModel<N> getModel() {
		return _model;
	}

	@Override
	public void addChild(IndexPosition position, N child) {
		if (child.getModel() != this.getModel()) {
			throw new IllegalArgumentException("Must not add node of another model.");
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException("Must not add node that is a child of another node.");
		}
		final AbstractMutableTLTreeModel<N> model = getModel();
		if (child == model.getRoot()) {
			throw new IllegalArgumentException("Root node cannot be added to another node.");
		}

		final List<N> children = internalGetChildren();

		N result = child;
		result.initParent(self());
		children.add(position.beforeIndex(children), result);
		model.handleAddSubtrees(self(), position, Collections.singletonList(result));

		fireModelEvent(TreeModelEvent.AFTER_NODE_ADD, result);
	}

	/**
	 * Reference to this node with the concrete node type.
	 */
	@SuppressWarnings("unchecked")
	protected final N self() {
		return (N) this;
	}

	@Override
	public N createChild(Object businessObject) {
		return createChild(IndexPosition.AUTO, businessObject);
	}

	@Override
	public N createChild(IndexPosition position, Object businessObject) {
		final N newChild = internalCreateNode(businessObject);
		if (newChild == null) {
			return null;
		}
		final List<N> children = internalGetChildren();
		children.add(position.beforeIndex(children), newChild);
		getModel().handleAddSubtrees(self(), position, Collections.singletonList(newChild));

		fireModelEvent(TreeModelEvent.AFTER_NODE_ADD, newChild);

		return newChild;
	}

	@Override
	public final void addChild(N child) {
		addChild(IndexPosition.AUTO, child);
	}

	@Override
	public final List<N> createChildren(List<?> businessObjects) {
		return createChildren(IndexPosition.AUTO, businessObjects);
	}

	@Override
	public List<N> createChildren(IndexPosition position, List<?> userObjects) {
		if (userObjects.isEmpty()) {
			return Collections.emptyList();
		}
		List<N> newNodes = new ArrayList<>(userObjects.size());
		for (int n = 0, cnt = userObjects.size(); n < cnt; n++) {
			N newChild = internalCreateNode(userObjects.get(n));
			if (newChild != null) {
				newNodes.add(newChild);
			}
		}
		if (newNodes.isEmpty()) {
			return Collections.emptyList();
		}
		final List<N> children = internalGetChildren();

		fireModelEvent(TreeModelEvent.BEFORE_STRUCTURE_CHANGE, self());

		children.addAll(position.beforeIndex(children), newNodes);
		getModel().handleAddSubtrees(self(), position, newNodes);

		fireModelEvent(TreeModelEvent.AFTER_STRUCTURE_CHANGE, self());
		return newNodes;
	}

	@Override
	public N removeChild(int index) {
		if (isLeaf()) {
			throw new IndexOutOfBoundsException("Cannot remove child nodes from a leaf node.");
		}
		final List<N> children = internalGetChildren();

		N removedNode = children.get(index);
		fireModelEvent(TreeModelEvent.BEFORE_NODE_REMOVE, removedNode);

		N result = children.remove(index);
		assert result == removedNode : "Fireing event may have change list of children";
		result.notifyRemoved();
		getModel().handleRemoveSubtree(self(), result);

		fireModelEvent(TreeModelEvent.AFTER_NODE_REMOVE, removedNode);
		return result;
	}

	@Override
	public void moveTo(N newParent) {
		moveTo(newParent, newParent.getChildCount());
	}

	@Override
	public void moveTo(N newParent, int index) {
		N oldParent = getParent();
		if (oldParent == null) {
			if (this == getModel().getRoot()) {
				throw new IllegalArgumentException("Root node cannot be moved.");
			} else {
				throw new IllegalArgumentException("Node not part of a model. Use addChild() instead.");
			}
		}
		if (newParent.getModel() != this.getModel()) {
			throw new IllegalArgumentException("Must not move node to another model.");
		}

		N commonAncestor = TLTreeModelUtil.getCommonAncestor(self(), newParent);
		assert commonAncestor != null : "At least the root node must be found as common ancestor.";

		if (commonAncestor == this) {
			throw new IllegalArgumentException("Must not move the node to a descendant or self node.");
		}

		final List<N> oldParentsChildren =
			oldParent.internalGetChildren();
		final int currentIndex = oldParent.getIndex(this);
		final int insertIndex;
		if (oldParent == newParent) {
			if (index == oldParentsChildren.size()) {
				/* Since the node to move is first removed from the child list the index to insert
				 * has to be adjusted */
				insertIndex = index - 1;
			} else {
				insertIndex = index;
			}

			if (currentIndex == insertIndex) {
				// the move is a noop: from a parent to itself to the same position
				return;
			}
		} else {
			insertIndex = index;
		}

		fireModelEvent(TreeModelEvent.BEFORE_STRUCTURE_CHANGE, commonAncestor);

		// remove from old parent
		N self = oldParentsChildren.remove(currentIndex);
		this.notifyRemoved();

		/* Must explicit have an AbstractMutableTlTreeNode to be able to access the private method
		 * mkChildren() in Java 1.7.*/
		AbstractMutableTLTreeNode<N> castedNewParent = newParent;
		// add to new parent as child
		final List<N> newParentsChildren = castedNewParent.internalGetChildren();

		this.initParent(newParent);
		newParentsChildren.add(insertIndex, self);

		fireModelEvent(TreeModelEvent.AFTER_STRUCTURE_CHANGE, commonAncestor);
	}

	@Override
	public void clearChildren() {
		if (isLeaf()) {
			return;
		}
		final List<N> children = internalGetChildren();
		if (children.isEmpty()) {
			return;
		}
		fireModelEvent(TreeModelEvent.BEFORE_STRUCTURE_CHANGE, self());

		removeNodes(children);
		children.clear();

		fireModelEvent(TreeModelEvent.AFTER_STRUCTURE_CHANGE, self());
	}

	/**
	 * Removes all nodes in the given list, i.e. notifies the sole nodes that they are
	 * {@link #notifyRemoved() removed} and informs the
	 * {@link AbstractMutableTLTreeModel#handleRemoveSubtree(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)
	 * model about the removal}.
	 * 
	 * @param childNodes
	 *        list of child nodes of this node to remove. must not be <code>null</code>.
	 */
	private void removeNodes(List<N> childNodes) {
		for (N node : childNodes) {
			node.notifyRemoved();
			getModel().handleRemoveSubtree(self(), node);
		}
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		boolean setBefore = isSet(property);
		T oldValue = super.set(property, value);

		boolean changed = !setBefore || !Utils.equals(value, oldValue);
		if (changed) {
			fireModelEvent(TreeModelEvent.NODE_CHANGED, self());
		}

		return oldValue;
	}

	@Override
	public <T> T reset(Property<T> property) {
		boolean changed = isSet(property);
		if (!changed) {
			// Prevent event.
			return get(property);
		}
		T result = super.reset(property);
		fireModelEvent(TreeModelEvent.NODE_CHANGED, self());
		return result;
	}

	// Make public.
	@Override
	public Object setBusinessObject(Object businessObject) {
		Object oldValue = super.setBusinessObject(businessObject);
		if (!Utils.equals(businessObject, oldValue)) {
			fireModelEvent(TreeModelEvent.NODE_CHANGED, self());
		}
		return oldValue;
	}

	/**
	 * Locally update this node keeping its substructure.
	 * 
	 * @see #setBusinessObject(Object)
	 * @see #updateNodeStructure()
	 */
	public void updateNodeProperties() {
		fireModelEvent(TreeModelEvent.NODE_CHANGED, self());
	}

	/**
	 * Whether this node is part of its model.
	 */
	public final boolean isAlive() {
		return getParent() != null || getModel().getRoot() == this;
	}

	/**
	 * fires the given event on the model given by {@link #getModel()} if this node is contained in
	 * the model.
	 * 
	 * @param treeModelEvent
	 *        the event type
	 * @param node
	 *        the changed node
	 */
	protected void fireModelEvent(int treeModelEvent, N node) {
		final AbstractMutableTLTreeModel<N> model = getModel();
		if (model.containsNode(self())) {
			model.fireTreeModelEvent(treeModelEvent, node);
		}
	}

	/**
	 * Creates a new node to act as child node of this node.
	 * 
	 * @param userObject
	 *        the user object of the returned node.
	 * 
	 * @return A new node acting as child of this node. The node may be <code>null</code> if the
	 *         {@link TreeBuilder} does not accept it.
	 * 
	 * @see #internalCreateChildren()
	 */
	protected N internalCreateNode(Object userObject) {
		AbstractMutableTLTreeModel<N> model = getModel();
		return model.getBuilder().createNode(model, self(), userObject);
	}

	/**
	 * Creates the children for this node.
	 * 
	 * @return The list of children later returned by {@link #internalGetChildren()}.
	 * 
	 * @see #internalCreateNode(Object)
	 */
	protected List<N> internalCreateChildren() {
		AbstractMutableTLTreeModel<N> model = getModel();
		return model.getBuilder().createChildList(self());
	}

	/**
	 * Returns the modifiable not <code>null</code> implementation list of children of this
	 * {@link AbstractMutableTLTreeNode}. The returned list is modified on structure changes of a
	 * node.
	 * 
	 * {@link #getChildren()} returns a view to the returned list.
	 */
	protected List<N> internalGetChildren() {
		if (this._children == null) {
			final AbstractMutableTLTreeModel<N> model = getModel();
			final List<N> childList = internalCreateChildren();
			assert childList != null : "The children list must not be null after initialization.";

			initChildren(childList);

			if (model.containsNode(self())) {
				// must be checked as a non initialized removed node could be asked for its
				// children which must not become part of the model
				for (N newlyCreatedChild : childList) {
					model.initSubtree(self(), newlyCreatedChild);
				}
			}
		}
		return this._children;
	}

	private void initChildren(final List<N> childList) {
		_children = childList;
		_childrenView = Collections.unmodifiableList(childList);
	}

	@Override
	public List<N> getChildren() {
		// trigger creation of lazy constructed child list
		internalGetChildren();
		return _childrenView;
	}

	/**
	 * Method to determine whether this node is initialized, i.e whether some time before the list
	 * of children was accessed. It can be used to avoid useless initialization of this node.
	 * 
	 * @return <code>true</code> iff the list of children of this node was requested before.
	 */
	@Override
	public boolean isInitialized() {
		return this._children != null;
	}

	/**
	 * Drops children of this node.
	 */
	protected void resetChildren() {
		if (_children != null) {
			fireModelEvent(TreeModelEvent.BEFORE_STRUCTURE_CHANGE, self());
			if (_children.size() > 0) {
				removeNodes(_children);
			}
			_children = null;
			_childrenView = null;
			fireModelEvent(TreeModelEvent.AFTER_STRUCTURE_CHANGE, self());
		} else {
			fireModelEvent(TreeModelEvent.NODE_CHANGED, self());
		}
	}

	/**
	 * Resets the sub structure rooted at the given node.
	 * 
	 * After calling this methods all changes manually done with the child list of the node (e.g.
	 * removing child or adding one) are skipped.
	 * 
	 * @see #updateNodeProperties()
	 */
	public void updateNodeStructure() {
		resetChildren();
	}

}

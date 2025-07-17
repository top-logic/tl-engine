/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.StructureView;
import com.top_logic.basic.col.filter.FilterFactory;

/**
 * A selection model that supports multiple selection in tree-like structures.
 * 
 * <p>
 * The selection model does not only provide information, whether a node is selected or not but also
 * records whether there is a descendant node (below a potentially collapsed node) that is selected.
 * </p>
 * 
 * <p>
 * There are six selection states per node, see {@link NodeSelectionState}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTreeMultiSelectionModel<T> extends AbstractRestrainedSelectionModel<T> {
	
	private final StructureView<T> _treeModel;

	/**
	 * Optimized selection state for nodes.
	 * 
	 * <p>
	 * A node is considered selected, if there is an entry for that node in the state map and the
	 * entrie's {@link NodeSelectionState} is {@link NodeSelectionState#isSelected()}, or there is an
	 * ancestor node with an entry in the map that has a {@link DescendantState} of
	 * {@link DescendantState#ALL}.
	 * </p>
	 */
	private final Map<T, NodeSelectionState> _state = new HashMap<>();

	/**
	 * Description of the selection state of the descendants of a tree node.
	 */
	public static enum DescendantState {
		/**
		 * None of the descendants of the node are selected.
		 */
		NONE,

		/**
		 * There are descendants of the node that are selected.
		 */
		SOME,

		/**
		 * All descendants of the node are selected.
		 */
		ALL;

		boolean isHomogeneous() {
			return switch (this) {
				case NONE, ALL -> true;
				case SOME -> false;
			};
		}
	}

	/**
	 * Description of the selection state of a tree node.
	 */
	public static enum NodeSelectionState {
		/**
		 * Neither the node nor any of its descendants is selected.
		 */
		NONE(false, DescendantState.NONE),

		/**
		 * The node is not selected, but there are selected descendants.
		 */
		SOME_DESCENDANTS(false, DescendantState.SOME),

		/**
		 * The node is not selected, but all of its descendants.
		 */
		ALL_DESCENDANTS(false, DescendantState.ALL),

		/**
		 * The node is selected, but none of its descendants.
		 */
		SELECTED_NO_DESCENDANTS(true, DescendantState.NONE),

		/**
		 * The node and some of its descendants are selected.
		 */
		SELECTED_SOME_DESCENDANTS(true, DescendantState.SOME),

		/**
		 * The node and all of its its descendants are selected.
		 */
		FULL(true, DescendantState.ALL);

		DescendantState _childrenState;

		boolean _selected;

		/**
		 * Creates a {@link NodeSelectionState}.
		 */
		private NodeSelectionState(boolean selected, DescendantState childState) {
			_selected = selected;
			_childrenState = childState;
		}

		/**
		 * Whether the current node is selected itself.
		 */
		boolean isSelected() {
			return _selected;
		}

		/**
		 * Whether all children are selected.
		 */
		boolean allChildren() {
			return childrenState() == DescendantState.ALL;
		}

		DescendantState childrenState() {
			return _childrenState;
		}

		static NodeSelectionState valueOf(boolean selected, DescendantState childrenState) {
			if (selected) {
				return switch (childrenState) {
					case NONE -> SELECTED_NO_DESCENDANTS;
					case SOME -> SELECTED_SOME_DESCENDANTS;
					case ALL -> FULL;
				};
			} else {
				return switch (childrenState) {
					case NONE -> NONE;
					case SOME -> SOME_DESCENDANTS;
					case ALL -> ALL_DESCENDANTS;
				};
			}
		}

		/**
		 * Whether the complete subtree has the same selection state.
		 */
		boolean isHomogeneous() {
			return _selected ? _childrenState == DescendantState.ALL : _childrenState == DescendantState.NONE;
		}
	}

	/**
	 * Create a new DefaultMultiSelectionModel, which allows to select all objects
	 *
	 */
	public DefaultTreeMultiSelectionModel(SelectionModelOwner owner, StructureView<T> treeModel) {
		this(null, owner, treeModel);
	}
	
	/**
	 * Create a new DefaultMultiSelectionModel, with a subset of non selectable objects
	 * 
	 * @param selectionFilter
	 *        - the {@link Filter filter}, which defines, if an object is selectable, or not
	 */
	public DefaultTreeMultiSelectionModel(Filter<? super T> selectionFilter, SelectionModelOwner owner,
			StructureView<T> treeModel) {
		super(owner);
		_treeModel = treeModel;
		setSelectionFilter(selectionFilter);
		setDeselectionFilter(FilterFactory.trueFilter());
	}
	
	@Override
	public boolean isSelectable(T obj) {
		return getSelectionFilter().accept(obj);
	}
	
	/**
	 * true, if the given object can be removed from set of selected objects.
	 */
	public boolean isDeselectable(T obj) {
		return getDeselectionFilter().accept(obj);
	}

	@Override
	public boolean isSelected(T obj) {
		return getTreeSelectionState(obj).isSelected();
	}	
	
	/**
	 * Retrieves the selection state for the given object.
	 * 
	 * <p>
	 * The selection state is only stored specifically for an object, if it cannot be derived from
	 * the selection state of its parent, see {@link DescendantState#ALL} and
	 * {@link DescendantState#NONE}.
	 * </p>
	 */
	public NodeSelectionState getTreeSelectionState(T obj) {
		NodeSelectionState state = _state.get(obj);
		if (state != null) {
			return state;
		}

		T ancestor = obj;
		while (true) {
			ancestor = _treeModel.getParent(ancestor);
			if (ancestor == null) {
				// No node on the path to parent has information about its children.
				return NodeSelectionState.NONE;
			}

			NodeSelectionState ancestorState = _state.get(ancestor);
			if (ancestorState != null) {
				return ancestorState.allChildren() ? NodeSelectionState.FULL : NodeSelectionState.NONE;
			}
		}
	}

	@Override
	public void setSelected(T obj, boolean select) {
		if (obj == null) {
			throw new IllegalArgumentException("Selected object may not be null.");
		}
		
		if (select) {
			if (!isSelectable(obj)) {
				return;
			}

			internalSetSelected(obj, select);
		} else {
			if (!isDeselectable(obj)) {
				return;
			}

			internalSetSelected(obj, select);
		}
	}

	/**
	 * Updates the selection state of the whole sub-tree rooted at the given node.
	 */
	public void setSelectedSubtree(T obj, boolean select) {
		NodeSelectionState oldState = getTreeSelectionState(obj);
		if (select == oldState.isSelected() && oldState.isHomogeneous()) {
			// No change.
			return;
		}

		HashSet<T> oldSelection = new HashSet<>(getSelection());

		NodeSelectionState newState = select ? NodeSelectionState.FULL : NodeSelectionState.NONE;
		updateLocalState(obj, newState, select);

		// Clear redundant descendant state.
		clearDescendantState(obj);

		fireSelectionChanged(oldSelection);
	}

	private void clearDescendantState(T parent) {
		Iterator<? extends T> it = _treeModel.getChildIterator(parent);
		while (it.hasNext()) {
			T child = it.next();
			NodeSelectionState before = _state.remove(child);

			if (before != null && before.childrenState() != DescendantState.NONE) {
				clearDescendantState(child);
			}
		}
	}

	private void internalSetSelected(T obj, boolean select) {
		NodeSelectionState oldState = getTreeSelectionState(obj);
		if (select == oldState.isSelected()) {
			// No change.
			return;
		}

		HashSet<T> oldSelection = new HashSet<>(getSelection());

		DescendantState newChildrenState;
		if (!_treeModel.hasChildren(obj)) {
			// A completely selected subtree must be marked as FULL, a subtree without any
			// selections with NONE. Therefore, a node without children cannot use the states
			// SELECTED_NO_CHILDREN and ALL_CHILDREN.
			newChildrenState = select ? DescendantState.ALL : DescendantState.NONE;
		} else {
			newChildrenState = oldState.childrenState();
		}
		
		NodeSelectionState newState = NodeSelectionState.valueOf(select, newChildrenState);
		updateLocalState(obj, newState, select);

		fireSelectionChanged(oldSelection);
	}


	private void updateLocalState(T obj, NodeSelectionState newState, boolean select) {
		// Note: Event "not selected" must be temporarily stored, since the states are currently
		// inconsistent and must be updated after the parent has been updated.
		_state.put(obj, newState);

		updateParent(obj, newState, select);

		// Clean up redundant state.
		if (newState == NodeSelectionState.NONE) {
			_state.remove(obj);
		}
	}

	private void updateParent(T obj, NodeSelectionState newChildState, boolean select) {
		T parent = _treeModel.getParent(obj);
		if (parent == null) {
			// No more nodes to update.
			return;
		}

		// Update parent information
		NodeSelectionState oldParentState = _state.get(parent);

		DescendantState newChildrenState;
		if (oldParentState == null) {
			// This means either NONE, or FULL (depending on some ancestor state). Since the update
			// is performed, because some selection changed in the subtree, the state must have
			// been NONE, if a node was selected (otherwise, the descendant node would have been
			// selected before), and FULL, if a node was deselected.

			oldParentState = select ? NodeSelectionState.NONE : NodeSelectionState.FULL;
			newChildrenState = newChildrenStateHomogeneous(parent, newChildState);
		} else {
			newChildrenState = switch (oldParentState.childrenState()) {
				case NONE, ALL -> newChildrenStateHomogeneous(parent, newChildState);
				case SOME -> newChildState.isHomogeneous()
					? computeChildrenState(parent, oldParentState)
					: DescendantState.SOME;
			};
		}

		NodeSelectionState newParentState = NodeSelectionState.valueOf(oldParentState.isSelected(), newChildrenState);

		_state.put(parent, newParentState);

		if (newParentState.childrenState().isHomogeneous()) {
			// Clean up redundant state.
			clearChildState(parent);
		} else if (oldParentState.childrenState().isHomogeneous()) {
			// Re-establish separate state for children.
			NodeSelectionState implicitChildState = oldParentState.childrenState() == DescendantState.ALL
				? NodeSelectionState.FULL
				: NodeSelectionState.NONE;
			Iterator<? extends T> it = _treeModel.getChildIterator(parent);
			while (it.hasNext()) {
				T child = it.next();
				_state.putIfAbsent(child, implicitChildState);
			}
		}

		if (newParentState != oldParentState) {
			updateParent(parent, newParentState, select);
		}
	}

	private void clearChildState(T parent) {
		Iterator<? extends T> it = _treeModel.getChildIterator(parent);
		while (it.hasNext()) {
			T child = it.next();
			_state.remove(child);
		}
	}

	/**
	 * The states of the children of the given parent have change in some complicated way, the new
	 * description of the overall children state has to be computed from scratch.
	 */
	private DescendantState computeChildrenState(T parent, NodeSelectionState oldParentState) {
		DescendantState result = null;

		Iterator<? extends T> it = _treeModel.getChildIterator(parent);
		while (it.hasNext()) {
			T child = it.next();

			DescendantState childResult;
			NodeSelectionState childState = _state.get(child);
			if (childState == null) {
				// Implicit state.
				childResult = oldParentState.childrenState().isHomogeneous()
					// Same as (old) parent state - cannot happen, since only called in complicated
					// situation.
					? oldParentState.childrenState()
					// Just not selected - vote for NONE.
					: DescendantState.NONE;
			} else if (childState.isHomogeneous()) {
				childResult = childState.childrenState();
			} else {
				// No further inspection required.
				return DescendantState.SOME;
			}

			if (result == null) {
				result = childResult;
			} else if (result != childResult) {
				return DescendantState.SOME;
			}
		}

		if (result == null) {
			// No children, make homogeneous. Cannot happen, since then the descendant state of the
			// parent would have been homogeneous before, and this method would not have been
			// called, since it is only called in complicated situation.
			return oldParentState.isSelected() ? DescendantState.ALL : DescendantState.NONE;
		}

		return result;
	}

	/**
	 * The new {@link DescendantState} for a parent that had a homogeneous child selection before, if
	 * some of its descendants has changed to the given new state.
	 */
	private DescendantState newChildrenStateHomogeneous(T parent, NodeSelectionState newState) {
		DescendantState childrenState;
		if (hasSingleChild(parent)) {
			// The parent follows its single child.
			childrenState = newState.childrenState();
		} else {
			// There are other children, therefore the state becomes mixed.
			childrenState = DescendantState.SOME;
		}
		return childrenState;
	}

	private boolean hasSingleChild(T node) {
		Iterator<? extends T> it = _treeModel.getChildIterator(node);
		if (it.hasNext()) {
			// There is one child.
			it.next();

			// Check whether there are no more.
			return !it.hasNext();
		}

		// This should never happen, since the query is only done, when there is at least one child.
		return false;
	}

	@Override
	public Set<? extends T> getSelection() {
		HashSet<T> result = new HashSet<>();
		for (Entry<T, NodeSelectionState> entry : _state.entrySet()) {
			NodeSelectionState state = entry.getValue();
			if (state.isSelected()) {
				result.add(entry.getKey());
			}

			if (state.childrenState() == DescendantState.ALL) {
				addDescendants(result, entry.getKey());
			}
		}
		return result;
	}

	private void addDescendants(HashSet<T> result, T node) {
		Iterator<? extends T> it = _treeModel.getChildIterator(node);
		while (it.hasNext()) {
			T child = it.next();
			result.add(child);
			addDescendants(result, child);
		}
	}

}

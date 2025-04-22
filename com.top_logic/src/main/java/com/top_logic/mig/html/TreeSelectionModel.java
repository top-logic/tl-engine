/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.util.AbstractListeners;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link AbstractMultiSelectionModel} for {@link TLTreeModel}.
 * 
 * <p>
 * The selection strategy in a {@link TreeSelectionModel} differs from the
 * {@link DefaultMultiSelectionModel}:
 * <ol>
 * <li>When a node is selected, the whole subtree starting from this node is selected.</li>
 * <li>When a node is de-selected, the whole subtree starting from this node is de-selected.</li>
 * <li>A node is selected, iff the whole subtree is selected.</li>
 * <li>A node is de-selected, iff the whole subtree is de-selected.</li>
 * </ol>
 * </p>
 * 
 * <p>
 * Nodes that have both, selected and de-selected children have {@link TriState#INDETERMINATE
 * indeterminate state}.
 * </p>
 * 
 * @implNote {@link #isSelected(Object)} returns <code>true</code> iff the whole subtree is
 *           selected.
 * @implNote {@link #getSelection()} contains the root nodes of the completely selected subtrees.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeSelectionModel<N> extends AbstractMultiSelectionModel {

	/**
	 * Event object used by {@link TreeSelectionListener}.
	 */
	public static class StateChanged<N> {

		private final TreeSelectionModel<N> _sender;

		private final Map<N, TriState> _formerStates;

		/**
		 * Creates a {@link StateChanged}.
		 */
		public StateChanged(TreeSelectionModel<N> sender, Map<N, TriState> formerStates) {
			super();
			_sender = sender;
			_formerStates = formerStates;
		}

		/**
		 * The {@link TreeSelectionModel} in which the states changed.
		 */
		public TreeSelectionModel<N> sender() {
			return _sender;
		}

		/**
		 * The stored states before the change.
		 * 
		 * @see #states()
		 */
		public Map<N, TriState> formerStates() {
			return _formerStates;
		}

		/**
		 * The state of the given node before the change.
		 * 
		 * @see #state(Object)
		 */
		public TriState formerState(N node) {
			return sender().getState(formerStates(), node);
		}

		/**
		 * The current state of the model.
		 * 
		 * @see #formerStates()
		 */
		public Map<N, TriState> states() {
			return sender().getStates();
		}

		/**
		 * The current state of the given node.
		 * 
		 * @see #formerState(Object)
		 */
		public TriState state(N node) {
			return sender().getState(node);
		}

	}

	/**
	 * Listener interfaces that is informed about changes {@link TreeSelectionModel}s.
	 */
	public static interface TreeSelectionListener<N> extends EventListener {

		/**
		 * Notifies the receiver about changes in the stores states.
		 * 
		 * @param event
		 *        Object holding change informations.
		 */
		void handleStateChanged(StateChanged<N> event);

	}

	private static class Listeners<N> extends AbstractListeners<TreeSelectionListener<N>, StateChanged<N>> {

		@Override
		protected void sendEvent(TreeSelectionListener<N> listener, StateChanged<N> event) {
			listener.handleStateChanged(event);
		}

	}

	private final Map<N, TriState> _states = new HashMap<>();

	private final Map<N, TriState> _statesView = Collections.unmodifiableMap(_states);

	private final Listeners<N> _listeners = new Listeners<>();

	private final Class<N> _nodeType;

	/**
	 * Creates a {@link TreeSelectionModel}.
	 */
	public TreeSelectionModel(SelectionModelOwner owner, Class<N> nodeType) {
		super(owner);
		_nodeType = nodeType;
	}

	/**
	 * The {@link TLTreeModel} holding the nodes which can be selected by this
	 * {@link TreeSelectionModel}.
	 */
	public abstract TLTreeModel<N> model();

	@Override
	public boolean isSelectable(Object obj) {
		return super.isSelectable(obj) && hasNodeType(obj);
	}

	private boolean hasNodeType(Object obj) {
		return _nodeType.isInstance(obj);
	}

	private N cast(Object obj) {
		return _nodeType.cast(obj);
	}

	@Override
	public boolean isSelected(Object obj) {
		if (!hasNodeType(obj)) {
			return false;
		}
		return getState(cast(obj)) == TriState.SELECTED;
	}

	@Override
	public void setSelected(Object obj, boolean select) {
		Set<?> oldSelection = null;
		Map<N, TriState> oldStates = null;
		if (hasListeners()) {
			oldSelection = getSelection();
		}
		if (_listeners.hasRegisteredListeners()) {
			oldStates = new HashMap<>(_states);
		}
		boolean changed = internalSetSelected(obj, select);
		if (changed) {
			if (hasListeners()) {
				fireSelectionChanged(oldSelection);
			}
			if (_listeners.hasRegisteredListeners()) {
				_listeners.notifyListeners(new StateChanged<>(this, oldStates));
			}
		}
	}

	private boolean internalSetSelected(Object obj, boolean select) {
		if (select) {
			if (!isSelectable(obj)) {
				return false;
			}
		} else {
			if (!isDeselectable(obj)) {
				return false;
			}
		}
		boolean selectionChanged = internalSelect(model(), cast(obj), select);
		if (selectionChanged) {
			setLastSelected(obj);
		}
		return selectionChanged;
	}

	@Override
	public void addToSelection(Collection<?> objects) {
		switch (objects.size()) {
			case 0:
				break;
			case 1:
				setSelected(objects.iterator().next(), true);
				break;
			default:
				Set<?> oldSelection = null;
				Map<N, TriState> oldStates = null;
				if (hasListeners()) {
					oldSelection = getSelection();
				}
				if (_listeners.hasRegisteredListeners()) {
					oldStates = new HashMap<>(_states);
				}
				boolean changed = false;
				for (Object obj : objects) {
					changed |= internalSetSelected(obj, true);
				}
				if (changed) {
					if (hasListeners()) {
						fireSelectionChanged(oldSelection);
					}
					if (_listeners.hasRegisteredListeners()) {
						_listeners.notifyListeners(new StateChanged<>(this, oldStates));
					}
				}
				break;
		}
	}

	@Override
	public void removeFromSelection(Collection<?> objects) {
		switch (objects.size()) {
			case 0:
				break;
			case 1:
				setSelected(objects.iterator().next(), false);
				break;
			default:
				Set<?> oldSelection = null;
				Map<N, TriState> oldStates = null;
				if (hasListeners()) {
					oldSelection = getSelection();
				}
				if (_listeners.hasRegisteredListeners()) {
					oldStates = new HashMap<>(_states);
				}
				boolean changed = false;
				for (Object obj : objects) {
					changed |= internalSetSelected(obj, false);
				}
				if (changed) {
					if (hasListeners()) {
						fireSelectionChanged(oldSelection);
					}
					if (_listeners.hasRegisteredListeners()) {
						_listeners.notifyListeners(new StateChanged<>(this, oldStates));
					}
				}
				break;

		}
	}

	@Override
	public Set<?> getSelection() {
		return calculateSelected();
	}

	@Override
	public void setSelection(Set<?> newSelection, Object lead) {
		Set<?> oldSelection = null;
		Map<N, TriState> oldStates = null;
		if (hasListeners()) {
			oldSelection = getSelection();
		}
		if (_listeners.hasRegisteredListeners()) {
			oldStates = new HashMap<>(_states);
		}
		boolean changed = !_states.isEmpty();
		_states.clear();
		for (Object obj : newSelection) {
			changed |= internalSetSelected(obj, true);
		}
		setLastSelected(lead);
		if (changed) {
			if (hasListeners()) {
				fireSelectionChanged(oldSelection);
			}
			if (_listeners.hasRegisteredListeners()) {
				_listeners.notifyListeners(new StateChanged<>(this, oldStates));
			}
		}
	}

	@Override
	public void clear() {
		setSelection(Collections.emptySet());
	}

	/**
	 * Adds the given {@link TreeSelectionListener} from the list of listeners.
	 * 
	 * @param listener
	 *        the listener to add.
	 * @return Whether the given listener was not registered before (newly registered).
	 */
	public boolean addTreeSelectionListener(TreeSelectionListener<N> listener) {
		return _listeners.addListener(listener);
	}

	/**
	 * Removes the given {@link TreeSelectionListener} from the list of listeners.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the listener was registered before removal (something changed).
	 */
	public boolean removeTreeSelectionListener(TreeSelectionListener<N> listener) {
		return _listeners.removeListener(listener);
	}

	/**
	 * A view to the stored mapping of nodes to selection state.
	 */
	public Map<N, TriState> getStates() {
		return _statesView;
	}

	private boolean internalSelect(TLTreeModel<N> model, N node, boolean selected) {
		assertPartOfModel(node);

		TriState currentState = _states.get(node);
		if (currentState != null) {
			switch (currentState) {
				case INDETERMINATE: {
					TriState newState = selected ? TriState.SELECTED : TriState.NOT_SELECTED;
					_states.put(node, newState);
					dropChildStates(model, node);
					propagateUp(model, node, newState, TriState.INDETERMINATE);
					break;
				}

				case NOT_SELECTED: {
					if (selected) {
						_states.put(node, TriState.SELECTED);
						dropChildStates(model, node);
						propagateUp(model, node, TriState.SELECTED, TriState.NOT_SELECTED);
					} else {
						// already not selected
						return false;
					}
					break;
				}
				case SELECTED: {
					if (selected) {
						// already selected
						return false;
					} else {
						_states.put(node, TriState.NOT_SELECTED);
						dropChildStates(model, node);
						propagateUp(model, node, TriState.NOT_SELECTED, TriState.SELECTED);
					}
					break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + currentState);
			}
		} else {
			// find current selection state of node.
			TriState state = ancestorState(model, _states, node);
			switch (state) {
				case INDETERMINATE:
					throw new IllegalStateException();
				case NOT_SELECTED:
					if (selected) {
						_states.put(node, TriState.SELECTED);
						propagateUp(model, node, TriState.SELECTED, TriState.NOT_SELECTED);
					} else {
						// already (inherited) not selected
						return false;
					}
					break;
				case SELECTED:
					if (selected) {
						// already (inherited) selected
						return false;
					} else {
						_states.put(node, TriState.NOT_SELECTED);
						propagateUp(model, node, TriState.NOT_SELECTED, TriState.SELECTED);
					}
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + state);
			}
		}
		return true;
	}

	private void assertPartOfModel(N node) {
		if (!model().containsNode(node)) {
			throw new IllegalArgumentException("Not part of the model: " + node);
		}
	}

	/**
	 * Computes the state for the given node.
	 *
	 * @param node
	 *        The node to get state for.
	 */
	public final TriState getState(N node) {
		return getState(_states, node);
	}

	TriState getState(Map<N, TriState> states, N node) {
		assertPartOfModel(node);
		TriState ownState = states.get(node);
		if (ownState != null) {
			return ownState;
		}
		return ancestorState(model(), states, node);

	}

	private static <N> TriState ancestorState(TLTreeModel<N> model, Map<N, TriState> states, N node) {
		N tmp = node;
		while (true) {
			tmp = model.getParent(tmp);
			if (tmp == null) {
				// Root reached
				return TriState.NOT_SELECTED;
			}
			TriState ancestorState = states.get(tmp);
			if (ancestorState != null) {
				return ancestorState;
			}
		}
	}

	private void propagateUp(TLTreeModel<N> model, N node, TriState newNodeState, TriState oldNodeState) {
		N parent = model.getParent(node);
		if (parent == null) {
			// node is root
			if (newNodeState == TriState.NOT_SELECTED) {
				_states.remove(node);
			}
			return;
		}
		TriState parentState = _states.get(parent);
		if (parentState == null) {
			/* No cached parents state. Some node deep in the tree was modified. */
			/* When there is no entry for the parent, the child hat gotten its state from the parent
			 * of the parent, therefore the parent has the same state as the node. */
			TriState oldParentState = oldNodeState;
			if (model.getChildren(parent).size() == 1 && newNodeState != TriState.INDETERMINATE) {
				/* The given node is the only child of the parent. Therefore this element can carry
				 * the new state. */
				_states.remove(node);
				_states.put(parent, newNodeState);
				propagateUp(model, parent, newNodeState, oldParentState);
			} else {
				setSiblingState(model, parent, node, oldNodeState);
				_states.put(parent, TriState.INDETERMINATE);
				propagateUp(model, parent, TriState.INDETERMINATE, oldParentState);
			}
			return;
		}

		switch (newNodeState) {
			case INDETERMINATE: {
				switch (parentState) {
					case INDETERMINATE:
						// nothing to do here.
						break;
					case NOT_SELECTED:
						setSiblingState(model, parent, node, oldNodeState);
						_states.put(parent, TriState.INDETERMINATE);
						propagateUp(model, parent, TriState.INDETERMINATE, TriState.NOT_SELECTED);
						break;
					case SELECTED:
						setSiblingState(model, parent, node, oldNodeState);
						_states.put(parent, TriState.INDETERMINATE);
						propagateUp(model, parent, TriState.INDETERMINATE, TriState.SELECTED);
						break;
				}
				break;
			}
			case NOT_SELECTED: {
				switch (parentState) {
					case INDETERMINATE:
						boolean allUnSelected = true;
						for (N child : model.getChildren(parent)) {
							if (_states.get(child) != TriState.NOT_SELECTED) {
								allUnSelected = false;
								break;
							}
						}
						if (allUnSelected) {
							_states.put(parent, TriState.NOT_SELECTED);
							dropChildStates(model, parent);
							propagateUp(model, parent, TriState.NOT_SELECTED, TriState.INDETERMINATE);
						}
						break;
					case NOT_SELECTED:
						// parent is also completely unselected. Remove state for child.
						_states.remove(node);
						break;
					case SELECTED:
						boolean anySibling = setSiblingState(model, parent, node, TriState.SELECTED);
						TriState newParentState = anySibling ? TriState.INDETERMINATE : TriState.NOT_SELECTED;
						_states.put(parent, newParentState);
						propagateUp(model, parent, newParentState, TriState.SELECTED);
						break;
				}
				break;
			}
			case SELECTED: {
				switch (parentState) {
					case INDETERMINATE:
						boolean allSelected = true;
						for (N child : model.getChildren(parent)) {
							if (_states.get(child) != TriState.SELECTED) {
								allSelected = false;
								break;
							}
						}
						if (allSelected) {
							_states.put(parent, TriState.SELECTED);
							dropChildStates(model, parent);
							propagateUp(model, parent, TriState.SELECTED, TriState.INDETERMINATE);
						}
						break;
					case NOT_SELECTED:
						boolean anySibling = setSiblingState(model, parent, node, TriState.NOT_SELECTED);
						TriState newParentState = anySibling ? TriState.INDETERMINATE : TriState.SELECTED;
						_states.put(parent, newParentState);
						propagateUp(model, parent, newParentState, TriState.NOT_SELECTED);
						break;
					case SELECTED:
						// parent is also completely selected. Remove state for child.
						_states.remove(node);
						break;
				}
				break;
			}
		}
	}

	private boolean setSiblingState(TLTreeModel<N> model, N parent, N node, TriState oldNodeState) {
		assert oldNodeState != TriState.INDETERMINATE;
		boolean anySibling = false;
		for (N child : model.getChildren(parent)) {
			if (child == node) {
				continue;
			}
			anySibling = true;
			TriState formerState = _states.put(child, oldNodeState);
			if (formerState != null) {
				String message =
					" Sibling state must only be set, when the state of the parent switches from " + TriState.SELECTED
							+ " or " + TriState.NOT_SELECTED
							+ ". In such case no sibling of the changed child must have an cache entry: Node: " + node
							+ ", parent: " + parent + ", old node state: " + oldNodeState + ", sibling: " + child
							+ ", sibling state: " + formerState;
				throw new IllegalStateException(message);
			}
		}
		return anySibling;
	}

	private void dropChildStates(TLTreeModel<N> model, N parent) {
		for (N child : model.getChildren(parent)) {
			TriState state = _states.remove(child);
			if (state == null) {
				// No stored state for child.
				continue;
			}
			dropChildStates(model, child);
		}
	}

	/**
	 * Calculates all nodes which have state {@link TriState#SELECTED}.
	 * 
	 * @see #calculateAllSelected()
	 */
	public Set<N> calculateSelected() {
		return _states.entrySet()
			.stream()
			.filter(e -> TriState.SELECTED == e.getValue())
			.map(Map.Entry::getKey)
			.collect(Collectors.toSet());
	}

	/**
	 * Calculates all selected nodes, i.e. all nodes for which {@link #getState(Object)} returns
	 * {@link TriState#SELECTED}.
	 * 
	 * @see #calculateSelected()
	 */
	public Set<N> calculateAllSelected() {
		TLTreeModel<N> model = model();
		N root = model.getRoot();
		TriState rootState = _states.get(root);
		if (rootState == null) {
			// No entry for root => no entry at all.
			return Collections.emptySet();
		}
		Set<N> selection = new HashSet<>();
		addToSelection(model, root, selection);
		return selection;
	}

	private void addToSelection(TLTreeModel<N> model, N node, Set<N> selection) {
		TriState cachedState = _states.get(node);
		switch (cachedState) {
			case INDETERMINATE:
				for (N child : model.getChildren(node)) {
					addToSelection(model, child, selection);
				}
				break;
			case NOT_SELECTED:
				// no selected ignore complete subtree.
				break;
			case SELECTED:
				addRecursive(model, node, selection);
				break;
		}

	}

	private void addRecursive(TLTreeModel<N> model, N node, Set<N> selection) {
		selection.add(node);
		for (N child : model.getChildren(node)) {
			addRecursive(model, child, selection);
		}
	}

}

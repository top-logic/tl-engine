/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Keeps the {@link DefaultTreeUINodeModel} a {@link ReactTreeControl} displays in sync with the
 * model behind it: changes of the displayed objects arrive through a {@link ModelScope}, changes of
 * the input the tree is built from through the {@link ViewChannel}s it reads.
 *
 * <p>
 * An object change reconciles the tree in place. The child list of every node whose children were
 * computed already is computed again: a node whose object is still in that list is kept, with the
 * subtrees the user opened below it and everything the display holds on it, and moved where the
 * list now has it; an object that appeared gets a node at its place; a node whose object is gone is
 * removed. A node nobody opened is left alone - its children are computed when somebody opens it,
 * and computing them to answer a change nobody sees is wasted work.
 * </p>
 *
 * <p>
 * Which nodes are reconciled follows the change. A deleted object loses its node, and the node it
 * hung in is reconciled. An updated object reconciles the children of its own node and the child
 * list of the node it hangs in: an object taken out of a composition is not deleted, it is only no
 * longer a child, and nothing but the child list of the node it hung in says so - and that node is
 * reported as changed by nobody, since the change was made on the object. Where the tree says what
 * holds an object, the node of what holds it now is reconciled as well, so an object that moved
 * appears where it went. A created object of an observed type reconciles the whole computed tree,
 * because the function computing the children is opaque: where the new object appears is not known
 * here.
 * </p>
 *
 * <p>
 * An input change is a different tree: the root object is computed from the new input and the model
 * is built anew. The subtrees that were open are opened again wherever the new tree holds their
 * objects.
 * </p>
 *
 * <p>
 * Every change the tree goes through is announced to the
 * {@link #addStructureListener(Runnable) structure listeners}, which is what a display holding on
 * to the nodes - a selection, above all - needs to express itself on the tree as it is now.
 * </p>
 *
 * <p>
 * Observation begins with {@link #attach(ModelScope)} and is stopped by {@link #detach()}, which
 * the control calls while it is displayed and when it stops being displayed. Nothing is observed in
 * between, so an observation that begins again reconciles the tree: what the objects in it went
 * through while nobody followed them is unknown, and so is the input, which may name another root
 * by now.
 * </p>
 */
public class ObservableTreeModel implements ModelListener, ViewChannel.ChannelListener, TreeModelListener {

	private final ReactTreeControl _treeControl;

	private final Function<Object[], Object> _rootFunction;

	private final TreeBuilder<DefaultTreeUINode> _builder;

	/** What holds an object in the tree, {@code null} where the tree does not say. */
	private final Function<Object, Object> _parentFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private DefaultTreeUINodeModel _treeModel;

	/** What to run when the tree changed, see {@link #addStructureListener(Runnable)}. */
	private final List<Runnable> _structureListeners = new ArrayList<>();

	/** The objects a listener is registered for, by their identity. */
	private Map<ObjectKey, TLObject> _observed = new HashMap<>();

	private ModelScope _modelScope;

	private boolean _attached;

	/** Whether the observation was stopped and has not begun again. */
	private boolean _suspended;

	/**
	 * Whether the tree is currently being brought up to date, so that the events of its own changes
	 * report nothing this class does not know already.
	 */
	private boolean _updating;

	/**
	 * Creates a new {@link ObservableTreeModel}.
	 *
	 * @param treeControl
	 *        The tree control displaying the given model.
	 * @param treeModel
	 *        The tree model the control was built with.
	 * @param rootFunction
	 *        Computes the root business object from the input channel values.
	 * @param builder
	 *        Computes the children of a node, and creates the nodes of the tree.
	 * @param parentFunction
	 *        What holds a business object in the tree, {@code null} where the tree does not say. It
	 *        is what tells where an object that moved went, see {@link NodeLocator#byParents(Function)}.
	 * @param observedTypes
	 *        Types whose creates reconcile the tree, empty for a tree that expects none.
	 * @param inputChannels
	 *        The channels whose values the root function is called with.
	 */
	public ObservableTreeModel(ReactTreeControl treeControl, DefaultTreeUINodeModel treeModel,
			Function<Object[], Object> rootFunction,
			TreeBuilder<DefaultTreeUINode> builder,
			Function<Object, Object> parentFunction,
			Set<TLStructuredType> observedTypes,
			List<ViewChannel> inputChannels) {
		_treeControl = treeControl;
		_treeModel = treeModel;
		_rootFunction = rootFunction;
		_observedTypes = observedTypes;
		_builder = builder;
		_parentFunction = parentFunction;
		_inputChannels = inputChannels;
	}

	/**
	 * The tree model displayed now, which an input change replaces with another one.
	 */
	public DefaultTreeUINodeModel getTreeModel() {
		return _treeModel;
	}

	/**
	 * Registers what to run whenever the tree changed: a reconcile that took nodes out or put nodes
	 * in, and a rebuild, which replaces the tree with another one.
	 *
	 * <p>
	 * The listener runs after the display was told about the change, so the tree it sees is the one
	 * that is shown.
	 * </p>
	 *
	 * @param listener
	 *        What to run, see {@link #removeStructureListener(Runnable)}.
	 */
	public void addStructureListener(Runnable listener) {
		_structureListeners.add(listener);
	}

	/**
	 * Drops a listener registered by {@link #addStructureListener(Runnable)}.
	 *
	 * @param listener
	 *        The listener to drop.
	 */
	public void removeStructureListener(Runnable listener) {
		_structureListeners.remove(listener);
	}

	/**
	 * Announces a change of the tree to the {@link #addStructureListener(Runnable) listeners}.
	 */
	private void notifyStructureChanged() {
		// A listener may register or drop another one, so what is notified is the set of listeners
		// the change was announced to.
		for (Runnable listener : new ArrayList<>(_structureListeners)) {
			listener.run();
		}
	}

	/**
	 * Begins observing on the given {@link ModelScope}, and takes up what happened before.
	 *
	 * <p>
	 * Called while the tree is displayed and undone by {@link #detach()} when it stops being
	 * displayed. Idempotent, so an attach/detach cycle registers the listeners exactly once each
	 * time.
	 * </p>
	 *
	 * @param scope
	 *        The scope the model listeners are registered on.
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_modelScope = scope;

		_treeModel.addTreeModelListener(this);
		syncObjectListeners();
		registerTypeListeners();
		registerChannelListeners();
		catchUp();
	}

	/**
	 * Removes all listeners and releases references.
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		_suspended = true;

		_treeModel.removeTreeModelListener(this);
		deregisterObjectListeners();
		deregisterTypeListeners();
		deregisterChannelListeners();

		_modelScope = null;
	}

	/**
	 * Takes up what happened while nobody was observing.
	 *
	 * <p>
	 * The input is read in any case: a channel written between the construction of this observation
	 * and its beginning - a channel bound to the URL taking up the value a deep link carries, for
	 * instance - reached no listener, so the root the tree was built from may already be the wrong
	 * one.
	 * </p>
	 *
	 * <p>
	 * An observation that resumes - one that was stopped by {@link #detach()} - reconciles the tree
	 * on top of that, because every change of the displayed objects passed unnoticed while the
	 * display was suspended. An observation beginning for the first time needs none: the tree was
	 * just built from the objects as they are.
	 * </p>
	 */
	private void catchUp() {
		boolean resumed = _suspended;
		_suspended = false;

		if (!rootUpToDate()) {
			reEvaluateTree();
			return;
		}
		if (!resumed) {
			return;
		}
		boolean changed;
		boolean before = beginUpdate();
		try {
			changed = reconcileSubtree(_treeModel.getRoot());
		} finally {
			endUpdate(before);
		}
		if (changed) {
			syncObjectListeners();
			_treeControl.updateVisibleState();
			notifyStructureChanged();
		}
	}

	/**
	 * Whether the tree is built from the root object the input yields now.
	 */
	private boolean rootUpToDate() {
		Object root = _rootFunction.apply(readChannelValues());
		return Objects.equals(root, _treeModel.getRoot().getBusinessObject());
	}

	// --- TreeModelListener ---

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		if (_modelScope == null || _updating) {
			return;
		}
		switch (evt.getType()) {
			case TreeModelEvent.AFTER_EXPAND: {
				if (evt.getNode() instanceof DefaultTreeUINode node) {
					// Opening a node computes its children, which are observed like every other
					// displayed object.
					node.getChildren();
				}
				syncObjectListeners();
				break;
			}
			case TreeModelEvent.AFTER_NODE_ADD:
			case TreeModelEvent.AFTER_NODE_REMOVE:
			case TreeModelEvent.AFTER_STRUCTURE_CHANGE: {
				syncObjectListeners();
				break;
			}
			default:
				break;
		}
	}

	// --- ModelListener ---

	@Override
	public void notifyChange(ModelChangeEvent event) {
		Set<DefaultTreeUINode> toReconcile = new LinkedHashSet<>();
		boolean changed;

		boolean before = beginUpdate();
		try {
			changed = removeDeleted(event, toReconcile);
			changed |= invalidateUpdated(event, toReconcile);

			if (hasRelevantCreates(event)) {
				// Where a created object appears is not known here, so the whole computed tree is
				// checked.
				changed |= reconcileSubtree(_treeModel.getRoot());
			} else {
				for (DefaultTreeUINode node : toReconcile) {
					changed |= reconcileChildren(node);
				}
			}
		} finally {
			endUpdate(before);
		}

		if (changed) {
			syncObjectListeners();
			_treeControl.updateVisibleState();
			notifyStructureChanged();
		}
	}

	/**
	 * Drops the nodes of the objects the given event reports as deleted, and collects the nodes
	 * they hung in.
	 *
	 * @return Whether the tree changed.
	 */
	private boolean removeDeleted(ModelChangeEvent event, Set<DefaultTreeUINode> toReconcile) {
		boolean changed = false;
		for (TLObject deleted : observedObjects(event.getDeleted())) {
			DefaultTreeUINode node = TreeNodes.findComputedNode(_treeModel.getRoot(), deleted);
			if (node == null) {
				continue;
			}
			DefaultTreeUINode parent = node.getParent();
			if (parent == null) {
				// The root itself is gone. The tree is rebuilt when the input names another root.
				continue;
			}
			int index = parent.getChildren().indexOf(node);
			if (index >= 0) {
				parent.removeChild(index);
				changed = true;
			}
			toReconcile.add(parent);
		}
		return changed;
	}

	/**
	 * Drops what the nodes of the objects the given event reports as updated display, and collects
	 * those nodes, the nodes they hang in, and the nodes holding them now.
	 *
	 * @return Whether the display of any node changed.
	 */
	private boolean invalidateUpdated(ModelChangeEvent event, Set<DefaultTreeUINode> toReconcile) {
		boolean changed = false;
		for (TLObject updated : observedObjects(event.getUpdated())) {
			if (!updated.tValid()) {
				// What happened to an object that is gone is told by the report of its deletion,
				// and nothing can be computed over it any more.
				continue;
			}
			DefaultTreeUINode node = TreeNodes.findComputedNode(_treeModel.getRoot(), updated);
			if (node == null) {
				continue;
			}
			_treeControl.invalidateNodeControl(node);
			toReconcile.add(node);

			DefaultTreeUINode parent = node.getParent();
			if (parent != null) {
				// The change may have taken the object out of the list it hung in, or moved it
				// inside that list - the list of the node it hangs in is the only thing that says
				// so, and nobody reports that node as changed.
				toReconcile.add(parent);
			}
			collectNewParent(updated, toReconcile);
			changed = true;
		}
		return changed;
	}

	/**
	 * Collects the node of what holds the given object now, so that an object that moved appears
	 * where it went.
	 */
	private void collectNewParent(TLObject updated, Set<DefaultTreeUINode> toReconcile) {
		if (_parentFunction == null) {
			// Without a function saying what holds an object, where it went is unknown; the node it
			// came from loses it, and the place it went to is computed when somebody opens it.
			return;
		}
		Object parentObject = _parentFunction.apply(updated);
		if (parentObject == null) {
			return;
		}
		DefaultTreeUINode parentNode = TreeNodes.findComputedNode(_treeModel.getRoot(), parentObject);
		if (parentNode != null) {
			toReconcile.add(parentNode);
		}
	}

	/**
	 * The objects of the given report that are displayed, and therefore observed.
	 */
	private List<TLObject> observedObjects(Stream<? extends TLObject> objects) {
		List<TLObject> result = new ArrayList<>();
		objects.forEach(object -> {
			ObjectKey key = key(object);
			if (key != null && _observed.containsKey(key)) {
				result.add(object);
			}
		});
		return result;
	}

	private boolean hasRelevantCreates(ModelChangeEvent event) {
		if (_observedTypes.isEmpty()) {
			return false;
		}
		for (TLStructuredType type : _observedTypes) {
			if (event.getCreated(type).findAny().isPresent()) {
				return true;
			}
		}
		return false;
	}

	// --- ChannelListener ---

	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		reEvaluateTree();
	}

	// --- Reconciliation ---

	/**
	 * Starts an update of the tree, whose own events report nothing that is not known here.
	 *
	 * @return The state to hand to {@link #endUpdate(boolean)} when the update is done.
	 */
	private boolean beginUpdate() {
		boolean before = _updating;
		_updating = true;
		return before;
	}

	/**
	 * Ends an update started by {@link #beginUpdate()}.
	 *
	 * @param before
	 *        What {@link #beginUpdate()} returned.
	 */
	private void endUpdate(boolean before) {
		_updating = before;
	}

	/**
	 * Reconciles the given node and every node below it whose children were computed already.
	 *
	 * @return Whether the tree changed.
	 */
	private boolean reconcileSubtree(DefaultTreeUINode node) {
		if (!node.isInitialized()) {
			return false;
		}
		boolean changed = reconcileChildren(node);
		for (DefaultTreeUINode child : new ArrayList<>(node.getChildren())) {
			changed |= reconcileSubtree(child);
		}
		return changed;
	}

	/**
	 * Brings the children of the given node in line with the child list computed for it now, keeping
	 * the nodes of the objects that are still in that list.
	 *
	 * @return Whether the children changed.
	 */
	private boolean reconcileChildren(DefaultTreeUINode node) {
		if (!node.isAlive() || !node.isInitialized()) {
			return false;
		}
		List<Object> expected = childObjects(node);
		Set<Object> expectedObjects = new HashSet<>(expected);
		boolean changed = false;

		// The nodes of the objects that are gone.
		for (int n = node.getChildCount() - 1; n >= 0; n--) {
			if (!expectedObjects.contains(node.getChildAt(n).getBusinessObject())) {
				node.removeChild(n);
				changed = true;
			}
		}

		// The nodes that stay, where the child list has them now, and the objects that appeared.
		for (int n = 0, cnt = expected.size(); n < cnt; n++) {
			Object businessObject = expected.get(n);
			int current = indexOfChild(node, businessObject, n);
			if (current < 0) {
				node.createChild(IndexPosition.before(n), businessObject);
				changed = true;
			} else if (current != n) {
				node.getChildAt(current).moveTo(node, n);
				changed = true;
			}
		}

		// A child list holding an object once cannot leave two nodes for it behind.
		while (node.getChildCount() > expected.size()) {
			node.removeChild(expected.size());
			changed = true;
		}
		return changed;
	}

	/**
	 * The index of the child of the given node standing for the given business object, searched
	 * from the given index on.
	 *
	 * @return The index of that child, {@code -1} where there is none.
	 */
	private static int indexOfChild(DefaultTreeUINode node, Object businessObject, int fromIndex) {
		List<DefaultTreeUINode> children = node.getChildren();
		for (int n = fromIndex, cnt = children.size(); n < cnt; n++) {
			if (Objects.equals(children.get(n).getBusinessObject(), businessObject)) {
				return n;
			}
		}
		return -1;
	}

	/**
	 * The business objects the child list computed for the given node holds.
	 *
	 * @implNote The nodes the {@link TreeBuilder} creates here carry those objects but are not part
	 *           of the tree: a node that stays is the one the tree has, keeping everything the
	 *           display holds on it.
	 */
	private List<Object> childObjects(DefaultTreeUINode node) {
		List<DefaultTreeUINode> children = _builder.createChildList(node);
		List<Object> result = new ArrayList<>(children.size());
		for (DefaultTreeUINode child : children) {
			result.add(child.getBusinessObject());
		}
		return result;
	}

	/**
	 * Builds the tree anew from the root object the input names now, opening the subtrees that were
	 * open again.
	 */
	private void reEvaluateTree() {
		Set<Object> expanded = TreeNodes.collectExpanded(_treeModel.getRoot());

		_treeModel.removeTreeModelListener(this);
		deregisterObjectListeners();

		Object newRoot = _rootFunction.apply(readChannelValues());
		_treeModel = new DefaultTreeUINodeModel(_builder, newRoot);
		_treeModel.addTreeModelListener(this);
		boolean before = beginUpdate();
		try {
			TreeNodes.restoreExpansion(_treeModel.getRoot(), expanded);
		} finally {
			endUpdate(before);
		}

		_treeControl.setTreeModel(_treeModel);
		syncObjectListeners();
		notifyStructureChanged();
	}

	// --- Listener registration ---

	/**
	 * Registers a listener for every object the tree displays now, and drops the ones registered for
	 * objects it does not display any more.
	 */
	private void syncObjectListeners() {
		if (_modelScope == null) {
			return;
		}
		Map<ObjectKey, TLObject> displayed = new HashMap<>();
		collectObjects(_treeModel.getRoot(), displayed);

		for (Map.Entry<ObjectKey, TLObject> entry : _observed.entrySet()) {
			if (!displayed.containsKey(entry.getKey())) {
				_modelScope.removeModelListener(entry.getValue(), this);
			}
		}
		for (Map.Entry<ObjectKey, TLObject> entry : displayed.entrySet()) {
			if (!_observed.containsKey(entry.getKey())) {
				_modelScope.addModelListener(entry.getValue(), this);
			}
		}
		_observed = displayed;
	}

	/**
	 * Collects the objects of the given node and of every node below it whose children were computed
	 * already.
	 */
	private static void collectObjects(DefaultTreeUINode node, Map<ObjectKey, TLObject> objects) {
		ObjectKey key = key(node.getBusinessObject());
		if (key != null) {
			objects.put(key, (TLObject) node.getBusinessObject());
		}
		if (!node.isInitialized()) {
			return;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			collectObjects(child, objects);
		}
	}

	private void deregisterObjectListeners() {
		if (_modelScope != null) {
			for (TLObject object : _observed.values()) {
				_modelScope.removeModelListener(object, this);
			}
		}
		_observed = new HashMap<>();
	}

	private void registerTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.addModelListener(type, this);
		}
	}

	private void deregisterTypeListeners() {
		for (TLStructuredType type : _observedTypes) {
			_modelScope.removeModelListener(type, this);
		}
	}

	private void registerChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.addListener(this);
		}
	}

	private void deregisterChannelListeners() {
		for (ViewChannel channel : _inputChannels) {
			channel.removeListener(this);
		}
	}

	// --- Utility ---

	private Object[] readChannelValues() {
		Object[] values = new Object[_inputChannels.size()];
		for (int n = 0; n < _inputChannels.size(); n++) {
			values[n] = _inputChannels.get(n).get();
		}
		return values;
	}

	/**
	 * The identity of the given object, {@code null} where it has none: it is no
	 * {@link TLObject}, or one that lives only in the display holding it and whose changes nobody
	 * is notified of.
	 */
	private static ObjectKey key(Object object) {
		return object instanceof TLObject model ? model.tId() : null;
	}

}

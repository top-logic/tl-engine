/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Companion to a {@link DefaultTreeUINodeModel} + {@link ReactTreeControl} that observes
 * persistent object changes via {@link ModelScope} and input changes via {@link ViewChannel},
 * translating them into tree model updates.
 *
 * <p>
 * This class lives in the view module and bridges the gap between the TopLogic model layer
 * and the model-free React control layer. The wrapped {@link DefaultTreeUINodeModel} and the
 * {@link ReactTreeControl} remain unchanged.
 * </p>
 *
 * <h3>Two trigger paths:</h3>
 * <ol>
 * <li><b>Channel change</b> (new input data): Re-evaluates the root function with the new input,
 * builds a new {@link DefaultTreeUINodeModel} and calls
 * {@link ReactTreeControl#setTreeModel(com.top_logic.layout.tree.model.TreeUIModel)} on the control,
 * and re-registers object listeners.</li>
 * <li><b>ModelScope event</b> (object changes): Removes nodes whose business objects were deleted
 * directly from the tree model, invalidates individual node controls for updates via
 * {@link ReactTreeControl#invalidateNode(Object)}, and rebuilds the full tree for creates of
 * observed types.</li>
 * </ol>
 *
 * <h3>Lifecycle:</h3>
 * <ul>
 * <li>{@link #attach(ModelScope)} - registers listeners (called on first render)</li>
 * <li>{@link #detach()} - removes all listeners (called on cleanup)</li>
 * </ul>
 */
public class ObservableTreeModel implements ModelListener, ViewChannel.ChannelListener {

	private final ReactTreeControl _treeControl;

	private final Function<Object[], Object> _rootFunction;

	private final TreeBuilder<DefaultTreeUINode> _builder;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private DefaultTreeUINodeModel _treeModel;

	private Set<ObjectKey> _observedKeys = new HashSet<>();

	private ModelScope _modelScope;

	private boolean _attached;

	/**
	 * Creates a new {@link ObservableTreeModel}.
	 *
	 * @param treeControl
	 *        The React tree control to update when changes occur.
	 * @param treeModel
	 *        The initial tree model.
	 * @param rootFunction
	 *        Function that takes channel values and returns the root business object. Used for
	 *        re-evaluation on channel change and create detection.
	 * @param builder
	 *        The {@link TreeBuilder} used to rebuild the tree model.
	 * @param observedTypes
	 *        Types to observe for create events. Empty means no create detection.
	 * @param inputChannels
	 *        The input channels whose values are passed to the root function.
	 */
	public ObservableTreeModel(ReactTreeControl treeControl, DefaultTreeUINodeModel treeModel,
			Function<Object[], Object> rootFunction,
			TreeBuilder<DefaultTreeUINode> builder,
			Set<TLStructuredType> observedTypes,
			List<ViewChannel> inputChannels) {
		_treeControl = treeControl;
		_treeModel = treeModel;
		_rootFunction = rootFunction;
		_builder = builder;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
	}

	/**
	 * Registers listeners on the given {@link ModelScope} and input channels.
	 *
	 * <p>
	 * Called lazily on first render (via {@code addBeforeWriteAction}).
	 * </p>
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_modelScope = scope;

		registerObjectListeners();
		registerTypeListeners();
		registerChannelListeners();
	}

	/**
	 * Removes all listeners and releases references.
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;

		deregisterObjectListeners();
		deregisterTypeListeners();
		deregisterChannelListeners();

		_modelScope = null;
		_observedKeys.clear();
	}

	// --- ModelListener ---

	@Override
	public void notifyChange(ModelChangeEvent event) {
		handleDeletes(event);
		handleUpdates(event);
		handleCreates(event);
	}

	private void handleDeletes(ModelChangeEvent event) {
		List<TLObject> toRemove = new ArrayList<>();
		event.getDeleted().forEach(obj -> {
			ObjectKey key = key(obj);
			if (key != null && _observedKeys.contains(key)) {
				toRemove.add(obj);
				_observedKeys.remove(key);
				_modelScope.removeModelListener(obj, this);
			}
		});
		if (toRemove.isEmpty()) {
			return;
		}
		// Find and remove nodes for the deleted business objects.
		boolean anyRemoved = false;
		for (TLObject deleted : toRemove) {
			DefaultTreeUINode node = findNode(_treeModel.getRoot(), deleted);
			if (node != null) {
				DefaultTreeUINode parent = node.getParent();
				if (parent != null) {
					int index = parent.getChildren().indexOf(node);
					if (index >= 0) {
						parent.removeChild(index);
						anyRemoved = true;
					}
				}
			}
		}
		if (anyRemoved) {
			// Structure changed: node list is different, rebuild visible state.
			_treeControl.invalidateAll();
		}
	}

	private void handleUpdates(ModelChangeEvent event) {
		event.getUpdated().forEach(obj -> {
			ObjectKey key = key(obj);
			if (key != null && _observedKeys.contains(key)) {
				// Find the tree node for this business object and invalidate only its control.
				DefaultTreeUINode node = findNode(_treeModel.getRoot(), obj);
				if (node != null) {
					_treeControl.invalidateNode(node);
				}
			}
		});
	}

	private void handleCreates(ModelChangeEvent event) {
		if (hasRelevantCreates(event)) {
			reEvaluateTree();
		}
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

	// --- Re-evaluation ---

	private void reEvaluateTree() {
		deregisterObjectListeners();
		Object[] channelValues = readChannelValues();
		Object newRoot = _rootFunction.apply(channelValues);
		_treeModel = new DefaultTreeUINodeModel(_builder, newRoot);
		_treeControl.setTreeModel(_treeModel);
		registerObjectListeners();
	}

	// --- Node search ---

	/**
	 * Searches the initialized subtree rooted at {@code node} for the node whose business object
	 * equals the given {@code businessObject}.
	 *
	 * <p>
	 * Only visits nodes whose children have been initialized; does not trigger lazy loading.
	 * </p>
	 */
	private DefaultTreeUINode findNode(DefaultTreeUINode node, Object businessObject) {
		if (businessObject.equals(node.getBusinessObject())) {
			return node;
		}
		if (!_treeModel.childrenInitialized(node)) {
			return null;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			DefaultTreeUINode found = findNode(child, businessObject);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	// --- Listener registration helpers ---

	private void registerObjectListeners() {
		_observedKeys.clear();
		walkInitializedNodes(_treeModel.getRoot());
	}

	private void walkInitializedNodes(DefaultTreeUINode node) {
		Object bo = node.getBusinessObject();
		if (bo instanceof TLObject tlObj) {
			ObjectKey key = key(tlObj);
			if (key != null) {
				_observedKeys.add(key);
				_modelScope.addModelListener(tlObj, this);
			}
		}
		if (!_treeModel.childrenInitialized(node)) {
			return;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			walkInitializedNodes(child);
		}
	}

	private void deregisterObjectListeners() {
		if (_modelScope == null) {
			_observedKeys.clear();
			return;
		}
		// Walk the already-initialized portion of the tree to remove listeners.
		// We avoid calling getRoot() if possible by checking if we have any keys registered.
		if (!_observedKeys.isEmpty()) {
			deregisterObjectListenersRecursive(_treeModel.getRoot());
		}
		_observedKeys.clear();
	}

	private void deregisterObjectListenersRecursive(DefaultTreeUINode node) {
		Object bo = node.getBusinessObject();
		if (bo instanceof TLObject tlObj) {
			_modelScope.removeModelListener(tlObj, this);
		}
		if (!_treeModel.childrenInitialized(node)) {
			return;
		}
		for (DefaultTreeUINode child : node.getChildren()) {
			deregisterObjectListenersRecursive(child);
		}
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
		for (int i = 0; i < _inputChannels.size(); i++) {
			values[i] = _inputChannels.get(i).get();
		}
		return values;
	}

	private static ObjectKey key(TLObject obj) {
		KnowledgeItem item = obj.tHandle();
		return item != null ? item.tId() : null;
	}

}

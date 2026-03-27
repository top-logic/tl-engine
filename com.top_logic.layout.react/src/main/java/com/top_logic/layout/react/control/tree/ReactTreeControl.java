/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Server-side React control that renders a tree with lazy-loaded children.
 *
 * <p>
 * The tree is flattened into a list of visible nodes, each annotated with its depth. Node content
 * is delegated to child {@link ReactControl}s created by a {@link ReactControlProvider}. Expansion,
 * collapse, and selection are handled server-side via commands.
 * </p>
 */
public class ReactTreeControl extends ReactControl {

	// -- State keys --

	/** @see #buildFullState() */
	private static final String NODES = "nodes";

	/** @see #setSelectionMode(String) */
	private static final String SELECTION_MODE = "selectionMode";

	/** @see #setDragEnabled(boolean) */
	private static final String DRAG_ENABLED = "dragEnabled";

	/** @see #setDropEnabled(boolean) */
	private static final String DROP_ENABLED = "dropEnabled";

	/** @see #handleDragOver(Map) */
	private static final String DROP_INDICATOR_NODE_ID = "dropIndicatorNodeId";

	/** @see #handleDragOver(Map) */
	private static final String DROP_INDICATOR_POSITION = "dropIndicatorPosition";

	/** @see #openContextMenu(List, Consumer, int, int) */
	private static final String CONTEXT_MENU = "contextMenu";

	/** @see #openContextMenu(List, Consumer, int, int) */
	private static final String CONTEXT_MENU_X = "contextMenuX";

	/** @see #openContextMenu(List, Consumer, int, int) */
	private static final String CONTEXT_MENU_Y = "contextMenuY";

	// -- Node state keys (used in {@link #addNodeState}) --

	/** Unique node identifier. */
	private static final String NODE_ID = "id";

	/** Nesting depth (0 for top-level visible nodes). */
	private static final String NODE_DEPTH = "depth";

	/** Whether the node has children and can be expanded. */
	private static final String NODE_EXPANDABLE = "expandable";

	/** Whether the node is currently expanded. */
	private static final String NODE_EXPANDED = "expanded";

	/** Whether the node is a leaf (no children). */
	private static final String NODE_LEAF = "leaf";

	/** Whether the node is currently loading children. */
	private static final String NODE_LOADING = "loading";

	/** Whether the node is selected. */
	private static final String NODE_SELECTED = "selected";

	/** The child {@link ReactControl} rendering the node content. */
	private static final String NODE_CONTENT = "content";

	// -- Nested interfaces --

	/**
	 * Provider for opening context menus on tree nodes.
	 */
	@FunctionalInterface
	public interface ContextMenuProvider {
		/**
		 * Opens a context menu for the given node at the specified coordinates.
		 *
		 * @param tree
		 *        The tree control.
		 * @param node
		 *        The node that was right-clicked.
		 * @param x
		 *        The client X coordinate.
		 * @param y
		 *        The client Y coordinate.
		 */
		void openContextMenu(ReactTreeControl tree, Object node, int x, int y);
	}

	// -- Fields --

	private TreeUIModel<Object> _treeModel;

	@SuppressWarnings("rawtypes")
	private SelectionModel _selectionModel;

	private final ReactControlProvider _contentProvider;

	private String _selectionMode = "single";

	private boolean _dragEnabled;

	private boolean _dropEnabled;

	private ContextMenuProvider _contextMenuProvider;

	private List<TreeDropTarget> _dropTargets = new ArrayList<>();

	/** The node ID currently showing a drop indicator, or null. */
	private String _dropIndicatorNodeId;

	/** The current drop position indicator. */
	private String _dropIndicatorPosition;

	/** The handler for the currently open context menu, or {@code null}. */
	private Consumer<String> _contextMenuActionHandler;

	/** Index into the flat visible node list of the last anchor-setting click, or -1. */
	private int _selectionAnchor = -1;

	/** Whether the last anchor-setting action was an add or remove. */
	private boolean _anchorAdded = true;

	/** Cache of content controls for visible nodes. Keyed by node object. */
	private final Map<Object, ReactControl> _nodeControlCache = new LinkedHashMap<>();

	/**
	 * Creates a new {@link ReactTreeControl}.
	 *
	 * @param treeModel
	 *        The tree model providing structure and expansion state.
	 * @param selectionModel
	 *        The selection model.
	 * @param contentProvider
	 *        Provider for creating node content controls.
	 */
	@SuppressWarnings("unchecked")
	public ReactTreeControl(ReactContext context, TreeUIModel<?> treeModel, SelectionModel<?> selectionModel,
			ReactControlProvider contentProvider) {
		super(context, null, "TLTreeView");
		_treeModel = (TreeUIModel<Object>) treeModel;
		_selectionModel = selectionModel;
		_contentProvider = contentProvider;

		setSelectionMode(_selectionMode);
		setDragEnabled(false);
		setDropEnabled(false);
		buildFullState();
	}

	/**
	 * Sets the selection mode.
	 *
	 * @param mode
	 *        One of {@code "single"}, {@code "multi"}.
	 */
	public void setSelectionMode(String mode) {
		_selectionMode = mode;
		putState(SELECTION_MODE, mode);
	}

	/**
	 * Replaces the tree model and rebuilds the control state.
	 *
	 * @param treeModel
	 *        The new tree model.
	 */
	@SuppressWarnings("unchecked")
	public void setTreeModel(TreeUIModel<?> treeModel) {
		_treeModel = (TreeUIModel<Object>) treeModel;
		_nodeControlCache.clear();
		buildFullState();
	}

	/**
	 * Replaces the selection model and rebuilds the control state.
	 *
	 * @param selectionModel
	 *        The new selection model.
	 */
	public void setSelectionModel(SelectionModel<?> selectionModel) {
		_selectionModel = selectionModel;
		buildFullState();
	}

	/**
	 * Enables or disables drag from tree nodes.
	 */
	public void setDragEnabled(boolean enabled) {
		_dragEnabled = enabled;
		putState(DRAG_ENABLED, Boolean.valueOf(enabled));
	}

	/**
	 * Enables or disables drop onto tree nodes.
	 */
	public void setDropEnabled(boolean enabled) {
		_dropEnabled = enabled;
		putState(DROP_ENABLED, Boolean.valueOf(enabled));
	}

	/**
	 * Sets the context menu provider.
	 */
	public void setContextMenuProvider(ContextMenuProvider provider) {
		_contextMenuProvider = provider;
	}

	/**
	 * Opens a coordinate-positioned context menu on the tree.
	 *
	 * <p>
	 * Pushes the menu items, position, and open flag as state so that the client-side tree
	 * component renders the context menu. When the user selects an item, the
	 * {@code contextMenuAction} command is dispatched and the given {@code actionHandler} is
	 * called with the selected item ID.
	 * </p>
	 *
	 * @param items
	 *        The menu items as list of maps. Each map should have at least {@code "id"} and
	 *        {@code "label"}. Optional keys: {@code "icon"}, {@code "disabled"}.
	 * @param actionHandler
	 *        Called with the selected item ID.
	 * @param x
	 *        The client X coordinate.
	 * @param y
	 *        The client Y coordinate.
	 */
	public void openContextMenu(List<Map<String, Object>> items, Consumer<String> actionHandler, int x, int y) {
		_contextMenuActionHandler = actionHandler;
		putState(CONTEXT_MENU, items);
		putState(CONTEXT_MENU_X, Integer.valueOf(x));
		putState(CONTEXT_MENU_Y, Integer.valueOf(y));
	}

	/**
	 * Closes the context menu.
	 */
	public void closeContextMenu() {
		_contextMenuActionHandler = null;
		putState(CONTEXT_MENU, null);
	}

	/**
	 * Adds a drop target to this tree.
	 */
	public void addDropTarget(TreeDropTarget target) {
		_dropTargets.add(target);
	}

	/**
	 * Removes the cached content control for the given node.
	 *
	 * <p>
	 * The next {@link #updateVisibleState()} call will recreate the control with current data.
	 * All other cached controls remain untouched.
	 * </p>
	 *
	 * @param node
	 *        The tree node whose content control should be invalidated.
	 */
	public void invalidateNodeControl(Object node) {
		ReactControl control = _nodeControlCache.remove(node);
		if (control != null) {
			control.cleanupTree();
		}
	}

	/**
	 * Rebuilds the visible node state from the current tree model.
	 *
	 * <p>
	 * Reuses cached content controls where available. Controls for nodes that are no longer
	 * visible (e.g. deleted or collapsed) are automatically removed from the cache. Controls
	 * that were previously invalidated via {@link #invalidateNodeControl(Object)} are recreated.
	 * </p>
	 */
	public void updateVisibleState() {
		buildFullState();
	}

	// -- Rendering --

	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		if (_nodeControlCache.isEmpty()) {
			// After a detach/reattach cycle, _nodeControlCache was cleared by cleanupNodeControls()
			// but _reactState still has stale node references. Rebuild the cache and state from the
			// tree model. At this point _sseQueue is still null, so putState() stores locally
			// without sending a PatchEvent.
			buildFullState();
		}
	}

	// -- State building --

	private void buildFullState() {
		// Collect the new set of visible nodes and build their state.
		List<Map<String, Object>> nodeStates = new ArrayList<>();
		Set<Object> newVisibleNodes = new HashSet<>();
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			newVisibleNodes.add(root);
			addNodeState(nodeStates, root, 0);
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			addChildStates(nodeStates, root, _treeModel.isRootVisible() ? 1 : 0, newVisibleNodes);
		}

		// Remove controls for nodes that are no longer visible.
		List<Object> toRemove = new ArrayList<>();
		for (Object cachedNode : _nodeControlCache.keySet()) {
			if (!newVisibleNodes.contains(cachedNode)) {
				toRemove.add(cachedNode);
			}
		}
		for (Object node : toRemove) {
			ReactControl control = _nodeControlCache.remove(node);
			if (control != null) {
				control.cleanupTree();
			}
		}

		putState(NODES, nodeStates);
	}

	private void addChildStates(List<Map<String, Object>> nodeStates, Object parent, int depth,
			Set<Object> visibleNodes) {
		for (Object child : _treeModel.getChildren(parent)) {
			visibleNodes.add(child);
			addNodeState(nodeStates, child, depth);
			if (_treeModel.isExpanded(child)) {
				addChildStates(nodeStates, child, depth + 1, visibleNodes);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addNodeState(List<Map<String, Object>> nodeStates, Object node, int depth) {
		boolean hasChildren = !_treeModel.isLeaf(node);
		boolean expanded = hasChildren && _treeModel.isExpanded(node);

		ReactControl contentControl = getOrCreateNodeControl(node);

		Map<String, Object> nodeState = new LinkedHashMap<>();
		nodeState.put(NODE_ID, getNodeId(node));
		nodeState.put(NODE_DEPTH, Integer.valueOf(depth));
		nodeState.put(NODE_EXPANDABLE, Boolean.valueOf(hasChildren));
		nodeState.put(NODE_EXPANDED, Boolean.valueOf(expanded));
		nodeState.put(NODE_LEAF, Boolean.valueOf(_treeModel.isLeaf(node)));
		nodeState.put(NODE_LOADING, Boolean.FALSE);
		nodeState.put(NODE_SELECTED, Boolean.valueOf(_selectionModel.isSelected(node)));
		nodeState.put(NODE_CONTENT, contentControl);

		nodeStates.add(nodeState);
	}

	private ReactControl getOrCreateNodeControl(Object node) {
		ReactControl control = _nodeControlCache.get(node);
		if (control == null) {
			control = _contentProvider.createControl(getReactContext(), node);
			_nodeControlCache.put(node, control);
			registerChildControl(control);
		}
		return control;
	}

	private String getNodeId(Object node) {
		return String.valueOf(System.identityHashCode(node));
	}

	private Object findNodeById(String nodeId) {
		for (Object node : _nodeControlCache.keySet()) {
			if (getNodeId(node).equals(nodeId)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Returns the index of the given node in the current flat visible node list.
	 */
	private int findNodeIndex(Object node) {
		int index = 0;
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			if (root == node) {
				return 0;
			}
			index++;
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			int result = findNodeIndexRecursive(root, node, index);
			if (result >= 0) {
				return result;
			}
		}
		return -1;
	}

	private int findNodeIndexRecursive(Object parent, Object target, int currentIndex) {
		for (Object child : _treeModel.getChildren(parent)) {
			if (child == target) {
				return currentIndex;
			}
			currentIndex++;
			if (_treeModel.isExpanded(child)) {
				int result = findNodeIndexRecursive(child, target, currentIndex);
				if (result >= 0) {
					return result;
				}
				currentIndex += countVisibleDescendants(child);
			}
		}
		return -1;
	}

	private int countVisibleDescendants(Object node) {
		int count = 0;
		for (Object child : _treeModel.getChildren(node)) {
			count++;
			if (_treeModel.isExpanded(child)) {
				count += countVisibleDescendants(child);
			}
		}
		return count;
	}

	/**
	 * Collects the flat list of visible nodes in order.
	 */
	private List<Object> collectVisibleNodes() {
		List<Object> result = new ArrayList<>();
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			result.add(root);
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			collectVisibleNodesRecursive(root, result);
		}
		return result;
	}

	private void collectVisibleNodesRecursive(Object parent, List<Object> result) {
		for (Object child : _treeModel.getChildren(parent)) {
			result.add(child);
			if (_treeModel.isExpanded(child)) {
				collectVisibleNodesRecursive(child, result);
			}
		}
	}

	private void cleanupNodeControls() {
		for (ReactControl control : _nodeControlCache.values()) {
			control.cleanupTree();
		}
		_nodeControlCache.clear();
	}

	@Override
	protected void cleanupChildren() {
		cleanupNodeControls();
	}

	// -- Commands --

	/**
	 * Expands a tree node, loading children and prefetching grandchildren.
	 */
	@ReactCommand("expand")
	void handleExpand(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		Object node = findNodeById(nodeId);
		if (node != null && !_treeModel.isLeaf(node) && !_treeModel.isExpanded(node)) {
			_treeModel.setExpanded(node, true);

			// Prefetch grandchildren: trigger getChildren on each child.
			for (Object child : _treeModel.getChildren(node)) {
				if (!_treeModel.isLeaf(child)) {
					// Access children to trigger lazy loading.
					_treeModel.getChildren(child);
				}
			}

			buildFullState();
		}
	}

	/**
	 * Collapses a tree node, removing its children from the visible list.
	 */
	@ReactCommand("collapse")
	void handleCollapse(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		Object node = findNodeById(nodeId);
		if (node != null && _treeModel.isExpanded(node)) {
			_treeModel.setExpanded(node, false);
			buildFullState();
		}
	}

	/**
	 * Selects a tree node. Supports single, toggle (Ctrl), and range (Shift) selection.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("select")
	void handleSelect(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
		boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

		Object node = findNodeById(nodeId);
		if (node == null || !_selectionModel.isSelectable(node)) {
			return;
		}

		if ("multi".equals(_selectionMode)) {
			if (shiftKey && _selectionAnchor >= 0) {
				// Range selection.
				List<Object> visibleNodes = collectVisibleNodes();
				int clickedIndex = visibleNodes.indexOf(node);
				if (clickedIndex >= 0) {
					int from = Math.min(_selectionAnchor, clickedIndex);
					int to = Math.max(_selectionAnchor, clickedIndex);
					for (int i = from; i <= to; i++) {
						Object rangeNode = visibleNodes.get(i);
						if (_selectionModel.isSelectable(rangeNode)) {
							_selectionModel.setSelected(rangeNode, _anchorAdded);
						}
					}
				}
			} else if (ctrlKey) {
				// Toggle selection.
				boolean wasSelected = _selectionModel.isSelected(node);
				_selectionModel.setSelected(node, !wasSelected);
				_anchorAdded = !wasSelected;
				List<Object> visibleNodes = collectVisibleNodes();
				_selectionAnchor = visibleNodes.indexOf(node);
			} else {
				// Single click in multi mode: replace selection.
				_selectionModel.clear();
				_selectionModel.setSelected(node, true);
				_anchorAdded = true;
				List<Object> visibleNodes = collectVisibleNodes();
				_selectionAnchor = visibleNodes.indexOf(node);
			}
		} else {
			// Single select mode.
			_selectionModel.clear();
			_selectionModel.setSelected(node, true);
		}

		buildFullState();
	}

	/**
	 * Opens a context menu at the given coordinates for a tree node.
	 */
	@ReactCommand("contextMenu")
	void handleContextMenu(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		Object node = findNodeById(nodeId);
		if (node != null && _contextMenuProvider != null) {
			int x = ((Number) arguments.get("x")).intValue();
			int y = ((Number) arguments.get("y")).intValue();
			_contextMenuProvider.openContextMenu(this, node, x, y);
		}
	}

	/**
	 * Evaluates whether a drop is allowed at the given position and updates the drop indicator
	 * state.
	 */
	@ReactCommand("dragOver")
	void handleDragOver(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		String position = (String) arguments.get("position");
		Object node = findNodeById(nodeId);
		if (node != null) {
			_dropIndicatorNodeId = nodeId;
			_dropIndicatorPosition = position;
			putState(DROP_INDICATOR_NODE_ID, nodeId);
			putState(DROP_INDICATOR_POSITION, position);
		}
	}

	/**
	 * Handles a drop event on a tree node. Clears drop indicators and processes the drop.
	 */
	@ReactCommand("drop")
	void handleDrop(Map<String, Object> arguments) {
		String nodeId = (String) arguments.get("nodeId");
		String position = (String) arguments.get("position");
		Object node = findNodeById(nodeId);

		// Clear drop indicators.
		_dropIndicatorNodeId = null;
		_dropIndicatorPosition = null;
		putState(DROP_INDICATOR_NODE_ID, null);
		putState(DROP_INDICATOR_POSITION, null);

		if (node != null) {
			// TODO: Integrate with full DnD framework (DndData, TreeDropTarget.handleDrop).
			// For now, the drop event is received but not processed. Full integration
			// requires a TreeData adapter for the React tree.
		}
	}

	/**
	 * Clears the drop indicator state when a drag operation ends.
	 */
	@ReactCommand("dragEnd")
	void handleDragEnd() {
		_dropIndicatorNodeId = null;
		_dropIndicatorPosition = null;
		putState(DROP_INDICATOR_NODE_ID, null);
		putState(DROP_INDICATOR_POSITION, null);
	}

	/**
	 * Handles the selection of a context menu item.
	 */
	@ReactCommand("contextMenuAction")
	void handleContextMenuAction(Map<String, Object> arguments) {
		String itemId = (String) arguments.get("itemId");
		Consumer<String> handler = _contextMenuActionHandler;
		closeContextMenu();
		if (handler != null && itemId != null) {
			handler.accept(itemId);
		}
	}

	/**
	 * Handles the context menu being closed without selection.
	 */
	@ReactCommand("contextMenuClose")
	void handleContextMenuClose() {
		closeContextMenu();
	}
}

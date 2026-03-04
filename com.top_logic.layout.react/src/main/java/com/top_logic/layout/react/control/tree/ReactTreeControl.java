/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ReactControlProvider;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private static final String NODES = "nodes";

	private static final String SELECTION_MODE = "selectionMode";

	private static final String DRAG_ENABLED = "dragEnabled";

	private static final String DROP_ENABLED = "dropEnabled";

	private static final String DROP_INDICATOR_NODE_ID = "dropIndicatorNodeId";

	private static final String DROP_INDICATOR_POSITION = "dropIndicatorPosition";

	// -- Commands --

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ExpandCommand(),
		new CollapseCommand(),
		new SelectCommand(),
		new ContextMenuCommand(),
		new DragOverCommand(),
		new DropCommand(),
		new DragEndCommand());

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

	private final TreeUIModel<Object> _treeModel;

	@SuppressWarnings("rawtypes")
	private final SelectionModel _selectionModel;

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
	public ReactTreeControl(TreeUIModel<?> treeModel, SelectionModel<?> selectionModel,
			ReactControlProvider contentProvider) {
		super(null, "TLTreeView", COMMANDS);
		_treeModel = (TreeUIModel<Object>) treeModel;
		_selectionModel = selectionModel;
		_contentProvider = contentProvider;

		putState(SELECTION_MODE, _selectionMode);
		putState(DRAG_ENABLED, Boolean.FALSE);
		putState(DROP_ENABLED, Boolean.FALSE);
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
	 * Adds a drop target to this tree.
	 */
	public void addDropTarget(TreeDropTarget target) {
		_dropTargets.add(target);
	}

	// -- Rendering --

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (_nodeControlCache.isEmpty()) {
			// After a detach/reattach cycle, _nodeControlCache was cleared by cleanupNodeControls()
			// but _reactState still has stale node references. Rebuild the cache and state from the
			// tree model. At this point _sseQueue is still null, so putState() stores locally
			// without sending a PatchEvent.
			buildFullState();
		}
		super.internalWrite(context, out);
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
				unregisterChildControl(control);
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
		nodeState.put("id", getNodeId(node));
		nodeState.put("depth", Integer.valueOf(depth));
		nodeState.put("expandable", Boolean.valueOf(hasChildren));
		nodeState.put("expanded", Boolean.valueOf(expanded));
		nodeState.put("leaf", Boolean.valueOf(_treeModel.isLeaf(node)));
		nodeState.put("loading", Boolean.FALSE);
		nodeState.put("selected", Boolean.valueOf(_selectionModel.isSelected(node)));
		nodeState.put("content", contentControl);

		nodeStates.add(nodeState);
	}

	private ReactControl getOrCreateNodeControl(Object node) {
		ReactControl control = _nodeControlCache.get(node);
		if (control == null) {
			control = _contentProvider.createControl(node);
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
	static class ExpandCommand extends ControlCommand {

		private static final String COMMAND_NAME = "expand";

		ExpandCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			Object node = tree.findNodeById(nodeId);
			if (node != null && !tree._treeModel.isLeaf(node) && !tree._treeModel.isExpanded(node)) {
				tree._treeModel.setExpanded(node, true);

				// Prefetch grandchildren: trigger getChildren on each child.
				for (Object child : tree._treeModel.getChildren(node)) {
					if (!tree._treeModel.isLeaf(child)) {
						// Access children to trigger lazy loading.
						tree._treeModel.getChildren(child);
					}
				}

				tree.buildFullState();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.expand");
		}
	}

	/**
	 * Collapses a tree node, removing its children from the visible list.
	 */
	static class CollapseCommand extends ControlCommand {

		private static final String COMMAND_NAME = "collapse";

		CollapseCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			Object node = tree.findNodeById(nodeId);
			if (node != null && tree._treeModel.isExpanded(node)) {
				tree._treeModel.setExpanded(node, false);
				tree.buildFullState();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.collapse");
		}
	}

	/**
	 * Selects a tree node. Supports single, toggle (Ctrl), and range (Shift) selection.
	 */
	static class SelectCommand extends ControlCommand {

		private static final String COMMAND_NAME = "select";

		SelectCommand() {
			super(COMMAND_NAME);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
			boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

			Object node = tree.findNodeById(nodeId);
			if (node == null || !tree._selectionModel.isSelectable(node)) {
				return HandlerResult.DEFAULT_RESULT;
			}

			if ("multi".equals(tree._selectionMode)) {
				if (shiftKey && tree._selectionAnchor >= 0) {
					// Range selection.
					List<Object> visibleNodes = tree.collectVisibleNodes();
					int clickedIndex = visibleNodes.indexOf(node);
					if (clickedIndex >= 0) {
						int from = Math.min(tree._selectionAnchor, clickedIndex);
						int to = Math.max(tree._selectionAnchor, clickedIndex);
						for (int i = from; i <= to; i++) {
							Object rangeNode = visibleNodes.get(i);
							if (tree._selectionModel.isSelectable(rangeNode)) {
								tree._selectionModel.setSelected(rangeNode, tree._anchorAdded);
							}
						}
					}
				} else if (ctrlKey) {
					// Toggle selection.
					boolean wasSelected = tree._selectionModel.isSelected(node);
					tree._selectionModel.setSelected(node, !wasSelected);
					tree._anchorAdded = !wasSelected;
					List<Object> visibleNodes = tree.collectVisibleNodes();
					tree._selectionAnchor = visibleNodes.indexOf(node);
				} else {
					// Single click in multi mode: replace selection.
					tree._selectionModel.clear();
					tree._selectionModel.setSelected(node, true);
					tree._anchorAdded = true;
					List<Object> visibleNodes = tree.collectVisibleNodes();
					tree._selectionAnchor = visibleNodes.indexOf(node);
				}
			} else {
				// Single select mode.
				tree._selectionModel.clear();
				tree._selectionModel.setSelected(node, true);
			}

			tree.buildFullState();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.select");
		}
	}

	/**
	 * Opens a context menu at the given coordinates for a tree node.
	 */
	static class ContextMenuCommand extends ControlCommand {

		private static final String COMMAND_NAME = "contextMenu";

		ContextMenuCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			Object node = tree.findNodeById(nodeId);
			if (node != null && tree._contextMenuProvider != null) {
				int x = ((Number) arguments.get("x")).intValue();
				int y = ((Number) arguments.get("y")).intValue();
				tree._contextMenuProvider.openContextMenu(tree, node, x, y);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.contextMenu");
		}
	}

	/**
	 * Evaluates whether a drop is allowed at the given position and updates the drop indicator
	 * state.
	 */
	static class DragOverCommand extends ControlCommand {

		private static final String COMMAND_NAME = "dragOver";

		DragOverCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			String position = (String) arguments.get("position");
			Object node = tree.findNodeById(nodeId);
			if (node != null) {
				tree._dropIndicatorNodeId = nodeId;
				tree._dropIndicatorPosition = position;
				tree.putState(DROP_INDICATOR_NODE_ID, nodeId);
				tree.putState(DROP_INDICATOR_POSITION, position);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.dragOver");
		}
	}

	/**
	 * Handles a drop event on a tree node. Clears drop indicators and processes the drop.
	 */
	static class DropCommand extends ControlCommand {

		private static final String COMMAND_NAME = "drop";

		DropCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			String position = (String) arguments.get("position");
			Object node = tree.findNodeById(nodeId);

			// Clear drop indicators.
			tree._dropIndicatorNodeId = null;
			tree._dropIndicatorPosition = null;
			tree.putState(DROP_INDICATOR_NODE_ID, null);
			tree.putState(DROP_INDICATOR_POSITION, null);

			if (node != null) {
				// TODO: Integrate with full DnD framework (DndData, TreeDropTarget.handleDrop).
				// For now, the drop event is received but not processed. Full integration
				// requires a TreeData adapter for the React tree.
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.drop");
		}
	}

	/**
	 * Clears the drop indicator state when a drag operation ends.
	 */
	static class DragEndCommand extends ControlCommand {

		private static final String COMMAND_NAME = "dragEnd";

		DragEndCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			tree._dropIndicatorNodeId = null;
			tree._dropIndicatorPosition = null;
			tree.putState(DROP_INDICATOR_NODE_ID, null);
			tree.putState(DROP_INDICATOR_POSITION, null);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.tree.dragEnd");
		}
	}
}

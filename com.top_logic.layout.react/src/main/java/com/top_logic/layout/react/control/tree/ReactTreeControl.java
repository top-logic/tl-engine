/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMarker;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropPlace;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.table.SelectionMode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Server-side React control that renders a tree with lazy-loaded children.
 *
 * <p>
 * The tree is flattened into a list of visible nodes, each annotated with its depth. Node content
 * is delegated to child {@link ReactControl}s created by a {@link ReactControlProvider}, which
 * receives the business object a node stands for
 * ({@link TreeUIModel#getBusinessObject(Object)}), not the node itself. A node therefore displays
 * its object exactly as any other place displaying the same object does, down to being a link to
 * where the application shows it. Expansion, collapse, selection and activation are handled
 * server-side via commands.
 * </p>
 *
 * <p>
 * Nodes are dragged and dropped through the seam of {@link com.top_logic.layout.react.control.dnd}:
 * {@link #setDragSource(String, Predicate)} makes the nodes draggable as drags of a kind - a
 * selected node drags the whole selection -, {@link #setDropTarget(DropTarget)} accepts a drop of
 * the kinds its target accepts and applies it. The objects of a drag and the reference objects of a
 * drop are the business objects of the nodes.
 * </p>
 *
 * <p>
 * The tree resolves a node and a {@link DropZone zone} into the {@link DropLocation} of each
 * {@link DropMode}: a drop {@link DropMode#ONTO onto} a node in any of its zones is made onto that
 * node. An {@link DropMode#ORDERED insertion} in the upper part of a node inserts before it among
 * its siblings; in its middle part as the first child of the node; in its lower part as the first
 * child of an expanded node with children, and after the node among its siblings otherwise; beside
 * the nodes as the last top-level node. A drop on the {@link DropMode#CONTROL tree as a whole} is
 * one wherever it is made.
 * </p>
 */
public class ReactTreeControl extends ReactControl implements DragSourceControl {

	// -- Command names --

	/** Id of the command expanding a node, see {@link #handleExpand(ExpandNodeArguments)}. */
	public static final String EXPAND_COMMAND = "expand";

	/** Id of the command collapsing a node, see {@link #handleCollapse(CollapseNodeArguments)}. */
	public static final String COLLAPSE_COMMAND = "collapse";

	/** Id of the command a click on a node sends, see {@link #handleSelect(SelectNodeArguments)}. */
	public static final String SELECT_COMMAND = "select";

	/**
	 * Id of the command opening a node sends, see {@link #handleActivate(ActivateNodeArguments)}.
	 */
	public static final String ACTIVATE_COMMAND = "activate";

	/** @see #handleContextMenu(ContextMenuArguments) */
	private static final String CONTEXT_MENU_COMMAND = "contextMenu";

	/** @see #handleDrop(DropArguments) */
	private static final String CMD_DROP = DropSupport.CMD_DROP;

	/** @see #handleDropProbe(DropProbeArguments) */
	private static final String CMD_DROP_PROBE = DropSupport.CMD_DROP_PROBE;

	/** @see #handleDropObjects(DropObjectsArguments) */
	private static final String CMD_DROP_OBJECTS = DropSupport.CMD_DROP_OBJECTS;

	// -- State keys --

	/** State key of the list of the displayed nodes, in display order. */
	public static final String NODES = "nodes";

	/** @see #setSelectionMode(SelectionMode) */
	private static final String SELECTION_MODE = "selectionMode";

	/** @see DropSupport#DRAG_ENABLED */
	private static final String DRAG_ENABLED = DropSupport.DRAG_ENABLED;

	/** @see DropSupport#DRAG_KIND */
	private static final String DRAG_KIND = DropSupport.DRAG_KIND;

	/** @see DropSupport#DROP_VERDICTS */
	private static final String DROP_VERDICTS = DropSupport.DROP_VERDICTS;

	// -- Node state keys (used in {@link #addNodeState}) --

	/** Node state key of the id the client sends back with a gesture on that node. */
	public static final String NODE_ID = "id";

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

	/**
	 * Whether the node may be dragged, present while the nodes are
	 * {@link #setDragSource(String, Predicate) draggable} at all.
	 */
	private static final String NODE_DRAGGABLE = "draggable";

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

	/**
	 * Notified when a node is activated: opened by a double-click, or by {@code Enter} while it
	 * carries the keyboard focus.
	 */
	@FunctionalInterface
	public interface ActivationHandler {

		/**
		 * Called after the activated node became the tree's selection.
		 *
		 * @param node
		 *        The activated node, as the tree model holds it.
		 * @return The outcome reported to the client (and to a scripted replay).
		 */
		HandlerResult nodeActivated(Object node);
	}

	// -- Fields --

	private TreeUIModel<Object> _treeModel;

	@SuppressWarnings("rawtypes")
	private SelectionModel _selectionModel;

	private final ReactControlProvider _contentProvider;

	private SelectionMode _selectionMode = SelectionMode.SINGLE;

	/** Whether nodes may be dragged at all. */
	private boolean _dragEnabled;

	/** The kind of a drag of nodes, {@code null} for a drag without a kind. */
	private String _dragKind;

	/**
	 * Which business objects may be dragged while {@link #_dragEnabled} is set, {@code null} when
	 * every node may be.
	 */
	private Predicate<Object> _draggable;

	/** The drop protocol shared with every control accepting drops. */
	private final DropSupport _dropSupport = new DropSupport(this);

	private ContextMenuProvider _contextMenuProvider;

	/** What a node activation runs, {@code null} for a tree whose nodes cannot be opened. */
	private ActivationHandler _activationHandler;

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
	 *        Provider for creating node content controls. It is called with the business object a
	 *        node stands for, see {@link TreeUIModel#getBusinessObject(Object)}.
	 */
	@SuppressWarnings("unchecked")
	public ReactTreeControl(ReactContext context, TreeUIModel<?> treeModel, SelectionModel<?> selectionModel,
			ReactControlProvider contentProvider) {
		super(context, null, "TLTreeView");
		_treeModel = (TreeUIModel<Object>) treeModel;
		_selectionModel = selectionModel;
		_contentProvider = contentProvider;

		setSelectionMode(_selectionMode);
		putState(DRAG_ENABLED, Boolean.FALSE);
		refreshDropTarget();
		buildFullState();
	}

	/**
	 * Sets whether the user may select one node at a time, or any number of them.
	 *
	 * @param mode
	 *        The selection mode, {@link SelectionMode#SINGLE} by default.
	 */
	public void setSelectionMode(SelectionMode mode) {
		_selectionMode = mode;
		putState(SELECTION_MODE, mode.getExternalName());
	}

	/**
	 * Whether more than one node may be selected at a time.
	 */
	private boolean multiSelection() {
		return _selectionMode == SelectionMode.MULTI;
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
	 * Makes the nodes draggable as drags of the given kind.
	 *
	 * <p>
	 * Dragging a selected node drags the whole selection, an unselected node drags itself. What a
	 * receiving {@link DropTarget} gets are the business objects of the nodes; the kind is what it
	 * accepts the drop by.
	 * </p>
	 *
	 * <p>
	 * A node whose business object the given predicate refuses offers no drag, and a drag of a
	 * selection including such a node is refused as a whole (see
	 * {@link DragSourceControl#isDraggable(Object)}).
	 * </p>
	 *
	 * @param dragKind
	 *        The {@link #dragKind() kind} of a drag, {@code null} for a drag without a kind.
	 * @param draggable
	 *        Which business objects may be dragged, {@code null} for all of them. Asked whenever
	 *        nodes are rendered; when its answer changes for other reasons, call
	 *        {@link #refreshDragSource()}.
	 *
	 * @see #setDragEnabled(boolean)
	 */
	public void setDragSource(String dragKind, Predicate<Object> draggable) {
		Object update = beginUpdate();
		try {
			_dragKind = dragKind;
			_draggable = draggable;
			putState(DRAG_KIND, dragKind);
			setDragEnabled(true);
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Switches dragging of nodes on or off, keeping the kind and the predicate given to
	 * {@link #setDragSource(String, Predicate)}.
	 */
	public void setDragEnabled(boolean enabled) {
		Object update = beginUpdate();
		try {
			_dragEnabled = enabled;
			putState(DRAG_ENABLED, Boolean.valueOf(enabled));
			buildFullState();
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Asks the {@link #setDragSource(String, Predicate) draggable predicate} again for the displayed
	 * nodes, after its answer may have changed.
	 */
	public void refreshDragSource() {
		buildFullState();
	}

	/**
	 * Makes the tree accept a drop of the objects the given target accepts, and applies such a drop
	 * through it.
	 *
	 * @param dropTarget
	 *        What dropped objects are done with, or {@code null} to accept no drop again.
	 */
	public void setDropTarget(DropTarget dropTarget) {
		_dropSupport.setTarget(dropTarget);
		refreshDropTarget();
	}

	/**
	 * Announces the {@link DropTarget#acceptedKinds() accepted kinds} and the
	 * {@link DropTarget#dropModes() modes} of the drop operations to the client again, after the
	 * {@link #setDropTarget(DropTarget) drop target's} answers changed.
	 */
	public void refreshDropTarget() {
		Object update = beginUpdate();
		try {
			_dropSupport.targetState().forEach(this::putState);
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Sets the context menu provider.
	 */
	public void setContextMenuProvider(ContextMenuProvider provider) {
		_contextMenuProvider = provider;
	}

	/**
	 * Sets what a node activation runs, replacing any handler set before.
	 *
	 * <p>
	 * The handler is called with the activated node, after that node became the tree's selection.
	 * Without one, a double-click and {@code Enter} select the node and do nothing further.
	 * </p>
	 *
	 * @param handler
	 *        The handler to call, {@code null} to make the nodes unopenable again.
	 */
	public void setActivationHandler(ActivationHandler handler) {
		_activationHandler = handler;
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
			// tree model. State written while rendering is part of the rendered output, so
			// putState() stores locally without sending a PatchEvent.
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
		if (_dragEnabled) {
			nodeState.put(NODE_DRAGGABLE, Boolean.valueOf(isDraggable(_treeModel.getBusinessObject(node))));
		}

		nodeStates.add(nodeState);
	}

	private ReactControl getOrCreateNodeControl(Object node) {
		ReactControl control = _nodeControlCache.get(node);
		if (control == null) {
			control = _contentProvider.createControl(getReactContext(), _treeModel.getBusinessObject(node));
			_nodeControlCache.put(node, control);
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

	/**
	 * Also disposes the controls of nodes that are currently collapsed or scrolled out: only the
	 * rendered nodes are part of the state, the others are only reachable through the cache.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		cleanupNodeControls();
	}

	// -- Commands --

	/**
	 * Expands a tree node, loading children and prefetching grandchildren.
	 */
	@ReactCommandHandler(EXPAND_COMMAND)
	void handleExpand(ExpandNodeArguments args) {
		String nodeId = args.getNodeId();
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
	@ReactCommandHandler(COLLAPSE_COMMAND)
	void handleCollapse(CollapseNodeArguments args) {
		String nodeId = args.getNodeId();
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
	@ReactCommandHandler(SELECT_COMMAND)
	void handleSelect(SelectNodeArguments args) {
		String nodeId = args.getNodeId();
		boolean ctrlKey = args.isCtrlKey();
		boolean shiftKey = args.isShiftKey();

		Object node = findNodeById(nodeId);
		if (node == null || !_selectionModel.isSelectable(node)) {
			return;
		}

		if (multiSelection()) {
			if (shiftKey && _selectionAnchor >= 0) {
				// Range selection.
				List<Object> visibleNodes = collectVisibleNodes();
				int clickedIndex = visibleNodes.indexOf(node);
				if (clickedIndex >= 0) {
					int from = Math.min(_selectionAnchor, clickedIndex);
					int to = Math.max(_selectionAnchor, clickedIndex);
					List<Object> rangeNodes = new ArrayList<>();
					for (int i = from; i <= to; i++) {
						Object rangeNode = visibleNodes.get(i);
						if (_selectionModel.isSelectable(rangeNode)) {
							rangeNodes.add(rangeNode);
						}
					}
					// The whole range is applied in one step, so that a single SelectionEvent
					// carries it to everything following the selection.
					if (_anchorAdded) {
						_selectionModel.addToSelection(rangeNodes);
					} else {
						_selectionModel.removeFromSelection(rangeNodes);
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
				selectOnly(node);
			}
		} else {
			// Single select mode.
			selectOnly(node);
		}

		buildFullState();
	}

	/**
	 * Activates a tree node: the node becomes the selection, and what
	 * {@link #setActivationHandler(ActivationHandler)} registered runs with it.
	 *
	 * <p>
	 * This is what a double-click on the node and {@code Enter} on the focused node send. An id
	 * naming no displayed node activates nothing.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@ReactCommandHandler(ACTIVATE_COMMAND)
	HandlerResult handleActivate(ActivateNodeArguments args) {
		Object node = findNodeById(args.getNodeId());
		if (node == null || !_selectionModel.isSelectable(node)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		selectOnly(node);
		buildFullState();

		ActivationHandler handler = _activationHandler;
		if (handler == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return handler.nodeActivated(node);
	}

	/**
	 * Makes the given node the sole selection and the range anchor.
	 *
	 * <p>
	 * {@link SelectionModel#setSelection(Set)} replaces the selection in one step, so that a single
	 * {@link SelectionEvent} carries the new selection. Everything following the selection - a
	 * display, a command's executability, a channel the selection is written to - therefore moves
	 * straight from the former selection to this node.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private void selectOnly(Object node) {
		_selectionModel.setSelection(Set.of(node));
		_anchorAdded = true;
		_selectionAnchor = collectVisibleNodes().indexOf(node);
	}

	/**
	 * Opens a context menu at the given coordinates for a tree node.
	 */
	@ReactCommandHandler(CONTEXT_MENU_COMMAND)
	void handleContextMenu(ContextMenuArguments args) {
		String nodeId = args.getNodeId();
		Object node = findNodeById(nodeId);
		if (node != null && _contextMenuProvider != null) {
			int x = args.getX();
			int y = args.getY();
			_contextMenuProvider.openContextMenu(this, node, x, y);
		}
	}

	/**
	 * Records a drop in replay-stable form: a {@link #CMD_DROP} becomes a
	 * {@link #CMD_DROP_OBJECTS} naming the dragged objects and the location of the drop by their
	 * business identities, see {@link DropSupport#recordDrop(Map, DropPlace.Resolver)}.
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_DROP.equals(command) && arguments != null) {
			RecordedCommand recorded = _dropSupport.recordDrop(arguments, this::dropPlace);
			if (recorded != null) {
				return recorded;
			}
		}
		return super.recordCommand(command, arguments);
	}

	// -- Drag and drop --

	@Override
	public boolean isDragEnabled() {
		return _dragEnabled;
	}

	@Override
	public String dragKind() {
		return _dragKind;
	}

	@Override
	public boolean isDraggable(Object object) {
		return _dragEnabled && (_draggable == null || _draggable.test(object));
	}

	@Override
	public List<?> dragObjects(List<String> keys) {
		if (keys == null) {
			return List.of();
		}
		List<Object> result = new ArrayList<>(keys.size());
		for (String key : keys) {
			Object node = findNodeById(key);
			if (node != null) {
				result.add(_treeModel.getBusinessObject(node));
			}
		}
		return result;
	}

	/**
	 * The business objects of the selected nodes: the displayed ones in display order, followed by
	 * those of selected nodes that are currently hidden in a collapsed subtree.
	 */
	@Override
	public List<?> dragSelection() {
		Set<?> selection = _selectionModel.getSelection();
		Set<Object> nodes = new LinkedHashSet<>();
		for (Object node : collectVisibleNodes()) {
			if (selection.contains(node)) {
				nodes.add(node);
			}
		}
		nodes.addAll(selection);
		List<Object> result = new ArrayList<>(nodes.size());
		for (Object node : nodes) {
			result.add(_treeModel.getBusinessObject(node));
		}
		return result;
	}

	/**
	 * Applies a drop the client made on this tree.
	 *
	 * <p>
	 * The node the drop names is resolved into the place the drop was made at (see
	 * {@link #dropPlace(String, DropZone)}), everything else is
	 * {@link DropSupport#drop(DropArguments, DropPlace.Resolver) shared} with every control accepting
	 * drops.
	 * </p>
	 */
	@ReactCommandHandler(CMD_DROP)
	HandlerResult handleDrop(DropArguments args) {
		return _dropSupport.drop(args, this::dropPlace);
	}

	/**
	 * Answers whether a drop right where a drag hovers would be accepted, without applying it.
	 *
	 * <p>
	 * The verdict is added to {@link #DROP_VERDICTS} under the {@link DropProbeArguments#getProbe()
	 * probe's identifier}, see {@link DropSupport#probe(DropProbeArguments, DropPlace.Resolver)}. The
	 * probe is technical: it is neither recorded nor offered as an action, and it never fails, since
	 * a refusal is its answer.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_DROP_PROBE, technical = true)
	void handleDropProbe(DropProbeArguments args) {
		putState(DROP_VERDICTS, _dropSupport.probe(args, this::dropPlace));
	}

	/**
	 * Applies a drop of the objects named by their business identity - the replay-stable
	 * counterpart of {@link #handleDrop(DropArguments)}, which a recorded drop is captured as.
	 *
	 * @see DropSupport#dropObjects(DropObjectsArguments, Predicate)
	 */
	@ReactCommandHandler(CMD_DROP_OBJECTS)
	HandlerResult handleDropObjects(DropObjectsArguments args) {
		return _dropSupport.dropObjects(args, this::displaysLocation);
	}

	/**
	 * The place of a drop made in the given zone of the node with the given client-side id, or
	 * beside the nodes.
	 *
	 * @return The place, {@code null} if the id names a node this tree no longer displays.
	 *
	 * @see DropPlace.Resolver#place(String, DropZone)
	 */
	private DropPlace dropPlace(String nodeId, DropZone zone) {
		if (nodeId == null) {
			return new NodePlace(null, DropZone.NONE);
		}
		Object node = findNodeById(nodeId);
		if (node == null) {
			return null;
		}
		return new NodePlace(node, zone);
	}

	/**
	 * A place of a drop in this tree.
	 *
	 * <p>
	 * An insertion from the upper part of a node is marked before the node, from its middle part as
	 * a highlight of the node, from its lower part after the node, and beside the nodes as a
	 * highlight of the tree as a whole.
	 * </p>
	 */
	private final class NodePlace implements DropPlace {

		/** The node the drop was made on, {@code null} for a drop beside the nodes. */
		private final Object _node;

		/** The zone of the node the drop was made in, {@link DropZone#NONE} without a node. */
		private final DropZone _zone;

		NodePlace(Object node, DropZone zone) {
			_node = node;
			_zone = node == null ? DropZone.NONE : zone;
		}

		@Override
		public DropLocation location(DropMode mode) {
			switch (mode) {
				case CONTROL:
					return new DropLocation.Control();
				case ONTO:
					if (_zone == DropZone.NONE) {
						return null;
					}
					return new DropLocation.Onto(businessObject(_node));
				case ORDERED:
					return insertion();
			}
			throw new IllegalArgumentException("Unknown drop mode: " + mode);
		}

		private DropLocation.Insert insertion() {
			switch (_zone) {
				case UPPER:
					return new DropLocation.Insert(businessObject(parentOf(_node)), businessObject(_node));
				case MIDDLE:
					return new DropLocation.Insert(businessObject(_node), businessObject(firstChild(_node)));
				case LOWER: {
					Object firstChild = _treeModel.isExpanded(_node) ? firstChild(_node) : null;
					if (firstChild != null) {
						return new DropLocation.Insert(businessObject(_node), businessObject(firstChild));
					}
					return new DropLocation.Insert(businessObject(parentOf(_node)), businessObject(nextSibling(_node)));
				}
				case NONE:
					break;
			}
			Object root = _treeModel.getRoot();
			return new DropLocation.Insert(_treeModel.isRootVisible() ? null : businessObject(root), null);
		}

		@Override
		public DropMarker marker(DropLocation location) {
			if (location instanceof DropLocation.Onto) {
				return DropMarker.INTO;
			}
			if (location instanceof DropLocation.Insert) {
				switch (_zone) {
					case UPPER:
						return DropMarker.BEFORE;
					case MIDDLE:
						return DropMarker.INTO;
					case LOWER:
						return DropMarker.AFTER;
					case NONE:
						break;
				}
			}
			return DropMarker.CONTROL;
		}

		@Override
		public String markerKey(DropMarker marker) {
			return marker == DropMarker.CONTROL ? null : getNodeId(_node);
		}

	}

	/** The business object of the given node, {@code null} for no node. */
	private Object businessObject(Object node) {
		return node == null ? null : _treeModel.getBusinessObject(node);
	}

	/** The node holding the given one in its child list, {@code null} for the root. */
	private Object parentOf(Object node) {
		return _treeModel.getParent(node);
	}

	/**
	 * The first child of the given node in the model's child order, loading the children of a node
	 * that has not computed them yet; {@code null} for a node without children.
	 */
	private Object firstChild(Object node) {
		if (_treeModel.isLeaf(node)) {
			return null;
		}
		List<?> children = _treeModel.getChildren(node);
		return children.isEmpty() ? null : children.get(0);
	}

	/** The node following the given one in its parent's child list, {@code null} for the last one. */
	private Object nextSibling(Object node) {
		Object parent = parentOf(node);
		if (parent == null) {
			return null;
		}
		List<?> siblings = _treeModel.getChildren(parent);
		int index = siblings.indexOf(node);
		return index >= 0 && index + 1 < siblings.size() ? siblings.get(index + 1) : null;
	}

	/**
	 * Whether the reference objects of the given location are places of this tree: the target of a
	 * drop onto a node is the object of a node, the parent of an insertion is the object of a node
	 * - or the parent of the top-level nodes -, and the object an insertion is made before is a
	 * child of that parent.
	 */
	private boolean displaysLocation(DropLocation location) {
		if (location instanceof DropLocation.Onto onto) {
			return onto.target() == null || nodeOf(onto.target()) != null;
		}
		if (location instanceof DropLocation.Insert insert) {
			if (insert.parent() == null) {
				if (!_treeModel.isRootVisible()) {
					return false;
				}
				return insert.before() == null
					|| insert.before().equals(_treeModel.getBusinessObject(_treeModel.getRoot()));
			}
			Object parent = nodeOf(insert.parent());
			if (parent == null) {
				return false;
			}
			if (insert.before() == null) {
				return true;
			}
			if (_treeModel.isLeaf(parent)) {
				return false;
			}
			for (Object child : _treeModel.getChildren(parent)) {
				if (insert.before().equals(_treeModel.getBusinessObject(child))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * The node of the given business object among the nodes whose parents have computed their
	 * children, {@code null} if there is none.
	 */
	private Object nodeOf(Object businessObject) {
		return nodeOf(_treeModel.getRoot(), businessObject);
	}

	private Object nodeOf(Object node, Object businessObject) {
		if (businessObject.equals(_treeModel.getBusinessObject(node))) {
			return node;
		}
		if (_treeModel.isLeaf(node) || !_treeModel.childrenInitialized(node)) {
			return null;
		}
		for (Object child : _treeModel.getChildren(node)) {
			Object result = nodeOf(child, businessObject);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}

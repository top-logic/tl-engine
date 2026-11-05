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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropPlace;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropTreeNavigation;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.dnd.TreeDropPlace;
import com.top_logic.layout.react.control.selection.SelectByKeysArguments;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.table.SelectionMode;
import com.top_logic.util.error.TopLogicException;

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
 * {@link DropMode} by the rules of a {@link TreeDropPlace}, which it shares with every control
 * displaying a tree.
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
	public static final String CONTEXT_MENU_COMMAND = "contextMenu";

	/** @see #handleSelectByKey(SelectNodeByKeyArguments) */
	public static final String SELECT_BY_KEY_COMMAND = "selectByKey";

	/** @see #handleSelectByKeys(SelectByKeysArguments) */
	public static final String SELECT_BY_KEYS_COMMAND = "selectByKeys";

	/** @see #handleExpandByKey(ExpandNodeByKeyArguments) */
	public static final String EXPAND_BY_KEY_COMMAND = "expandByKey";

	/** @see #handleCollapseByKey(CollapseNodeByKeyArguments) */
	public static final String COLLAPSE_BY_KEY_COMMAND = "collapseByKey";

	/** @see #handleActivateByKey(ActivateNodeByKeyArguments) */
	public static final String ACTIVATE_BY_KEY_COMMAND = "activateByKey";

	/** @see #handleContextMenuByKey(ContextMenuByKeyArguments) */
	public static final String CONTEXT_MENU_BY_KEY_COMMAND = "contextMenuByKey";

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

	/**
	 * Key of the business identity of a node in the headless projection of the {@link #NODES}, see
	 * {@link #scriptingScalarState()}.
	 */
	public static final String NODE_KEY = ModelKeyArguments.KEY;

	// -- Command argument names --

	/** The argument naming the node of a gesture by its {@link #NODE_ID}. */
	private static final String ARG_NODE_ID = SelectNodeArguments.NODE_ID;

	/** @see SelectNodeArguments#isCtrlKey() */
	private static final String ARG_CTRL_KEY = SelectNodeArguments.CTRL_KEY;

	/** @see SelectNodeArguments#isShiftKey() */
	private static final String ARG_SHIFT_KEY = SelectNodeArguments.SHIFT_KEY;

	/** @see ContextMenuArguments#getX() */
	private static final String ARG_X = ContextMenuArguments.X;

	/** @see ContextMenuArguments#getY() */
	private static final String ARG_Y = ContextMenuArguments.Y;

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
		 * @throws TopLogicException
		 *         If the reaction fails in a way the user can act on; reported to the client (and
		 *         to a scripted replay).
		 */
		void nodeActivated(Object node);
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

	/** The nodes as the drop rules see them. */
	private final DropTreeNavigation<Object> _navigation = new Navigation();

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

	/**
	 * Adds an agent-facing {@link #NODES} projection to the headless state.
	 *
	 * <p>
	 * The regular {@link #NODES} state holds the content <em>controls</em> of the nodes, so it is
	 * left out of the projection, which omits control-bearing state; the content controls are
	 * projected as nodes of their own. Here each displayed node is projected in display order with
	 * its {@link #NODE_DEPTH}, {@link #NODE_EXPANDABLE}, {@link #NODE_EXPANDED},
	 * {@link #NODE_SELECTED} and the {@link #NODE_KEY business identity} of its object - the key an
	 * agent passes to {@link #SELECT_BY_KEY_COMMAND} and the other commands by key. A node whose
	 * object has no identity is projected with its {@link #NODE_ID} instead.
	 * </p>
	 */
	@Override
	public Map<String, Object> scriptingScalarState() {
		Map<String, Object> result = super.scriptingScalarState();
		List<Map<String, Object>> nodes = new ArrayList<>();
		Map<Object, Integer> depths = new HashMap<>();
		for (Object node : collectVisibleNodes()) {
			Object parent = _treeModel.getParent(node);
			Integer parentDepth = parent == null ? null : depths.get(parent);
			int depth = parentDepth == null ? 0 : parentDepth.intValue() + 1;
			depths.put(node, Integer.valueOf(depth));

			Map<String, Object> nodeState = new LinkedHashMap<>();
			ScriptingModelKey.putIdentity(nodeState, null, _treeModel.getBusinessObject(node), NODE_ID,
				getNodeId(node));
			boolean hasChildren = !_treeModel.isLeaf(node);
			nodeState.put(NODE_DEPTH, Integer.valueOf(depth));
			nodeState.put(NODE_EXPANDABLE, Boolean.valueOf(hasChildren));
			nodeState.put(NODE_EXPANDED, Boolean.valueOf(hasChildren && _treeModel.isExpanded(node)));
			nodeState.put(NODE_SELECTED, Boolean.valueOf(_selectionModel.isSelected(node)));
			nodes.add(nodeState);
		}
		result.put(NODES, nodes);
		return result;
	}

	private String getNodeId(Object node) {
		return String.valueOf(System.identityHashCode(node));
	}

	/**
	 * The displayed node whose business object is named by the given {@link ScriptingModelKey key},
	 * {@code null} if the key resolves to no object or no displayed node stands for it.
	 */
	private Object displayedNode(ModelName name) {
		Object target = ScriptingModelKey.locate(null, name);
		if (target == null) {
			return null;
		}
		for (Object node : collectVisibleNodes()) {
			if (target.equals(_treeModel.getBusinessObject(node))) {
				return node;
			}
		}
		return null;
	}

	/** The failure of a command naming by key no displayed node. */
	private static TopLogicException unresolved(ModelName name) {
		// A recorded node key that no longer designates a displayed node is an explicit failure
		// (replay reports success:false), never a silent no-op.
		return new TopLogicException(I18NConstants.ERROR_NODE_KEY_UNRESOLVED__KEY.fill(name));
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
		expand(findNodeById(args.getNodeId()));
	}

	/**
	 * Expands the displayed node whose business object is named by the given
	 * {@link ScriptingModelKey key} - the replay-stable counterpart of
	 * {@link #handleExpand(ExpandNodeArguments)}, which a recorded expansion is captured as.
	 */
	@ReactCommandHandler(EXPAND_BY_KEY_COMMAND)
	void handleExpandByKey(ExpandNodeByKeyArguments args) {
		Object node = displayedNode(args.getKey());
		if (node == null) {
			throw unresolved(args.getKey());
		}
		expand(node);
	}

	private void expand(Object node) {
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
		collapse(findNodeById(args.getNodeId()));
	}

	/**
	 * Collapses the displayed node whose business object is named by the given
	 * {@link ScriptingModelKey key} - the replay-stable counterpart of
	 * {@link #handleCollapse(CollapseNodeArguments)}, which a recorded collapse is captured as.
	 */
	@ReactCommandHandler(COLLAPSE_BY_KEY_COMMAND)
	void handleCollapseByKey(CollapseNodeByKeyArguments args) {
		Object node = displayedNode(args.getKey());
		if (node == null) {
			throw unresolved(args.getKey());
		}
		collapse(node);
	}

	private void collapse(Object node) {
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
		boolean ctrlKey = args.isCtrlKey();
		boolean shiftKey = args.isShiftKey();

		Object node = findNodeById(args.getNodeId());
		if (node == null || !_selectionModel.isSelectable(node)) {
			return;
		}

		Set<Object> selection = clickSelection(node, ctrlKey, shiftKey);
		if (multiSelection() && !(shiftKey && _selectionAnchor >= 0)) {
			// A click without a range sets the anchor - a toggle to the side it toggled the node to.
			_anchorAdded = selection.contains(node);
			_selectionAnchor = collectVisibleNodes().indexOf(node);
		} else if (!multiSelection()) {
			_anchorAdded = true;
			_selectionAnchor = collectVisibleNodes().indexOf(node);
		}
		// The whole change is applied in one step, so that a single SelectionEvent carries it to
		// everything following the selection.
		_selectionModel.setSelection(selection);

		buildFullState();
	}

	/**
	 * The nodes a click on the given node selects, without changing the selection.
	 *
	 * <p>
	 * In {@link SelectionMode#MULTI}, a click with {@code Shift} adds the selectable nodes from the
	 * range anchor up to the clicked one to the selection - or takes them out of it, when the click
	 * setting the anchor took its node out -, a click with {@code Ctrl} toggles the clicked node,
	 * and a plain click selects the clicked node only. In {@link SelectionMode#SINGLE}, a click
	 * selects the clicked node only.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private Set<Object> clickSelection(Object node, boolean ctrlKey, boolean shiftKey) {
		if (!multiSelection()) {
			return Set.of(node);
		}
		if (shiftKey && _selectionAnchor >= 0) {
			Set<Object> result = new LinkedHashSet<>(_selectionModel.getSelection());
			List<Object> visibleNodes = collectVisibleNodes();
			int clickedIndex = visibleNodes.indexOf(node);
			if (clickedIndex >= 0) {
				int from = Math.min(_selectionAnchor, clickedIndex);
				int to = Math.min(Math.max(_selectionAnchor, clickedIndex), visibleNodes.size() - 1);
				for (int i = from; i <= to; i++) {
					Object rangeNode = visibleNodes.get(i);
					if (_selectionModel.isSelectable(rangeNode)) {
						if (_anchorAdded) {
							result.add(rangeNode);
						} else {
							result.remove(rangeNode);
						}
					}
				}
			}
			return result;
		}
		if (ctrlKey) {
			Set<Object> result = new LinkedHashSet<>(_selectionModel.getSelection());
			if (!result.remove(node)) {
				result.add(node);
			}
			return result;
		}
		return Set.of(node);
	}

	/**
	 * Selects the displayed node whose business object is named by the given
	 * {@link ScriptingModelKey key} as the only one - the replay-stable counterpart of a plain
	 * click, {@link #handleSelect(SelectNodeArguments)}, which it is recorded as.
	 */
	@ReactCommandHandler(SELECT_BY_KEY_COMMAND)
	void handleSelectByKey(SelectNodeByKeyArguments args) {
		Object node = displayedNode(args.getKey());
		if (node == null) {
			throw unresolved(args.getKey());
		}
		if (_selectionModel.isSelectable(node)) {
			selectOnly(node);
			buildFullState();
		}
	}

	/**
	 * Sets the selection to the nodes whose business objects are named by the given
	 * {@link ScriptingModelKey keys} - the replay-stable form a click with {@code Ctrl} or
	 * {@code Shift} is recorded in, as the selection it results in.
	 *
	 * <p>
	 * A key may name a node hidden in a collapsed subtree, as long as its parent has computed its
	 * children. The first displayed selected node becomes the range anchor.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@ReactCommandHandler(SELECT_BY_KEYS_COMMAND)
	void handleSelectByKeys(SelectByKeysArguments args) {
		Set<Object> selection = new LinkedHashSet<>();
		List<ModelName> unresolved = new ArrayList<>();
		for (ModelName name : args.getKeys()) {
			Object target = ScriptingModelKey.locate(null, name);
			Object node = target == null ? null : nodeOf(target);
			if (node == null) {
				unresolved.add(name);
			} else {
				selection.add(node);
			}
		}
		if (!unresolved.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_NODE_KEYS_UNRESOLVED__KEYS.fill(unresolved));
		}
		_selectionModel.setSelection(selection);
		_anchorAdded = true;
		_selectionAnchor = -1;
		List<Object> visibleNodes = collectVisibleNodes();
		for (int n = 0, cnt = visibleNodes.size(); n < cnt; n++) {
			if (selection.contains(visibleNodes.get(n))) {
				_selectionAnchor = n;
				break;
			}
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
	@ReactCommandHandler(ACTIVATE_COMMAND)
	void handleActivate(ActivateNodeArguments args) {
		activate(findNodeById(args.getNodeId()));
	}

	/**
	 * Activates the displayed node whose business object is named by the given
	 * {@link ScriptingModelKey key} - the replay-stable counterpart of
	 * {@link #handleActivate(ActivateNodeArguments)}, which a recorded activation is captured as.
	 */
	@ReactCommandHandler(ACTIVATE_BY_KEY_COMMAND)
	void handleActivateByKey(ActivateNodeByKeyArguments args) {
		Object node = displayedNode(args.getKey());
		if (node == null) {
			throw unresolved(args.getKey());
		}
		activate(node);
	}

	@SuppressWarnings("unchecked")
	private void activate(Object node) {
		if (node == null || !_selectionModel.isSelectable(node)) {
			return;
		}
		selectOnly(node);
		buildFullState();

		ActivationHandler handler = _activationHandler;
		if (handler == null) {
			return;
		}
		handler.nodeActivated(node);
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
		openContextMenu(findNodeById(args.getNodeId()), args.getX(), args.getY());
	}

	/**
	 * Opens the context menu of the displayed node whose business object is named by the given
	 * {@link ScriptingModelKey key} - the replay-stable counterpart of
	 * {@link #handleContextMenu(ContextMenuArguments)}, which a recorded right-click is captured as.
	 */
	@ReactCommandHandler(CONTEXT_MENU_BY_KEY_COMMAND)
	void handleContextMenuByKey(ContextMenuByKeyArguments args) {
		Object node = displayedNode(args.getKey());
		if (node == null) {
			throw unresolved(args.getKey());
		}
		openContextMenu(node, args.getX(), args.getY());
	}

	private void openContextMenu(Object node, int x, int y) {
		if (node != null && _contextMenuProvider != null) {
			_contextMenuProvider.openContextMenu(this, node, x, y);
		}
	}

	/**
	 * Records a gesture on a node in replay-stable form, since the node ids the client sends are
	 * allocated per session.
	 *
	 * <p>
	 * A {@link #EXPAND_COMMAND}, {@link #COLLAPSE_COMMAND}, {@link #ACTIVATE_COMMAND} and
	 * {@link #CONTEXT_MENU_COMMAND} become their counterparts by key - {@link #EXPAND_BY_KEY_COMMAND},
	 * {@link #COLLAPSE_BY_KEY_COMMAND}, {@link #ACTIVATE_BY_KEY_COMMAND},
	 * {@link #CONTEXT_MENU_BY_KEY_COMMAND} - naming the node by the business identity of its object,
	 * and so does a plain click, {@link #SELECT_COMMAND}, as {@link #SELECT_BY_KEY_COMMAND}. A click
	 * with {@code Ctrl} or {@code Shift} depends on the range anchor and on the order of the
	 * displayed nodes, so it is recorded as the selection it results in, a
	 * {@link #SELECT_BY_KEYS_COMMAND} of the identities of the selected nodes; consecutive ones
	 * coalesce into one step. A drop becomes a {@link #CMD_DROP_OBJECTS} naming the dragged objects
	 * and the location of the drop by their business identities, see
	 * {@link DropSupport#recordDrop(Map, DropPlace.Resolver)}. A gesture on a node whose object has
	 * no identity is recorded verbatim.
	 * </p>
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (arguments != null) {
			RecordedCommand recorded = recordByKey(command, arguments);
			if (recorded != null) {
				return recorded;
			}
		}
		return super.recordCommand(command, arguments);
	}

	private RecordedCommand recordByKey(String command, Map<String, Object> arguments) {
		if (CMD_DROP.equals(command)) {
			return _dropSupport.recordDrop(arguments, this::dropPlace);
		}
		if (!(arguments.get(ARG_NODE_ID) instanceof String nodeId)) {
			return null;
		}
		Object node = findNodeById(nodeId);
		if (node == null) {
			return null;
		}
		switch (command) {
			case EXPAND_COMMAND:
				return recordByKey(node, ExpandNodeByKeyArguments.class, EXPAND_BY_KEY_COMMAND);
			case COLLAPSE_COMMAND:
				return recordByKey(node, CollapseNodeByKeyArguments.class, COLLAPSE_BY_KEY_COMMAND);
			case ACTIVATE_COMMAND:
				return recordByKey(node, ActivateNodeByKeyArguments.class, ACTIVATE_BY_KEY_COMMAND);
			case CONTEXT_MENU_COMMAND: {
				if (!(arguments.get(ARG_X) instanceof Number x) || !(arguments.get(ARG_Y) instanceof Number y)) {
					return null;
				}
				RecordedCommand recorded =
					recordByKey(node, ContextMenuByKeyArguments.class, CONTEXT_MENU_BY_KEY_COMMAND);
				if (recorded != null) {
					ContextMenuByKeyArguments contextMenu = (ContextMenuByKeyArguments) recorded.command();
					contextMenu.setX(x.intValue());
					contextMenu.setY(y.intValue());
				}
				return recorded;
			}
			case SELECT_COMMAND: {
				boolean ctrlKey = Boolean.TRUE.equals(arguments.get(ARG_CTRL_KEY));
				boolean shiftKey = Boolean.TRUE.equals(arguments.get(ARG_SHIFT_KEY));
				if (!multiSelection() || (!ctrlKey && !shiftKey)) {
					return recordByKey(node, SelectNodeByKeyArguments.class, SELECT_BY_KEY_COMMAND);
				}
				if (!_selectionModel.isSelectable(node)) {
					return null;
				}
				return recordSelection(clickSelection(node, ctrlKey, shiftKey));
			}
			default:
				return null;
		}
	}

	/**
	 * The given command addressing the given node by the business identity of its object,
	 * {@code null} when the object has none.
	 */
	private RecordedCommand recordByKey(Object node, Class<? extends ModelKeyArguments> type, String command) {
		ModelName key = ScriptingModelKey.name(null, _treeModel.getBusinessObject(node));
		if (key == null) {
			return null;
		}
		ModelKeyArguments recorded = TypedConfiguration.newConfigItem(type);
		recorded.setName(command);
		recorded.setKey(key);
		return new RecordedCommand(recorded);
	}

	/**
	 * A {@link #SELECT_BY_KEYS_COMMAND} setting the selection to the given nodes, {@code null} when
	 * the object of one of them has no business identity.
	 */
	private RecordedCommand recordSelection(Set<Object> selection) {
		SelectByKeysArguments recorded = TypedConfiguration.newConfigItem(SelectByKeysArguments.class);
		recorded.setName(SELECT_BY_KEYS_COMMAND);
		for (Object node : selection) {
			ModelName name = ScriptingModelKey.name(null, _treeModel.getBusinessObject(node));
			if (name == null) {
				return null;
			}
			recorded.getKeys().add(name);
		}
		return new RecordedCommand(recorded, true);
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
	void handleDrop(DropArguments args) {
		_dropSupport.drop(args, this::dropPlace);
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
	void handleDropObjects(DropObjectsArguments args) {
		_dropSupport.dropObjects(args, this::displaysLocation);
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
			return TreeDropPlace.beside(_navigation);
		}
		Object node = findNodeById(nodeId);
		if (node == null) {
			return null;
		}
		return new TreeDropPlace<>(_navigation, node, nodeId, zone);
	}

	/**
	 * Whether the reference objects of the given location are places of this tree, see
	 * {@link TreeDropPlace#displays(DropTreeNavigation, java.util.function.Function, DropLocation)}.
	 */
	private boolean displaysLocation(DropLocation location) {
		return TreeDropPlace.displays(_navigation, this::nodeOf, location);
	}

	/**
	 * The nodes of this tree as the {@link TreeDropPlace drop rules} see them: the top-level nodes
	 * are the root, where it is displayed, and its children otherwise.
	 */
	private final class Navigation implements DropTreeNavigation<Object> {

		@Override
		public List<?> topLevel() {
			Object root = _treeModel.getRoot();
			return _treeModel.isRootVisible() ? List.of(root) : children(root);
		}

		@Override
		public Object topLevelParent() {
			return _treeModel.isRootVisible() ? null : _treeModel.getBusinessObject(_treeModel.getRoot());
		}

		@Override
		public Object parent(Object node) {
			Object parent = _treeModel.getParent(node);
			if (parent == _treeModel.getRoot() && !_treeModel.isRootVisible()) {
				return null;
			}
			return parent;
		}

		@Override
		public List<?> children(Object node) {
			return _treeModel.isLeaf(node) ? List.of() : _treeModel.getChildren(node);
		}

		@Override
		public boolean isExpanded(Object node) {
			return _treeModel.isExpanded(node);
		}

		@Override
		public Object businessObject(Object node) {
			return _treeModel.getBusinessObject(node);
		}

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
	/**
	 * The answer to a drop probe names a node by its client-side id and is only of use to the client
	 * rendering the drag, so it is left out of the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DROP_VERDICTS);
	}

}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DNDUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Drag;
import com.top_logic.layout.DragAndDropCommand;
import com.top_logic.layout.Drop;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.IdentifierSourceProxy;
import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.LocalScope;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DragSourceSPI;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.list.DoubleClickAction;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.SubtreeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.UpdateVisitor;
import com.top_logic.layout.tree.dnd.LegacyTreeDrag;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropEvent.Position;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link Control} displaying {@link TreeData}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeControl extends AbstractControlBase implements TreeModelListener, TreeDataListener,
		TreeDataEventVisitor, DragSourceSPI, Drag<List<?>>, Drop<List<?>>, ContextMenuOwner {

	/**
	 * CSS class identifying a tree node in the DOM structure.
	 * 
	 * <p>
	 * An element marked with {@link #TREE_NODE_CSS_CLASS} is expected to have an
	 * {@link HTMLConstants#ID_ATTR} with the client-side ID of the tree node.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "inspect.js" and "ajax-form.js".
	 * </p>
	 */
	public static final String TREE_NODE_CSS_CLASS = "treeNode";

	/**
	 * CSS class additionally put on a tree node if it cannot be selected.
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 * 
	 * @see #TREE_NODE_CSS_CLASS
	 */
	public static final String NO_SELECT_CSS = "tcrNoSelect";

	/**
	 * CSS class identifying a node row element that displays the tree node excluding its potential
	 * children.
	 * 
	 * <p>
	 * Elements carrying this class are updated, if local properties of a tree node change. Those
	 * elements also have an ID attribute with the {@link #getNodeId(Object) node ID}.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 */
	public static final String NODE_LABEL_CSS_CLASS = "tcrLabel";

	/**
	 * CSS class for elements representing a node and its potential children.
	 * 
	 * <p>
	 * Elements of this class are updated, if the structure of a node changes.
	 * </p>
	 */
	public static final String SUBTREE_CSS_CLASS = "tcrNode";

	/**
	 * CSS class identifying the expand/collapse button of tree nodes.
	 * 
	 * <p>
	 * When an element marked with {@link #NODE_TOGGLE_BUTTON_CSS} is clicked, the tree node in the
	 * context is expanded or collapsed.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 */
	public static final String NODE_TOGGLE_BUTTON_CSS = FormConstants.TOGGLE_BUTTON_CSS_CLASS + " " + "tcrToggle";

	/**
	 * CSS class set on a node text, if the node itself is not selectable.
	 * 
	 * @see #NO_SELECT_CSS
	 * @see #SELECTABLE_NODE_CSS
	 */
	public static final String UNSELECTABLE_NODE_CSS = "unselectableNode";

	/**
	 * CSS class set on a node anchor of a selectable node.
	 * 
	 * <p>
	 * This class is used from client-side script to prevent default actions on node anchors.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 * 
	 * @see #UNSELECTABLE_NODE_CSS
	 */
	public static final String SELECTABLE_NODE_CSS = "selectableNode" + " " + "tcrLink";

	/**
	 * CSS class set on a node td of a selectable node.
	 * 
	 * <p>
	 * This class is used from client-side script to prevent default actions on node anchors.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 * 
	 * @see #UNSELECTABLE_NODE_CSS
	 */
	public static final String SELECTABLE_ROW_NODE_CSS = "selectableNode" + " " + "tcrLabel";

	/**
	 * CSS class set on an expanded node.
	 * 
	 * <p>
	 * This class is used from client-side script to decide about drag &amp; drop semantics.
	 * </p>
	 * 
	 * <p>
	 * This constant is also used in "ajax-form.js".
	 * </p>
	 */
	public static final String NODE_EXPANDED_CSS_CLASS = "tcrExpanded";

	protected static final Map<String, ControlCommand> TREE_COMMANDS = createCommandMap(new ControlCommand[] {
		ToggleExpansion.INSTANCE,
		NodeSelectAction.INSTANCE,
		NodeDblClickAction.INSTANCE,
		DndTreeDropAction.INSTANCE,
		DragAndDropCommand.INSTANCE,
		ContextMenuOpener.INSTANCE,
		TreeInspector.INSTANCE,
		DragOverAction.INSTANCE
	});

	private final TreeData data;

	/**
	 * {@link IdentityProvider} for the currently displayed nodes of this tree.
	 */
	private final TreeNodeIdentification nodeIds;

	/**
	 * Producer of consistent incremental updates for possibly multiple changes in the
	 * {@link #getModel()} and {@link #getSelectionModel()}.
	 */
	private final TreeUpdateListener updateListener;

	/**
	 * The node-local {@link ControlScope} per node.
	 */
	private final HashMap<Object, LocalScope> scopeByNode = new HashMap<>();

	NodeSelectionVetoListener _selectionVetoListener;

	ExpansionVetoListener _expansionVetoListener;

	private Object lastClickedNode;

	private Optional<LegacyTreeDrag> _legacyDrag;

	private Optional<Drop<List<?>>> _legacyDrop;

	/**
	 * @see #getTitleBar()
	 */
	private View titleBar;

	Optional<DoubleClickAction<Control, Object>> _dblClickAction;

	private ContextMenuProvider _contextMenuProvider = NoContextMenuProvider.INSTANCE;

	/**
	 * Create a {@link TreeControl} with active nodes.
	 */
	public TreeControl(TreeData data) {
		this(data, TREE_COMMANDS);
	}

	protected TreeControl(TreeData data, Map<String, ControlCommand> treeCommands) {
		super(treeCommands);

		this.data = data;

		this.nodeIds = new TreeNodeIdentification(new IdentifierSourceProxy(this));
		this.updateListener = initUpdateListener(nodeIds);
		_legacyDrag = Optional.empty();
		_legacyDrop = Optional.empty();
		setDblClickAction(null);
	}

	protected TreeUpdateListener initUpdateListener(TreeNodeIdentification aTreeNodeIdentification) {
		return new TreeUpdateListener(aTreeNodeIdentification, this);
	}

	protected void initTreeModel(TreeUIModel newModel) {
		newModel.addTreeModelListener(this);
		this.updateListener.setModel(newModel);
		this.nodeIds.setModel(newModel);
	}

	protected void initSelectionModel(SelectionModel newSelection) {
		this.updateListener.setSelectionModel(newSelection);
	}

	/**
	 * The current value model of this tree.
	 */
	@Override
	public final TreeUIModel getModel() {
		return data.getTreeModel();
	}

	/**
	 * The current {@link SelectionModel} of this tree.
	 */
	public final SelectionModel getSelectionModel() {
		return data.getSelectionModel();
	}

	/**
	 * The current {@link TreeRenderer} of this tree.
	 */
	public final TreeRenderer getRenderer() {
		return data.getTreeRenderer();
	}

	@Override
	public void handleTreeDataChange(TreeDataEvent event) {
		event.visit(this);
	}

	@Override
	public void notifySelectionModelChange(TreeData source, SelectionModel newSelection, SelectionModel oldSelection) {
		if (!Utils.equals(data, source)) {
			return;
		}
		initSelectionModel(newSelection);
	}

	@Override
	public void notifyRendererChange(TreeData source, TreeRenderer newRenderer, TreeRenderer oldRenderer) {
		if (!Utils.equals(data, source)) {
			return;
		}
		requestRepaint();
	}

	@Override
	public void notifyTreeModelChange(TreeData source, TreeUIModel newModel, TreeUIModel oldModel) {
		if (!Utils.equals(data, source)) {
			return;
		}
		oldModel.removeTreeModelListener(this);
		initTreeModel(newModel);
		requestRepaint();
	}

	/**
	 * Set a {@link DoubleClickAction}, which shall be performed, if a double click on a node
	 * occurred. A <code>null</code> value means, that no action shall be performed.
	 */
	@SuppressWarnings("unchecked")
	public void setDblClickAction(DoubleClickAction<? extends Control, ? extends Object> dblClickAction) {
		_dblClickAction = Optional.ofNullable((DoubleClickAction<Control, Object>) dblClickAction);
		requestRepaint();
	}

	/**
	 * Generate output to make a DOM node respond to double click actions.
	 */
	public void writeOnDblClick(TagWriter out) throws IOException {
		if (_dblClickAction.isPresent()) {
			out.beginAttribute(ONDBLCLICK_ATTR);
			out.append("return services.form.TreeControl.dblClick(event, '");
			out.append(getID());
			out.append("', ");
			out.append(String.valueOf(_dblClickAction.get().isWaitPaneRequested()));
			out.append(");");
			out.endAttribute();
		}
	}

	@Override
	protected boolean hasUpdates() {
		return this.updateListener.hasUpdates() || hasChildUpdates();
	}

	/**
	 * Whether any node-local child {@link Control} has updates.
	 */
	private boolean hasChildUpdates() {
		for (Iterator<LocalScope> it = scopeByNode.values().iterator(); it.hasNext();) {
			LocalScope scope = it.next();
			if (scope.hasUpdates()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Flush before producing updates to make sure to create fresh IDs for
		// moved nodes.
		this.nodeIds.flush();

		try {
			final UpdateVisitor<Void, UpdateQueue, TreeControl> updateVisitor = getUpdateVisitor();
			for (Iterator it = updateListener.getUpdates().iterator(); it.hasNext();) {
				NodeUpdate update = (NodeUpdate) it.next();
				update.visit(updateVisitor, actions, this);
			}

			// Child controls must not be registered at the global scope, because
			// they may produce updates during a revalidation in which they are
			// removed from the display from their container control.
			for (Iterator<LocalScope> it = scopeByNode.values().iterator(); it.hasNext();) {
				LocalScope scope = it.next();
				scope.revalidate(context, actions);
			}
		} finally {
			this.updateListener.clear();
		}

	}

	@Override
	protected void handleRepaintRequested(UpdateQueue actions) {
		super.handleRepaintRequested(actions);
		// Flush before producing updates to make sure to create fresh IDs for
		// moved nodes.
		this.nodeIds.flush();
		this.updateListener.clear();
	}

	protected UpdateVisitor<Void, UpdateQueue, TreeControl> getUpdateVisitor() {
		return new UpdateVisitor<>() {
			@Override
			public Void visitNodeUpdate(NodeUpdate nodeUpdate, UpdateQueue arg1, TreeControl arg2) {
				Object node = nodeUpdate.getNode();
				clearSingleNodeScope(node, false);

				getRenderer().addNodeUpdateActions(arg1, arg2, node, nodeUpdate.getNodeId());
				return null;
			}

			@Override
			public Void visitSubtreeUpdate(SubtreeUpdate subtreeUpdate, UpdateQueue arg1, TreeControl arg2) {
				Object node = subtreeUpdate.getNode();
				clearSubTreeScope(node);

				final String stopID = subtreeUpdate.getRightmostVisibleDescendantId();
				getRenderer().addSubtreeUpdateActions(arg1, arg2, node, subtreeUpdate.getNodeId(), stopID);
				return null;
			}

		};
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		this.updateListener.attach();
	}

	@Override
	protected void detachInvalidated() {
		this.updateListener.detach();

		clearAllNodeScopes();

		super.detachInvalidated();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		this.data.addTreeDataListener(this);

		initTreeModel(getModel());
		initSelectionModel(getSelectionModel());

		this.nodeIds.attach();
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();

		this.getModel().removeTreeModelListener(this);
		this.nodeIds.detach();

		this.data.removeTreeDataListener(this);
	}

	ControlScope getOrCreateNodeScope(NodeContext nodeContext) {
		Object node = nodeContext.currentNode();
		LocalScope localScope = lookupNodeScope(node);

		if (localScope == null) {
			localScope = newLocalScope();
			scopeByNode.put(node, localScope);
		}

		return localScope;
	}

	@Override
	protected void disableChildScopes(boolean disabled) {
		super.disableChildScopes(disabled);

		for (LocalScope ls : scopeByNode.values()) {
			ls.disableScope(disabled);
		}
	}

	private LocalScope lookupNodeScope(Object node) {
		return scopeByNode.get(node);
	}

	private LocalScope removeNodeScope(Object node) {
		return scopeByNode.remove(node);
	}

	private void clearAllNodeScopes() {
		for (Iterator<LocalScope> it = this.scopeByNode.values().iterator(); it.hasNext();) {
			LocalScope nodeScope = it.next();
			nodeScope.clear();
		}
		scopeByNode.clear();
	}

	/**
	 * Looks for the scope for the given node and {@link LocalScope#clear() clears} it.
	 * 
	 * @param node
	 *        the node whose scope must be cleared
	 * @param remove
	 *        if <code>true</code>, the scope is not just cleared but also removed.
	 * 
	 * @return <code>true</code> iff there is a scope to clear
	 */
	/* package protected */boolean clearSingleNodeScope(Object node, boolean remove) {
		LocalScope nodeScope;
		if (remove) {
			nodeScope = removeNodeScope(node);
		} else {
			nodeScope = lookupNodeScope(node);
		}
		if (nodeScope != null) {
			nodeScope.clear();
			return true;
		} else {
			return false;
		}
	}

	/* package protected */ void clearSubTreeScope(Object subtreeRoot) {
		if (subtreeRoot.equals(getModel().getRoot()) && !getModel().isRootVisible()) {
			assert lookupNodeScope(subtreeRoot) == null : "No local scope for invisble root";
			removeChildScopes(subtreeRoot);
		} else {
			clearSubtreeScope(subtreeRoot, false);
		}
	}

	private void clearSubtreeScope(Object subtreeRoot, boolean removeScope) {
		if (clearSingleNodeScope(subtreeRoot, removeScope)) {
			removeChildScopes(subtreeRoot);
		}
	}

	private void removeChildScopes(Object subtreeRoot) {
		TreeUIModel treeModel = getModel();
		if (!treeModel.childrenInitialized(subtreeRoot)) {
			// Children not initialized, therefore no cached groups for children.
			return;
		}
		for (Iterator<?> it = treeModel.getChildIterator(subtreeRoot); it.hasNext();) {
			clearSubtreeScope(it.next(), true);
		}
	}

	public final String getNodeId(Object node) {
		return nodeIds.getObjectId(node);
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		switch (evt.getType()) {
			case TreeModelEvent.BEFORE_COLLAPSE: {
				Object node = evt.getNode();

				clearSubTreeScope(node);
				break;
			}
			case TreeModelEvent.AFTER_NODE_REMOVE: {
				Object node = evt.getNode();
				clearSubTreeScope(node);
				break;
			}
		}
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		clearAllNodeScopes();
		getRenderer().writeControl(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		getRenderer().appendControlCSSClasses(out, this);
	}

	/**
	 * Sets the {@link NodeSelectionVetoListener} to be able to give a veto for select a new node.
	 */
	public void setNodeSelectionVetoListener(NodeSelectionVetoListener listener) {
		_selectionVetoListener = listener;
	}

	/**
	 * Sets the {@link NodeSelectionVetoListener} to be able to give a veto for select a new node.
	 */
	public void setNodeExpansionVetoListener(ExpansionVetoListener listener) {
		_expansionVetoListener = listener;
	}

	/**
	 * The {@link ContextMenuProvider} for displayed nodes in this tree.
	 */
	public ContextMenuProvider getContextMenuProvider() {
		return _contextMenuProvider;
	}

	/**
	 * @see #getContextMenuProvider()
	 */
	public void setContextMenuProvider(ContextMenuProvider contextMenuProvider) {
		_contextMenuProvider = contextMenuProvider;
		requestRepaint();
	}

	/**
	 * Decides, whether the given node has a context menu.
	 * 
	 * @see #getContextMenuProvider()
	 * @see ContextMenuProvider#hasContextMenu(Object)
	 */
	public boolean hasContextMenu(Object node) {
		if (node == null) {
			return false;
		}
		return _contextMenuProvider.hasContextMenu(node);
	}

	@Override
	public Menu createContextMenu(String contextInfo) {
		Object directTarget = getNodeById(contextInfo);
		Set<?> selection = getSelectionModel().getSelection();
		Object extendedTarget = ContextMenuProvider.getContextMenuTarget(directTarget, selection);
		return _contextMenuProvider.getContextMenu(extendedTarget);
	}

	@Override
	public Object getDragData(String ref) {
		return getNodeById(ref);
	}

	@Override
	public Maybe<? extends ModelName> getDragDataName(Object dragSource, String ref) {
		return ModelResolver.buildModelNameIfAvailable(dragSource, getDragData(ref));
	}

	@Override
	public Object getDragSourceModel() {
		return getData();
	}

	Object getLastClickedNode() {
		return lastClickedNode;
	}

	void setLastClickedNode(Object clickedNode) {
		lastClickedNode = clickedNode;
	}

	@Override
	public void notifyDrag(String dropId, Object dragInfo, Object dropInfo) {
		_legacyDrag.ifPresent(drag -> drag.notifyDrag(dropId, dragInfo, dropInfo));
	}

	@Override
	public void notifyDrop(List<?> value, Object dropInfo) throws com.top_logic.layout.Drop.DropException {
		if (_legacyDrop.isPresent()) {
			_legacyDrop.get().notifyDrop(value, dropInfo);
		}
	}

	/**
	 * true, if legacy support of drag operation shall be used, false otherwise.
	 */
	public boolean useLegacyDrag() {
		return _legacyDrag.isPresent();
	}

	/**
	 * Creates the {@link HTMLConstants#TL_DRAG_N_DROP} attribute.
	 * 
	 * @see DNDUtil#writeDNDInfo(TagWriter, com.top_logic.layout.FrameScope, Collection)
	 */
	public void writeLegacyDragAndDropSupport(DisplayContext context, TagWriter out) throws IOException {
		if (_legacyDrag.isPresent()) {
			out.writeAttribute(TL_TYPE_ATTR, "services.form.TreeControl");
			DNDUtil.writeDNDInfo(out, getScope().getFrameScope(), _legacyDrag.get().getDropTargets());
		}
	}

	/**
	 * true, if legacy support of drop operation shall be used, false otherwise.
	 */
	public boolean useLegacyDrop() {
		return _legacyDrop.isPresent();
	}

	/**
	 * Set drag operation, that shall be performed during legacy Drag'n'Drop.
	 * 
	 * @see #setLegacyTreeDropOperation(Drop)
	 */
	public void setLegacyTreeDragOperation(LegacyTreeDrag dragOperation) {
		_legacyDrag = Optional.ofNullable(dragOperation);
		requestRepaint();
	}

	/**
	 * Set drop operation, that shall be performed during legacy Drag'n'Drop.
	 * 
	 * @see #setLegacyTreeDragOperation(LegacyTreeDrag)
	 */
	public void setLegacyTreeDropOperation(Drop<List<?>> dropOperation) {
		_legacyDrop = Optional.ofNullable(dropOperation);
		requestRepaint();
	}

	/**
	 * Abstract base class for actions that modify the displayed tree state using AJAX mechanisms.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract static class TreeAction extends ControlCommand {

		public static final String ID_PARAM = "id";

		protected TreeAction(String id) {
			super(id);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			TreeControl treeControl = (TreeControl) control;
			String id = (String) arguments.get(ID_PARAM);

			Object node;
			if (id != null) {
				// Handle cases, where node IDs are prefixed since a single node has more than one
				// element in the DOM (as in viewport tables).
				int separator = id.indexOf('_');
				if (separator >= 0) {
					id = id.substring(0, separator);
				}
				node = treeControl.getNodeById(id);

				if (node == null) {
					Logger.error("Tree node not found for id '" + id + "' in AJAX request.", this);
					return HandlerResult.DEFAULT_RESULT;
				}
			} else {
				node = null;
			}

			return execute(context, treeControl, node, arguments);
		}

		/**
		 * Implementation of this command. This method must be implemented in concrete sub-classes.
		 * 
		 * @param node
		 *        The tree nodee that was referenced in the request with the <code>id</code>
		 *        argument.
		 * @param arguments
		 *        A list of further arguments passed along with this command invocation.
		 * @return A sequence of actions that are executed on the client-side in response to this
		 *         command.
		 */
		protected abstract HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node,
				Map<String, Object> arguments);

		public void writeInvokeExpression(TagWriter out, TreeControl tree, String nodeID) throws IOException {
			writeInvokeExpression(out, tree, new JSObject(ID_PARAM, new JSString(nodeID)));
		}
	}

	/**
	 * Super class of {@link TreeAction} that wants to toogle expansion state of a node.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ToggleExpansion extends TreeAction {

		/**
		 * Name of the {@link TreeAction} that collapses a tree node.
		 */
		public static final String COMMAND_NAME = "toggleExpand";

		public static final ToggleExpansion INSTANCE = new ToggleExpansion();

		/**
		 * Singleton constructor.
		 */
		protected ToggleExpansion() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node, Map arguments) {
			return toggleExpansion(treeControl, node);
		}

		protected HandlerResult toggleExpansion(final TreeControl tree, final Object toggleNode) {
			boolean expand = !tree.getModel().isExpanded(toggleNode);
			recordToggle(tree, toggleNode, expand);
			return doExpand(tree, toggleNode, expand);
		}

		HandlerResult doExpand(final TreeControl tree, final Object toggleNode, final boolean expand) {
			if (tree._expansionVetoListener != null) {
				try {
					tree._expansionVetoListener.checkVeto(tree, toggleNode, expand);
				} catch (VetoException ex) {
					ex.setContinuationCommand(new Command() {

						@Override
						public HandlerResult executeCommand(DisplayContext context) {
							return doExpand(tree, toggleNode, expand);
						}

					});
					ex.process(tree.getWindowScope());
					return HandlerResult.DEFAULT_RESULT;
				}
			}
			tree.getModel().setExpanded(toggleNode, expand);
			return HandlerResult.DEFAULT_RESULT;
		}

		protected static void recordToggle(TreeControl treeControl, Object node, boolean expand) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordExpand(treeControl.getData(), node, expand);
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TOGGLE_NODE_ACTION;
		}
	}

	/**
	 * Action that selects the referenced node and deselects all formerly selected nodes.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected static class NodeSelectAction extends TreeAction {

		/** Parameter (boolean) whether CTRL key is pressed or not. */
		public static final String CTRL_PARAM = "ctrlModifier";

		/** Parameter (boolean) whether SHIFT key is pressed or not. */
		public static final String SHIFT_PARAM = "shiftModifier";

		/**
		 * Name of the {@link TreeAction} that selects a tree node.
		 */
		public static final String COMMAND_NAME = "selectTree";

		public static final NodeSelectAction INSTANCE = new NodeSelectAction();

		/**
		 * Singleton constructor.
		 */
		protected NodeSelectAction() {
			super(NodeSelectAction.COMMAND_NAME);
		}

		/**
		 * Answers the selection request with a sequence of client actions that update the newly
		 * selected node and the formerly selected nodes. Additionally, a JavaScript snipplet is
		 * sent that re-validates all other components currently displayed on the client's page.
		 */
		@Override
		protected HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node, Map arguments) {
			SelectionModel selectionModel = treeControl.getSelectionModel();
			boolean ctrl = Utils.getbooleanValue(arguments.get(CTRL_PARAM));
			boolean shift = Utils.getbooleanValue(arguments.get(SHIFT_PARAM));
			boolean selected;
			if (ctrl) {
				// CTRL means inversion of selection.
				selected = !selectionModel.isSelected(node);
			} else {
				selected = true;
			}

			return changeSelection(treeControl, node, shift, ctrl, selected);
		}

		HandlerResult changeSelection(TreeControl treeControl, Object node, boolean shift, boolean ctrl,
				boolean selected) {
			SelectionModel selectionModel = treeControl.getSelectionModel();
			if (treeControl._selectionVetoListener != null) {
				try {
					treeControl._selectionVetoListener.checkVeto(selectionModel, node, selected);
				} catch (VetoException ex) {
					handleVetoException(ex, treeControl, node, shift, ctrl, selected);
					return HandlerResult.DEFAULT_RESULT;
				}
			}

			if (shift && selectionModel.isMultiSelectionSupported()) {
				Set<Object> selectedNodes = getNextSelection(treeControl, node, selectionModel);
				selectionModel.setSelection(selectedNodes);
				recordSelection(treeControl, selectedNodes, true, SelectionChangeKind.ABSOLUTE);
			} else if (ctrl) {
				selectionModel.setSelected(node, selected);
				if (selectionModel.getSelection().isEmpty()) {
					recordSelection(treeControl, Collections.emptySet(), true, SelectionChangeKind.ABSOLUTE);
				} else {
					recordSelection(treeControl, node, selected, SelectionChangeKind.INCREMENTAL);
				}
			} else {
				// Select exactly the requested node.
				selectionModel.setSelection(Collections.singleton(node));
				recordSelection(treeControl, node, true, SelectionChangeKind.ABSOLUTE);
			}
			treeControl.setLastClickedNode(node);

			return HandlerResult.DEFAULT_RESULT;
		}

		private void recordSelection(TreeControl treeControl, Object selection, boolean selected,
				SelectionChangeKind selectionChangeKind) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(
					treeControl.getData(), selection, selected, selectionChangeKind);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Set<Object> getNextSelection(TreeControl treeControl, Object node, SelectionModel selectionModel) {
			boolean selectNodes;
			Set<Object> markerNodes = new HashSet<>();
			Object lastClickedNode = treeControl.getLastClickedNode();
			if (lastClickedNode == null || !treeControl.getModel().isDisplayed(lastClickedNode)) {
				selectNodes = true;
				markerNodes.add(node);
			} else {
				selectNodes = false;
				markerNodes.addAll(Arrays.asList(lastClickedNode, node));
			}

			Set<Object> selectedNodes = new HashSet<>();
			Iterator visibleNodeIterator = TreeUIModelUtil.createVisibleNodeDFSIterator(treeControl.getModel());
			while (visibleNodeIterator.hasNext()) {
				Object treeNode = visibleNodeIterator.next();
				if (markerNodes.contains(treeNode)) {
					selectNodes = !selectNodes;
					markerNodes.remove(treeNode);
				}
				if (selectionModel.isSelectable(treeNode) && (selectNodes || markerNodes.isEmpty())) {
					selectedNodes.add(treeNode);
				}
				if (markerNodes.isEmpty()) {
					break;
				}
			}
			return selectedNodes;
		}

		private void handleVetoException(VetoException ex, final TreeControl treeControl, final Object node,
				boolean shift, final boolean ctrl, final boolean selected) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return changeSelection(treeControl, node, shift, ctrl, selected);
				}
			});
			ex.process(treeControl.getWindowScope());
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.NODE_SELECT_ACTION;
		}
	}

	@SuppressWarnings("javadoc")
	protected static class NodeDblClickAction extends TreeAction {

		/**
		 * Name of the {@link TreeAction} that selects a tree node.
		 */
		public static final String COMMAND_NAME = "dblClick";

		/** Singleton instance of {@link NodeDblClickAction} */
		public static final NodeDblClickAction INSTANCE = new NodeDblClickAction();

		/**
		 * Singleton constructor.
		 */
		protected NodeDblClickAction() {
			super(NodeDblClickAction.COMMAND_NAME);
		}

		/**
		 * Answers the selection request with a sequence of client actions that update the newly
		 * selected node and the formerly selected nodes. Additionally, a JavaScript snipplet is
		 * sent that re-validates all other components currently displayed on the client's page.
		 */
		@Override
		protected HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node, Map arguments) {
			if (treeControl._dblClickAction.isPresent()) {
				return handleDblClick(context, treeControl, node);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		HandlerResult handleDblClick(DisplayContext context, TreeControl treeControl, Object node) {
			SelectionModel selectionModel = treeControl.getSelectionModel();
			if (treeControl._selectionVetoListener != null) {
				try {
					treeControl._selectionVetoListener.checkVeto(selectionModel, node, true);
				} catch (VetoException ex) {
					handleVetoException(ex, treeControl, node);
					return HandlerResult.DEFAULT_RESULT;
				}
			}
			recordSelection(treeControl, node, true, SelectionChangeKind.ABSOLUTE);
			treeControl._dblClickAction.get().handleDoubleClick(context, treeControl, node);
			return HandlerResult.DEFAULT_RESULT;
		}

		private void recordSelection(TreeControl treeControl, Object selection, boolean selected,
				SelectionChangeKind selectionChangeKind) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(
					treeControl.getData(), selection, selected, selectionChangeKind);
			}
		}

		private void handleVetoException(VetoException ex, final TreeControl treeControl, final Object node) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return handleDblClick(context, treeControl, node);
				}
			});
			ex.process(treeControl.getWindowScope());
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.NODE_DBL_CLICK_ACTION;
		}
	}

	/**
	 * {@link TreeAction} executed, when the element is dragged over a drop zone.
	 */
	public static class DragOverAction extends TreeAction {

		private static final String COMMAND_NAME = "dragOver";

		/**
		 * Singleton {@link DragOverAction} instance.
		 */
		public static final DragOverAction INSTANCE = new DragOverAction();

		private DragOverAction() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node,
				Map<String, Object> arguments) {
			DndData data = DnD.getDndData(context, arguments);

			if (data != null) {
				TreeData treeData = treeControl.getData();
				String position = (String) arguments.get("pos");
				TreeDropEvent dropEvent = new TreeDropEvent(data, treeData, node, Position.fromString(position));

				List<TreeDropTarget> dropTargets = treeData.getDropTargets();
				for (TreeDropTarget dropTarget : dropTargets) {
					if (dropTarget.canDrop(dropEvent)) {
						displayDropMarker(treeControl, (String) arguments.get(ID_PARAM), position);

						return HandlerResult.DEFAULT_RESULT;
					}
				}

				changeToNoDropCursor(treeControl, (String) arguments.get(ID_PARAM), position);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private void changeToNoDropCursor(TreeControl treeControl, String targetID, String position) {
			treeControl.getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.TreeControl.changeToNoDropCursor(");
					TagUtil.writeJsString(out, targetID);
					out.append(",");
					TagUtil.writeJsString(out, position);
					out.append(");");
				}
			}));
		}

		private void displayDropMarker(TreeControl treeControl, String targetID, String position) {
			treeControl.getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.form.TreeControl.displayDropMarker(");
					TagUtil.writeJsString(out, targetID);
					out.append(",");
					TagUtil.writeJsString(out, position);
					out.append(");");
				}
			}));
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DND_TREE_DRAG_OVER_ACTION;
		}

	}

	/**
	 * {@link TreeAction} executed, when a drop operation happens on a tree.
	 */
	public static class DndTreeDropAction extends TreeAction {

		/**
		 * Singleton {@link DndTreeDropAction} instance.
		 */
		public static final DndTreeDropAction INSTANCE = new DndTreeDropAction();

		private static final String COMMAND_NAME = "dndTreeDrop";

		private static final String POS_PARAM = "pos";

		private DndTreeDropAction() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TreeControl treeControl, Object node, Map arguments) {
			TreeData treeData = treeControl.getData();

			DndData data = DnD.getDndData(context, arguments);
			if (data != null) {
				String pos = (String) arguments.get(POS_PARAM);

				TreeDropEvent dropEvent = new TreeDropEvent(data, treeData, node, Position.fromString(pos));

				List<TreeDropTarget> dropTargets = treeData.getDropTargets();
				for (TreeDropTarget dropTarget : dropTargets) {
					if (dropTarget.canDrop(dropEvent)) {
						dropTarget.handleDrop(dropEvent);

						return HandlerResult.DEFAULT_RESULT;
					}
				}

				throw new TopLogicException(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DND_TREE_DROP_ACTION;
		}

	}

	@FrameworkInternal
	public final Object getNodeById(String id) {
		return nodeIds.getObjectById(id);
	}

	/**
	 * Sets a view for the title of the tree
	 * 
	 * @param titleBar
	 *        the new view to the title. may be <code>null</code>
	 * @see #getTitleBar()
	 */
	public void setTitleBar(View titleBar) {
		this.titleBar = titleBar;
	}

	/**
	 * Returns a view which will be rendered as title of the tree.
	 * 
	 * @return may be <code>null</code> if no title shall be written
	 */
	public View getTitleBar() {
		return titleBar;
	}

	public TreeData getData() {
		return data;
	}

}

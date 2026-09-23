/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.react.flow.callback.ClickHandler;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.ContextMenu;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.MouseButton;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.SelectionUtil;
import com.top_logic.react.flow.operations.WidgetTraversal;
import com.top_logic.react.flow.server.handler.DiagramContextMenuProviderSPI;
import com.top_logic.react.flow.server.handler.ServerDropHandler;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * {@link ReactControl} for displaying flow diagrams.
 *
 * <p>
 * This control serializes a {@link Diagram} model as initial React state and uses SSE to push
 * incremental msgbuf patches to the client. Client commands (click, drop, update) are received via
 * {@link ReactCommandHandler @ReactCommandHandler}-annotated methods.
 * </p>
 */
public class FlowDiagramControl extends ReactControl {

	/** The React module identifier for the flow diagram component. */
	public static final String REACT_MODULE = "TLFlowDiagram";

	/** Name of the command the client sends its changes to the displayed diagram with. */
	public static final String CMD_UPDATE = "update";

	/** Name of the {@link #CMD_UPDATE} argument holding the msgbuf patch of those changes. */
	public static final String ARG_PATCH = "patch";

	/** State key holding the serialized {@link Diagram} the client mounts on. */
	private static final String DIAGRAM_STATE = "diagram";

	/** State key holding a msgbuf patch of the server-side changes to the displayed diagram. */
	private static final String DIAGRAM_PATCH_STATE = "diagramPatch";

	/**
	 * Notified when the user objects of the selected diagram elements change.
	 */
	public interface SelectionListener {

		/**
		 * Called after the selection changed, with the user objects of the elements now selected.
		 *
		 * <p>
		 * A listener refuses the change by throwing a
		 * {@link com.top_logic.layout.view.channel.ChannelVetoException} - the unsaved changes of a
		 * form the selection would replace block it. The refusal unwinds through whoever changed
		 * the selection; the diagram keeps what the client displays.
		 * </p>
		 */
		void selectionChanged(Set<Object> userObjects);
	}

	/**
	 * Notified when the displayed {@link Diagram} was replaced.
	 */
	public interface ModelListener {

		/**
		 * Called after {@link FlowDiagramControl#setModel(Diagram)} carried the selection over to
		 * the new diagram, and before that diagram is serialized for the client.
		 *
		 * <p>
		 * The new diagram is the control's {@link FlowDiagramControl#getModel() model}, and what is
		 * left of the selection is its {@link FlowDiagramControl#getSelectedUserObjects() selected
		 * user objects}. A listener may select further elements; the selection it leaves behind is
		 * the one the client is served.
		 * </p>
		 */
		void modelReplaced();
	}

	private Diagram _diagram;

	private DefaultScope _graphScope;

	private ContextMenuProvider _contextMenuProvider = NoContextMenuProvider.INSTANCE;

	private final List<SelectionListener> _selectionListeners = new CopyOnWriteArrayList<>();

	private final List<ModelListener> _modelListeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link FlowDiagramControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param diagram
	 *        The diagram model to display, may be {@code null}.
	 * @param contextMenuProvider
	 *        The context menu provider, or {@code null} for no context menu.
	 */
	public FlowDiagramControl(ReactContext context, Diagram diagram, ContextMenuProvider contextMenuProvider) {
		super(context, diagram, REACT_MODULE);

		_diagram = diagram;
		if (contextMenuProvider != null) {
			_contextMenuProvider = contextMenuProvider;
		}
	}

	/**
	 * Creates a {@link FlowDiagramControl} without a context menu.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param diagram
	 *        The diagram model to display, may be {@code null}.
	 */
	public FlowDiagramControl(ReactContext context, Diagram diagram) {
		this(context, diagram, null);
	}

	@Override
	public Diagram getModel() {
		return _diagram;
	}

	/**
	 * Displays the given {@link Diagram} instead of the one displayed so far.
	 *
	 * <p>
	 * The selection is carried over: the elements of the new diagram that carry a user object
	 * selected in the old one are selected, and a user object the new diagram has no element for is
	 * simply not displayed as selected any more. What that means for a selection published
	 * elsewhere is the {@link ModelListener}'s to decide - it is told after the carry-over and
	 * before the new diagram is serialized, so that the elements it selects reach the client with
	 * that diagram rather than as a patch racing the remount it triggers.
	 * </p>
	 *
	 * @param diagram
	 *        The diagram to display, may be <code>null</code> to display nothing.
	 */
	public void setModel(Diagram diagram) {
		Set<Object> selectedUserObjects = getSelectedUserObjects();

		_diagram = diagram;
		// The scope belongs to the diagram it serialized, which nothing displays any more: the
		// markings below are part of the new diagram's serialization, not of a patch to the old
		// one.
		_graphScope = null;

		markSelection(selectedUserObjects);
		notifyModelReplaced();

		initDiagramState();
	}

	/**
	 * The user objects of the currently selected diagram elements, in selection order.
	 */
	public Set<Object> getSelectedUserObjects() {
		if (_diagram == null) {
			return Set.of();
		}
		Set<Object> result = new LinkedHashSet<>();
		for (Widget widget : _diagram.getSelection()) {
			if (!SelectionUtil.isSelected(widget)) {
				continue;
			}
			Object userObject = widget.getUserObject();
			if (userObject != null) {
				result.add(userObject);
			}
		}
		return result;
	}

	/**
	 * Selects exactly the diagram elements carrying one of the given user objects, deselects every
	 * other one, and shows the result.
	 *
	 * <p>
	 * A user object no element carries selects nothing - it is not part of this diagram, and
	 * nothing can be marked for it. The {@link SelectionListener}s are told what the diagram
	 * displays afterwards.
	 * </p>
	 *
	 * @param userObjects
	 *        The user objects to select the elements of, empty to select nothing.
	 */
	public void selectUserObjects(Set<?> userObjects) {
		if (_diagram == null) {
			return;
		}
		markSelection(userObjects);
		pushDiagramChanges();

		notifySelectionChanged();
	}

	/**
	 * Whether the diagram lets the user select more than one element at a time.
	 */
	public boolean isMultiSelect() {
		return _diagram != null && _diagram.isMultiSelect();
	}

	/**
	 * Registers a listener notified on selection changes.
	 *
	 * @param listener
	 *        The listener to notify.
	 *
	 * @see #removeSelectionListener(SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		_selectionListeners.add(listener);
	}

	/**
	 * Unregisters a listener added through {@link #addSelectionListener(SelectionListener)}.
	 *
	 * @param listener
	 *        The listener to stop notifying.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		_selectionListeners.remove(listener);
	}

	/**
	 * Registers a listener notified when the displayed {@link Diagram} is replaced.
	 *
	 * @param listener
	 *        The listener to notify.
	 *
	 * @see #removeModelListener(ModelListener)
	 */
	public void addModelListener(ModelListener listener) {
		_modelListeners.add(listener);
	}

	/**
	 * Unregisters a listener added through {@link #addModelListener(ModelListener)}.
	 *
	 * @param listener
	 *        The listener to stop notifying.
	 */
	public void removeModelListener(ModelListener listener) {
		_modelListeners.remove(listener);
	}

	/**
	 * Marks the diagram elements carrying one of the given user objects as selected and every other
	 * one as unselected, without telling anybody about it.
	 */
	private void markSelection(Set<?> userObjects) {
		if (_diagram == null) {
			return;
		}
		List<Widget> selected = new ArrayList<>();
		WidgetTraversal.visitAll(_diagram, widget -> {
			Object userObject = widget.getUserObject();
			boolean select =
				userObject != null && SelectionUtil.isSelectable(widget) && userObjects.contains(userObject);
			if (select) {
				selected.add(widget);
			}
			if (SelectionUtil.isSelected(widget) != select) {
				// Only a widget whose marking actually changes is written, so that the patch
				// carries the elements that change their appearance and no others.
				SelectionUtil.setSelected(widget, select);
			}
		});
		if (!selected.equals(_diagram.getSelection())) {
			_diagram.setSelection(selected);
		}
	}

	/**
	 * Tells the {@link SelectionListener}s what the diagram displays as selected.
	 */
	private void notifySelectionChanged() {
		Set<Object> userObjects = getSelectedUserObjects();
		for (SelectionListener listener : _selectionListeners) {
			listener.selectionChanged(userObjects);
		}
	}

	/**
	 * Tells the {@link ModelListener}s that the displayed diagram was replaced.
	 */
	private void notifyModelReplaced() {
		for (ModelListener listener : _modelListeners) {
			listener.modelReplaced();
		}
	}

	/**
	 * The provider for a context menu for user objects of {@link SelectableBox} nodes.
	 */
	public ContextMenuProvider getContextMenuProvider() {
		return _contextMenuProvider;
	}

	/**
	 * @see #getContextMenuProvider()
	 */
	public void setContextMenuProvider(ContextMenuProvider contextMenuProvider) {
		_contextMenuProvider = contextMenuProvider;
	}

	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();

		initDiagramState();
	}

	@Override
	protected void onCleanup() {
		_graphScope = null;

		super.onCleanup();
	}

	/**
	 * Serializes the current diagram model and stores it as initial React state.
	 */
	private void initDiagramState() {
		if (_diagram == null) {
			putState(DIAGRAM_STATE, "");
			return;
		}

		_graphScope = new ExternalScope(2, 0);
		StringW out = new StringW();
		try {
			_diagram.writeTo(_graphScope, new JsonWriter(out));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		putState(DIAGRAM_STATE, out.toString());
	}

	/**
	 * Pushes pending diagram changes to the client via SSE.
	 *
	 * <p>
	 * Call this method after making server-side changes to the diagram model. The msgbuf patch is
	 * serialized and sent as the {@link #DIAGRAM_PATCH_STATE} state update. Nothing is sent while
	 * the diagram has not been serialized yet, or after the control was disposed.
	 * </p>
	 */
	public void pushDiagramChanges() {
		if (_graphScope != null && _graphScope.hasChanges()) {
			StringW out = new StringW();
			try {
				_graphScope.createPatch(new JsonWriter(out));
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
			putState(DIAGRAM_PATCH_STATE, out.toString());
		}
	}

	/**
	 * Handles msgbuf patch updates sent from the client.
	 */
	@ReactCommandHandler(CMD_UPDATE)
	public HandlerResult handleUpdate(ReactContext context, Map<String, Object> args) {
		String patch = (String) args.get(ARG_PATCH);
		try {
			processUpdate(patch);
		} catch (IOException ex) {
			Logger.error("Failed to update diagram.", ex, FlowDiagramControl.class);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles click events sent from the client.
	 */
	@ReactCommandHandler("dispatchClick")
	public HandlerResult handleClick(ReactContext context, Map<String, Object> args) {
		@SuppressWarnings("unchecked")
		List<String> buttonNames = (List<String>) args.get("mouseButtons");
		Set<MouseButton> buttons =
			buttonNames.stream().map(n -> MouseButton.valueOf(n)).collect(Collectors.toSet());
		int nodeId = ((Number) args.get("nodeId")).intValue();
		processClick(nodeId, buttons);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles drop events sent from the client.
	 */
	@ReactCommandHandler("dispatchDrop")
	public HandlerResult handleDrop(ReactContext context, Map<String, Object> args) {
		int nodeId = ((Number) args.get("nodeId")).intValue();
		// Note: Drop data must be provided by the client in the args map.
		// Full DnD integration is deferred to a later task.
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles context menu requests from the client.
	 */
	@ReactCommandHandler("contextMenu")
	public HandlerResult handleContextMenu(ReactContext context, Map<String, Object> args) {
		String contextInfo = (String) args.get("contextInfo");
		Menu menu = createContextMenu(contextInfo);
		if (menu != null) {
			// TODO: Send context menu to client via SSE state update.
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a context menu for the given context info.
	 *
	 * @param contextInfo
	 *        The context info identifying the target node (typically a node ID).
	 * @return the context menu, or {@code null} if no menu is available.
	 */
	public Menu createContextMenu(String contextInfo) {
		Widget node = (Widget) _graphScope.resolveOrFail(Integer.parseInt(contextInfo));

		if (node instanceof ContextMenu contextMenu) {
			Object userObject = node.getUserObject();

			DiagramContextMenuProviderSPI menuProvider = (DiagramContextMenuProviderSPI) contextMenu.getMenuProvider();

			if (!menuProvider.hasContextMenu(userObject)) {
				return null;
			}

			return menuProvider.getContextMenu(userObject, userObject);
		}

		Object userObject;
		if (_diagram.getSelection().contains(node)) {
			// A context menu opened on a selected item means that the operation targets the whole
			// selection.
			List<Object> selectedUserObjects = _diagram.getSelection().stream()
				.map(Widget::getUserObject)
				.filter(Objects::nonNull)
				.toList();

			// Make sure not to use collections as user objects for single-select diagrams.
			userObject = _diagram.isMultiSelect()
				? selectedUserObjects
				: CollectionUtil.getFirst(selectedUserObjects);
		} else {
			userObject = node.getUserObject();
			if (userObject == null) {
				return null;
			}
		}

		return _contextMenuProvider.getContextMenu(node, userObject);
	}

	/**
	 * Applies the changes the client made to the diagram, among them the selection it displays.
	 */
	void processUpdate(String patch) throws IOException {
		if (_graphScope == null) {
			// The diagram the patch was made against is not displayed any more.
			return;
		}
		JsonReader json = new JsonReader(new StringR(patch));
		_graphScope.applyChanges(json);

		notifySelectionChanged();
	}

	/**
	 * Processing click events sent from the client-side.
	 */
	public void processClick(int nodeId, Set<MouseButton> buttons) {
		ClickTarget node = (ClickTarget) _graphScope.resolveOrFail(nodeId);
		ClickHandler clickHandler = node.getClickHandler();
		if (clickHandler != null) {
			clickHandler.onClick(node, buttons);
		}
	}

	/**
	 * Processing drop events sent from the client-side.
	 */
	public void processDrop(int nodeId, Object data) {
		DropRegion node = (DropRegion) _graphScope.resolveOrFail(nodeId);
		com.top_logic.react.flow.callback.DropHandler dropHandler = node.getDropHandler();
		if (dropHandler instanceof ServerDropHandler serverDrop) {
			// ServerDropHandler.onDrop requires DndData; full integration deferred.
			Logger.info("Drop on node " + nodeId + " deferred (DnD integration pending).",
				FlowDiagramControl.class);
		}
	}

}

class ExternalScope extends DefaultScope {

	Map<SharedGraphNode, Integer> _objectIds = new HashMap<>();

	/**
	 * Creates a {@link ExternalScope}.
	 */
	public ExternalScope(int totalParticipants, int participantId) {
		super(totalParticipants, participantId);
	}

	public void clear() {
		_objectIds.clear();
		index().clear();
	}

	@Override
	public int id(SharedGraphNode node) {
		Integer id = _objectIds.get(node);
		return id == null ? 0 : id.intValue();
	}

	@Override
	public void initId(SharedGraphNode node, int id) {
		_objectIds.put(node, Integer.valueOf(id));
	}

}

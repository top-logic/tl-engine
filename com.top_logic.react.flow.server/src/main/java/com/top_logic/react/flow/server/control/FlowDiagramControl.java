/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.control;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.react.flow.callback.ClickHandler;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.ContextMenu;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.MouseButton;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Widget;
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
 * {@link ReactCommand @ReactCommand}-annotated methods.
 * </p>
 */
public class FlowDiagramControl extends ReactControl {

	/** The React module identifier for the flow diagram component. */
	public static final String REACT_MODULE = "TLFlowDiagram";

	private Diagram _diagram;

	private DefaultScope _graphScope;

	private ContextMenuProvider _contextMenuProvider = NoContextMenuProvider.INSTANCE;

	private ViewChannel _selectionChannel;

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
	 * Sets a new {@link Diagram} to display.
	 */
	public void setModel(Diagram diagram) {
		_diagram = diagram;
		initDiagramState();
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

	/**
	 * Sets the {@link ViewChannel} to write the selected node's user object to when the diagram
	 * selection changes.
	 *
	 * @param channel
	 *        The channel to write selections to, or {@code null} to disable selection publishing.
	 */
	public void setSelectionChannel(ViewChannel channel) {
		_selectionChannel = channel;
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
			putState("diagram", "");
			return;
		}

		_graphScope = new ExternalScope(2, 0);
		StringW out = new StringW();
		try {
			_diagram.writeTo(_graphScope, new JsonWriter(out));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		putState("diagram", out.toString());
	}

	/**
	 * Pushes pending diagram changes to the client via SSE.
	 *
	 * <p>
	 * Call this method after making server-side changes to the diagram model. The msgbuf patch is
	 * serialized and sent as a {@code "diagramPatch"} state update.
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
			putState("diagramPatch", out.toString());
		}
	}

	/**
	 * Handles msgbuf patch updates sent from the client.
	 */
	@ReactCommand("update")
	public HandlerResult handleUpdate(ReactContext context, Map<String, Object> args) {
		String patch = (String) args.get("patch");
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
	@ReactCommand("dispatchClick")
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
	@ReactCommand("dispatchDrop")
	public HandlerResult handleDrop(ReactContext context, Map<String, Object> args) {
		int nodeId = ((Number) args.get("nodeId")).intValue();
		// Note: Drop data must be provided by the client in the args map.
		// Full DnD integration is deferred to a later task.
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles context menu requests from the client.
	 */
	@ReactCommand("contextMenu")
	public HandlerResult handleContextMenu(ReactContext context, Map<String, Object> args) {
		String contextInfo = (String) args.get("contextInfo");
		Menu menu = createContextMenu(contextInfo);
		if (menu != null) {
			// TODO: Send context menu to client via SSE state update.
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles selection changes sent from the client.
	 *
	 * <p>
	 * The client sends the IDs of the currently selected nodes. The server resolves each ID to its
	 * {@link Widget} and extracts the user object from the {@link SelectableBox}. The result is
	 * written to the configured selection channel.
	 * </p>
	 */
	@ReactCommand("selection")
	public HandlerResult handleSelection(ReactContext context, Map<String, Object> args) {
		if (_selectionChannel == null || _graphScope == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		@SuppressWarnings("unchecked")
		List<Number> nodeIds = (List<Number>) args.get("nodeIds");
		if (nodeIds == null || nodeIds.isEmpty()) {
			_selectionChannel.set(null);
			return HandlerResult.DEFAULT_RESULT;
		}

		List<Object> selectedUserObjects = nodeIds.stream()
			.map(id -> _graphScope.resolveOrFail(id.intValue()))
			.filter(node -> node instanceof Widget)
			.map(node -> ((Widget) node).getUserObject())
			.filter(Objects::nonNull)
			.toList();

		if (selectedUserObjects.isEmpty()) {
			_selectionChannel.set(null);
		} else if (selectedUserObjects.size() == 1) {
			_selectionChannel.set(selectedUserObjects.get(0));
		} else {
			_selectionChannel.set(selectedUserObjects);
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

	void processUpdate(String patch) throws IOException {
		JsonReader json = new JsonReader(new StringR(patch));
		_graphScope.applyChanges(json);

		updateSelectionChannel();
	}

	private void updateSelectionChannel() {
		if (_selectionChannel == null || _diagram == null) {
			return;
		}

		List<Object> selectedUserObjects = _diagram.getSelection().stream()
			.filter(w -> w instanceof SelectableBox)
			.filter(w -> ((SelectableBox) w).isSelected())
			.map(Widget::getUserObject)
			.filter(Objects::nonNull)
			.toList();

		if (selectedUserObjects.isEmpty()) {
			_selectionChannel.set(null);
		} else if (selectedUserObjects.size() == 1) {
			_selectionChannel.set(selectedUserObjects.get(0));
		} else {
			_selectionChannel.set(selectedUserObjects);
		}
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

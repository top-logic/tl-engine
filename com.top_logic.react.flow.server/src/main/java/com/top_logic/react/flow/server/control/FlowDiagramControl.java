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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.react.flow.callback.ClickHandler;
import com.top_logic.react.flow.control.FlowControlCommon;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.MouseButton;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.SelectionUtil;
import com.top_logic.react.flow.operations.WidgetTraversal;
import com.top_logic.react.flow.server.handler.ServerDropHandler;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;
import de.haumacher.msgbuf.observer.Listener;

/**
 * {@link ReactControl} for displaying flow diagrams.
 *
 * <p>
 * This control serializes a {@link Diagram} model as initial React state and uses SSE to push
 * incremental msgbuf patches to the client. Client commands (click, drop, update, context menu) are
 * received via {@link ReactCommandHandler @ReactCommandHandler}-annotated methods.
 * </p>
 *
 * <p>
 * The diagram has no context menu of its own: a diagram inside a <code>&lt;context-menu&gt;</code>
 * of the view layer shows the commands of that element. Before the menu opens, the client reports
 * the element the menu was opened on ({@link FlowControlCommon#CMD_CONTEXT_MENU}), and the control
 * adjusts the selection the menu targets, see {@link #selectForContextMenu(Number)}.
 * </p>
 */
public class FlowDiagramControl extends ReactControl implements FlowControlCommon {

	/** The React module identifier for the flow diagram component. */
	public static final String REACT_MODULE = "TLFlowDiagram";

	/** State key holding the serialized {@link Diagram} the client mounts on. */
	private static final String DIAGRAM_STATE = "diagram";

	/** State key holding a msgbuf patch of the server-side changes to the displayed diagram. */
	private static final String DIAGRAM_PATCH_STATE = "diagramPatch";

	/**
	 * State key holding whether the client shows the diagram of the {@link #DIAGRAM_STATE} in the
	 * section (position and scale) it has shown so far, see {@link #setModel(Diagram, boolean)}.
	 */
	protected static final String KEEP_VIEW_STATE = "keepView";

	/**
	 * Notified when the user objects of the selected diagram elements change.
	 */
	public interface SelectionListener {

		/**
		 * Called after the selection changed, with the user objects of the elements now selected.
		 *
		 * <p>
		 * A listener refuses the change by throwing a {@link ChannelVetoException} - the unsaved
		 * changes of a form the selection would replace block it. The refusal unwinds through
		 * whoever changed the selection. A selection the user made in the diagram is restored to the
		 * one displayed before, both in the control and in the client, without telling the
		 * listeners again, and the veto is rethrown; clicking the same element again asks again.
		 * A listener notified before the refusing one keeps what it was told.
		 * </p>
		 *
		 * @param userObjects
		 *        The user objects of the elements now selected.
		 * @param replacing
		 *        Whether the user replaced the whole selection by a plain click in the diagram (see
		 *        {@link Diagram#isIncrementalSelection()}), so that also selected objects the diagram
		 *        does not display are given up. <code>false</code> for a click with a modifier key
		 *        and for an update not touching the selection.
		 */
		void selectionChanged(Set<Object> userObjects, boolean replacing);
	}

	/**
	 * Notified when the displayed {@link Diagram} was replaced.
	 */
	public interface ModelListener {

		/**
		 * Called after {@link FlowDiagramControl#setModel(Diagram, boolean)} carried the selection over to
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

	/**
	 * Whether the client shows the current {@link #_diagram} in the section shown before it was
	 * set, see {@link #setModel(Diagram, boolean)}.
	 */
	private boolean _keepView;

	private GraphScope _graphScope;

	/**
	 * The next ID a {@link GraphScope} of this control assigns, an ID of the server (odd) never
	 * assigned before.
	 */
	private int _nextGraphId = 1;

	/**
	 * Whether the diagram changed since it was last serialized to the {@link #DIAGRAM_STATE}.
	 */
	private boolean _serializationStale;

	private final List<SelectionListener> _selectionListeners = new CopyOnWriteArrayList<>();

	private final List<ModelListener> _modelListeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link FlowDiagramControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param diagram
	 *        The diagram model to display, may be {@code null}.
	 */
	public FlowDiagramControl(ReactContext context, Diagram diagram) {
		super(context, diagram, REACT_MODULE);

		_diagram = diagram;
	}

	@Override
	public Diagram getModel() {
		return _diagram;
	}

	/**
	 * A diagram is addressed by its role alone: its {@link #getModel() model} is the whole displayed
	 * content, which neither names the diagram nor stays the same while it is displayed.
	 */
	@Override
	public String scriptingName() {
		return "";
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
	 * <p>
	 * The client fits the new diagram into the control, as when it is shown the first time. Use
	 * {@link #setModel(Diagram, boolean)} to keep the section the user has panned and zoomed to.
	 * </p>
	 *
	 * @param diagram
	 *        The diagram to display, may be <code>null</code> to display nothing.
	 */
	public void setModel(Diagram diagram) {
		setModel(diagram, false);
	}

	/**
	 * Displays the given {@link Diagram} instead of the one displayed so far, optionally in the
	 * section the user has panned and zoomed to.
	 *
	 * <p>
	 * The selection is carried over as described for {@link #setModel(Diagram)}.
	 * </p>
	 *
	 * @param diagram
	 *        The diagram to display, may be <code>null</code> to display nothing.
	 * @param keepView
	 *        Whether the client shows the new diagram in the section (position and scale) it has
	 *        shown so far, e.g. when the diagram is rebuilt for a change of the displayed objects.
	 *        The section is not adapted to the new content: an element outside of it stays
	 *        outside, also when it is selected. <code>false</code> fits the new diagram into the
	 *        control, e.g. when it displays another object. A diagram with a
	 *        {@link Diagram#isFixedView() fixed view} ignores the flag.
	 */
	public void setModel(Diagram diagram, boolean keepView) {
		Set<Object> selectedUserObjects = getSelectedUserObjects();

		_diagram = diagram;
		_keepView = keepView;
		// The scope belongs to the diagram it serialized, which nothing displays any more: the
		// markings below are part of the new diagram's serialization, not of a patch to the old
		// one.
		releaseScope(_graphScope);
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

		notifySelectionChanged(true);
	}

	/**
	 * Whether a selectable element of the diagram carries the given user object.
	 */
	public boolean hasElementFor(Object userObject) {
		if (_diagram == null || userObject == null) {
			return false;
		}
		boolean[] found = { false };
		WidgetTraversal.visitAll(_diagram, widget -> {
			if (!found[0] && SelectionUtil.isSelectable(widget) && userObject.equals(widget.getUserObject())) {
				found[0] = true;
			}
		});
		return found[0];
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
		List<Widget> selected = mark(widget -> {
			Object userObject = widget.getUserObject();
			return userObject != null && SelectionUtil.isSelectable(widget) && userObjects.contains(userObject);
		});
		if (!selected.equals(_diagram.getSelection())) {
			_diagram.setSelection(selected);
		}
	}

	/**
	 * Makes the given elements the selection of the diagram, in the given order, and marks exactly
	 * them as selected, without telling anybody about it.
	 */
	private void selectExactly(List<Widget> selection) {
		mark(selection::contains);
		if (!selection.equals(_diagram.getSelection())) {
			_diagram.setSelection(new ArrayList<>(selection));
		}
	}

	/**
	 * Marks the diagram elements the given criterion accepts as selected and every other one as
	 * unselected.
	 *
	 * @return The marked elements, in the order of the diagram.
	 */
	private List<Widget> mark(Predicate<Widget> select) {
		List<Widget> selected = new ArrayList<>();
		WidgetTraversal.visitAll(_diagram, widget -> {
			boolean selectWidget = select.test(widget);
			if (selectWidget) {
				selected.add(widget);
			}
			if (SelectionUtil.isSelected(widget) != selectWidget) {
				// Only a widget whose marking actually changes is written, so that the patch
				// carries the elements that change their appearance and no others.
				SelectionUtil.setSelected(widget, selectWidget);
			}
		});
		return selected;
	}

	/**
	 * Tells the {@link SelectionListener}s what the diagram displays as selected.
	 */
	private void notifySelectionChanged(boolean replacing) {
		Set<Object> userObjects = getSelectedUserObjects();
		for (SelectionListener listener : _selectionListeners) {
			listener.selectionChanged(userObjects, replacing);
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

	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();

		initDiagramState();
	}

	@Override
	protected void onCleanup() {
		releaseScope(_graphScope);
		_graphScope = null;

		super.onCleanup();
	}

	/**
	 * Serializes the current diagram model and stores it as initial React state, together with the
	 * {@link #KEEP_VIEW_STATE}, so that the client receives both in the same update.
	 */
	private void initDiagramState() {
		Object token = beginUpdate();
		try {
			putState(KEEP_VIEW_STATE, Boolean.valueOf(_keepView));
			putDiagramState();
		} finally {
			commitUpdate(token);
		}
	}

	private void putDiagramState() {
		_serializationStale = false;
		if (_diagram == null) {
			putState(DIAGRAM_STATE, "");
			return;
		}

		// Serializing the displayed diagram again keeps the IDs its elements have: the client holds
		// them already, and an update it sent before receiving this serialization names the same
		// elements. The scope of a replaced diagram is gone by now, so its elements get new IDs.
		GraphScope before = _graphScope;
		GraphScope scope = new GraphScope(before);
		StringW out = new StringW();
		try {
			_diagram.writeTo(scope, new JsonWriter(out));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} finally {
			scope.serialized();
		}
		releaseScope(before);
		_graphScope = scope;
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
	 *
	 * <p>
	 * The patch is {@link #sendTransientState(String, Object) no part of the state}: it changes the
	 * diagram the client holds at the moment it receives it. Kept in the state, the full state the
	 * client receives when it connects again would apply it once more to the diagram already
	 * containing it. The changed diagram is serialized again instead, see
	 * {@link #refreshDiagramState()}.
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
			sendTransientState(DIAGRAM_PATCH_STATE, out.toString());

			_serializationStale = true;
			refreshDiagramState();
		}
	}

	/**
	 * Serializes the diagram again after it changed, recording the serialization in the
	 * {@link #DIAGRAM_STATE} without sending it.
	 *
	 * <p>
	 * The client receives the changes as patches. The serialization is for the full state the
	 * client receives whenever it needs one: when it connects again, or when it polls after missing
	 * events, the full state replaces the patches it missed. Such a full state is read outside of
	 * the control's processing, so the serialization has to be current at all times. It keeps the
	 * IDs of the elements, so that it describes the diagram the client holds, and updates the
	 * client sends before receiving it remain valid.
	 * </p>
	 */
	private void refreshDiagramState() {
		if (!_serializationStale || _graphScope == null) {
			// Unchanged, or not serialized yet: the rendering serializes the diagram anyway.
			return;
		}
		updateStateSilently(this::initDiagramState);
	}

	/**
	 * Handles msgbuf patch updates sent from the client.
	 */
	@ReactCommandHandler(CMD_UPDATE)
	public void handleUpdate(ReactContext context, Map<String, Object> args) {
		String patch = (String) args.get(ARG_PATCH);
		try {
			processUpdate(patch);
		} catch (IOException ex) {
			Logger.error("Failed to update diagram.", ex, FlowDiagramControl.class);
		}
	}

	/**
	 * Handles click events sent from the client.
	 */
	@ReactCommandHandler(CMD_DISPATCH_CLICK)
	public void handleClick(ReactContext context, Map<String, Object> args) {
		@SuppressWarnings("unchecked")
		List<String> buttonNames = (List<String>) args.get(ARG_MOUSE_BUTTONS);
		Set<MouseButton> buttons =
			buttonNames.stream().map(n -> MouseButton.valueOf(n)).collect(Collectors.toSet());
		int nodeId = ((Number) args.get(ARG_NODE_ID)).intValue();
		processClick(nodeId, buttons);
	}

	/**
	 * Handles drop events sent from the client.
	 */
	@ReactCommandHandler(CMD_DISPATCH_DROP)
	public void handleDrop(ReactContext context, Map<String, Object> args) {
		int nodeId = ((Number) args.get(ARG_NODE_ID)).intValue();
		// Note: Drop data must be provided by the client in the args map.
		// Full DnD integration is deferred to a later task.
	}

	/**
	 * Handles the report of the client that a context menu is about to open on the diagram.
	 *
	 * @see #selectForContextMenu(Number)
	 */
	@ReactCommandHandler(CMD_CONTEXT_MENU)
	public void handleContextMenu(ReactContext context, Map<String, Object> args) {
		selectForContextMenu((Number) args.get(ARG_NODE_ID));
	}

	/**
	 * Makes the selection the target of a context menu opened on the given element.
	 *
	 * <p>
	 * The menu acts on the selection, as for a table:
	 * </p>
	 * <ul>
	 * <li>An element that is not selected is selected alone, replacing the whole selection - also
	 * the selected objects the diagram does not display.</li>
	 * <li>An element that is selected keeps the selection, also a selection of several elements,
	 * and the menu acts on all of them.</li>
	 * <li>The empty area of the diagram (no element) clears the selection, so that only commands
	 * without an object apply.</li>
	 * </ul>
	 *
	 * <p>
	 * A changed selection is shown in the client and reported to the {@link SelectionListener}s like
	 * a plain click. A change they refuse with a {@link ChannelVetoException} is taken back, in the
	 * control and in the client, and the veto is rethrown.
	 * </p>
	 *
	 * @param nodeId
	 *        The ID of the selectable element the menu was opened on, <code>null</code> for the
	 *        empty area of the diagram.
	 */
	public void selectForContextMenu(Number nodeId) {
		if (_diagram == null) {
			return;
		}
		List<Widget> selection;
		if (nodeId == null) {
			selection = List.of();
		} else {
			if (!(resolve(nodeId.intValue()) instanceof Widget node) || !SelectionUtil.isSelectable(node)) {
				// The menu was opened in a diagram the control no longer displays.
				return;
			}
			if (SelectionUtil.isSelected(node)) {
				// The menu acts on the selection the element is part of.
				return;
			}
			selection = List.of(node);
		}

		List<Widget> selectionBefore = new ArrayList<>(_diagram.getSelection());
		selectExactly(selection);
		pushDiagramChanges();
		try {
			notifySelectionChanged(true);
		} catch (ChannelVetoException ex) {
			selectExactly(selectionBefore);
			pushDiagramChanges();
			throw ex;
		}
	}

	/**
	 * Applies the changes the client made to the diagram, among them the selection it displays.
	 *
	 * <p>
	 * A selection change the {@link SelectionListener}s refuse with a {@link ChannelVetoException}
	 * is taken back: the diagram selects what it selected before the update, sends that to the
	 * client, which still marks the refused selection, and rethrows the veto.
	 * </p>
	 */
	void processUpdate(String patch) throws IOException {
		if (_graphScope == null) {
			// The diagram the patch was made against is not displayed any more.
			return;
		}
		JsonReader json = new JsonReader(new StringR(patch));

		// Whether the patch reports a plain click replacing the selection.
		Diagram diagram = _diagram;
		List<Widget> selectionBefore = new ArrayList<>(diagram.getSelection());
		boolean[] replacing = { false };
		Listener clickKind = (obj, property, value) -> {
			if (obj == diagram && Diagram.INCREMENTAL_SELECTION__PROP.equals(property)) {
				replacing[0] = !((Boolean) value).booleanValue();
			}
		};
		diagram.registerListener(clickKind);
		try {
			_graphScope.applyChanges(json);
		} catch (IllegalArgumentException ex) {
			// The patch names elements this scope does not know: it was made for a diagram the
			// control replaced while the patch was on its way.
			Logger.info("Dropped a client update for a replaced diagram: " + ex.getMessage(),
				FlowDiagramControl.class);
			return;
		} finally {
			diagram.unregisterListener(clickKind);
		}
		_serializationStale = true;

		try {
			notifySelectionChanged(replacing[0]);
		} catch (ChannelVetoException ex) {
			selectExactly(selectionBefore);
			pushDiagramChanges();
			throw ex;
		} finally {
			refreshDiagramState();
		}
	}

	/**
	 * Processing click events sent from the client-side.
	 */
	public void processClick(int nodeId, Set<MouseButton> buttons) {
		if (!(resolve(nodeId) instanceof ClickTarget node)) {
			// A click into a diagram the control no longer displays.
			return;
		}
		ClickHandler clickHandler = node.getClickHandler();
		if (clickHandler != null) {
			clickHandler.onClick(node, buttons);
		}
	}

	/**
	 * Processing drop events sent from the client-side.
	 */
	public void processDrop(int nodeId, Object data) {
		if (!(resolve(nodeId) instanceof DropRegion node)) {
			// A drop into a diagram the control no longer displays.
			return;
		}
		com.top_logic.react.flow.callback.DropHandler dropHandler = node.getDropHandler();
		if (dropHandler instanceof ServerDropHandler serverDrop) {
			// ServerDropHandler.onDrop requires DndData; full integration deferred.
			Logger.info("Drop on node " + nodeId + " deferred (DnD integration pending).",
				FlowDiagramControl.class);
		}
	}

	/**
	 * Stops the given scope from observing the diagram it serialized.
	 */
	private static void releaseScope(GraphScope scope) {
		if (scope != null) {
			scope.release();
		}
	}

	/**
	 * The element of the displayed diagram with the given ID, <code>null</code> if there is none -
	 * the client refers to a diagram the control no longer displays.
	 */
	private SharedGraphNode resolve(int id) {
		return _graphScope == null ? null : _graphScope.resolve(id);
	}

	/**
	 * The {@link DefaultScope} a serialization of the displayed {@link Diagram} is shared with the
	 * client through.
	 *
	 * <p>
	 * Each serialization of a diagram starts a new scope. The scopes of a control draw their IDs from
	 * one sequence, so that no ID a scope assigns names an element of another diagram: a patch or
	 * command the client made for a diagram the control no longer displays names nothing in the
	 * displayed one, instead of the element that happens to have the same position in it. A scope
	 * serializing the same diagram again takes over the IDs the previous scope assigned, so that
	 * the same diagram is serialized the same way as long as it does not change.
	 * </p>
	 */
	private final class GraphScope extends DefaultScope {

		private final Map<SharedGraphNode, Integer> _objectIds = new HashMap<>();

		/**
		 * The scope whose IDs this one takes over while serializing, <code>null</code> afterwards.
		 */
		private GraphScope _previous;

		/**
		 * Creates a {@link GraphScope} for the server, the participant 0 of the two sharing the
		 * diagram.
		 *
		 * @param previous
		 *        The scope of an earlier serialization of the same diagram, whose IDs are taken
		 *        over, or <code>null</code> to assign new IDs.
		 */
		GraphScope(GraphScope previous) {
			super(2, 0);
			_previous = previous;
		}

		@Override
		public void writeRefOrData(JsonWriter out, SharedGraphNode node) throws IOException {
			if (_previous != null && id(node) == 0) {
				int id = _previous.id(node);
				if (id != 0) {
					enter(node, id);
					node.writeData(this, out, id);
					return;
				}
			}
			super.writeRefOrData(out, node);
		}

		/**
		 * Called when the serialization is complete: the IDs of the previous scope are not needed
		 * any more.
		 */
		void serialized() {
			_previous = null;
		}

		/**
		 * Stops observing the elements this scope serialized.
		 */
		void release() {
			for (SharedGraphNode node : index().values()) {
				node.unregisterListener(this);
			}
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

		@Override
		public int newId() {
			int result = _nextGraphId;
			// Skip the IDs of the client, the other participant.
			_nextGraphId += 2;
			return result;
		}

		/**
		 * The element with the given ID, <code>null</code> if this scope has none, e.g. because the
		 * ID belongs to a diagram the control no longer displays.
		 */
		SharedGraphNode resolve(int id) {
			return index().get(Integer.valueOf(id));
		}

	}

}

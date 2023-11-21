/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;

import com.top_logic.ajax.client.bal.BAL;
import com.top_logic.ajax.client.bal.Coordinates;
import com.top_logic.ajax.client.control.AbstractJSControl;
import com.top_logic.ajax.client.control.JSControl;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.client.diagramjs.binding.UmlJS;
import com.top_logic.client.diagramjs.command.CommandExecutionPhase;
import com.top_logic.client.diagramjs.command.CommandStack;
import com.top_logic.client.diagramjs.core.Canvas;
import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.event.DragEventNames;
import com.top_logic.client.diagramjs.event.EventBus;
import com.top_logic.client.diagramjs.event.GeneralEventNames;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.Root;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.remote.shared.ScopeEvent;
import com.top_logic.common.remote.shared.ScopeListener;
import com.top_logic.common.remote.update.ChangeIO;
import com.top_logic.common.remote.update.Changes;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.diagramjs.client.ajax.GraphDropArguments;
import com.top_logic.graph.diagramjs.client.ajax.UpdateGraphCommandArguments;
import com.top_logic.graph.diagramjs.client.service.event.ClickEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.CreateClassEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.CreateClassPropertyEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.CreateConnectionEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.CreateEnumerationEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.DeleteGraphPartEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.ElementVisibilityEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.ElementsMoveEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.GoToDefinitionEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.ShapeResizeEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.UpdatedWaypointsEventHandler;
import com.top_logic.graph.diagramjs.client.service.event.listener.DragOverEventListener;
import com.top_logic.graph.diagramjs.client.service.event.listener.DropEventListener;
import com.top_logic.graph.diagramjs.client.service.scope.DiagramJSGraphScope;
import com.top_logic.graph.diagramjs.client.service.scope.listener.DefaultGraphScopeListener;

/**
 * {@link JSControl} for a diagramJS graph display.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphControl extends AbstractJSControl implements ScopeListener, GeneralEventNames {

	private static final String GRAPH_SUFFIX = "-graph";

	private static final String UMLJS_CONTAINER_SUFFIX = "-umljs-container";

	private static final String DROP_DATA_FORMAT = "text";

	private static final String DOM_DRAG_OVER_EVENT_NAME = DragOverEvent.getType().getName();

	private static final String DOM_DROP_EVENT_NAME = DropEvent.getType().getName();

	private static final int PALETTE_OFFSET_Y = -20;

	private static final int PALLETE_OFFSET_X = -90;

	Diagram _diagram;

	private DiagramJSGraphScope _scope;

	private Timer _updateTimer;

	Root _root;

	private Element _rootElement;

	/**
	 * Creates a {@link DiagramJSGraphControl}.
	 */
	public DiagramJSGraphControl(String id) {
		super(id);

		_rootElement = Document.get().getElementById(getParentID(id));
		_diagram = UmlJS.createDiagram(_rootElement, getUmlJSContainerID(id));
		_scope = new DiagramJSGraphScope(DiagramJSHandleFactory.INSTANCE);
		_updateTimer = createUpdateTimer();
		_root = createDisplayRootNode();
	}

	private String getUmlJSContainerID(String id) {
		return id + UMLJS_CONTAINER_SUFFIX;
	}

	private String getParentID(String id) {
		return id + GRAPH_SUFFIX;
	}

	@Override
	public void init(Object[] args) {
		checkArguments(args);

		_scope.addListener(new DefaultGraphScopeListener(_diagram));
		initSharedGraphModel((String) args[0]);
		setShowHiddenElements((boolean) args[1]);
		_scope.addListener(this);

		registerDisplayGraphEventHandlers(_diagram.getEventBus());

		addEventListeners();

		setViewbox();
	}

	private void setViewbox() {
		List<Base> selectedDisplayGraphParts = getSelectedDisplayGraphParts();

		if (selectedDisplayGraphParts.isEmpty()) {
			moveDiagramBesidesPalette();
		} else {
			_diagram.getSelection().select(selectedDisplayGraphParts, false);

			Canvas canvas = _diagram.getCanvas();

			Dimension size = canvas.getSize();

			Position center = DiagramJSObjectUtil.getCanvasCenter(selectedDisplayGraphParts, canvas);

			canvas.setViewbox(DiagramJSObjectUtil.createBounds(center, size));
		}
	}

	private List<Base> getSelectedDisplayGraphParts() {
		return _scope.getGraphModel().getSelectedGraphParts().stream()
			.map(graphPart -> (Base) graphPart.getTag())
			.collect(Collectors.toList());
	}

	private void moveDiagramBesidesPalette() {
		Canvas canvas = _diagram.getCanvas();

		Bounds viewbox = canvas.getViewbox();

		viewbox.setX(PALLETE_OFFSET_X);
		viewbox.setY(PALETTE_OFFSET_Y);

		canvas.setViewbox(viewbox);
	}

	private void addEventListeners() {
		enableEvents(_rootElement, DOM_DROP_EVENT_NAME, DOM_DRAG_OVER_EVENT_NAME);

		setEventListeners();
	}

	private void setEventListeners() {
		Map<String, EventListener> listeners = new HashMap<>();

		listeners.put(DOM_DROP_EVENT_NAME, new DropEventListener(this));
		listeners.put(DOM_DRAG_OVER_EVENT_NAME, DragOverEventListener.INSTANCE);

		Event.setEventListener(_rootElement, new DispatchingEventListener(listeners));
	}

	private Position getEventPosition(NativeEvent event) {
		Bounds viewbox = getViewbox();

		Coordinates coordinates = BAL.relativeMouseCoordinates(event, getRootElement());
		Position position = JavaScriptObject.createObject().cast();

		position.setX(coordinates.getX() + viewbox.getX());
		position.setY(coordinates.getY() + viewbox.getY());

		return position;
	}

	/**
	 * Triggers server side drop command with the drop position.
	 */
	public void onDrop(Event event) {
		Position eventPosition = getEventPosition(event.cast());

		GraphDropArguments arguments = JavaScriptObject.createObject().cast();

		arguments.setX(eventPosition.getX());
		arguments.setY(eventPosition.getY());
		arguments.setData(event.getDataTransfer().getData(DROP_DATA_FORMAT));

		sendCommand(GraphControlCommon.GRAPH_DROP_COMMAND, arguments);

		event.stopPropagation();
		event.preventDefault();
	}

	private Bounds getViewbox() {
		return _diagram.getCanvas().getViewbox();
	}

	private static void enableEvents(Element element, String... eventNames) {
		Objects.requireNonNull(element);

		if (eventNames == null) {
			return;
		}

		for (String eventName : eventNames) {
			DOM.sinkBitlessEvent(element, eventName);
		}
	}

	private Timer createUpdateTimer() {
		return new Timer() {
			@Override
			public void run() {
				sendChangesToServer();
			}
		};
	}

	void sendChangesToServer() {
		UpdateGraphCommandArguments arguments = JavaScriptObject.createObject().cast();
		arguments.setGraphUpdate(ChangeIO.writeChanges(popChanges()));

		sendCommand(GraphControlCommon.UPDATE_SERVER_GRAPH_COMMAND, arguments);
	}

	private void registerDisplayGraphEventHandlers(EventBus eventBus) {
		String id = getId();

		addEventBusHandlers(eventBus, id);
		addCommandStackHandlers(id);
	}

	private void addCommandStackHandlers(String id) {
		CommandStack commandStack = _diagram.getCommandStack();

		commandStack.addCommandInterceptor(getUpdatedWaypointsEventNames(), CommandExecutionPhase.POST_EXECUTED,
			new UpdatedWaypointsEventHandler());
		commandStack.addCommandInterceptor(SHAPE_RESIZE_EVENT, CommandExecutionPhase.POST_EXECUTED,
			new ShapeResizeEventHandler());
		commandStack.addCommandInterceptor(ELEMENTS_MOVE_EVENT, CommandExecutionPhase.POST_EXECUTED,
			new ElementsMoveEventHandler());
		commandStack.addCommandInterceptor(ELEMENTS_VISIBILITY_EVENT, CommandExecutionPhase.POST_EXECUTED,
			new ElementVisibilityEventHandler(id));
	}

	private void addEventBusHandlers(EventBus eventBus, String id) {
		eventBus.addEventHandler(ELEMENT_CLICKED_EVENT, new ClickEventHandler(_scope.getGraphModel()));
		eventBus.addEventHandler(DragEventNames.DRAG_CONNECT_END_EVENT, new CreateConnectionEventHandler(id));
		eventBus.addEventHandler(CREATE_CLASS_PROPERTY_EVENT, new CreateClassPropertyEventHandler(id));
		eventBus.addEventHandler(CREATE_CLASS_EVENT, new CreateClassEventHandler(id));
		eventBus.addEventHandler(CREATE_ENUMERATION_EVENT, new CreateEnumerationEventHandler(id));
		eventBus.addEventHandler(DELETE_ELEMENT_EVENT, new DeleteGraphPartEventHandler(id));
		eventBus.addEventHandler(ELEMENT_GOTO_EVENT, new GoToDefinitionEventHandler(id));
	}

	private List<String> getUpdatedWaypointsEventNames() {
		return Arrays.asList(UPDATET_WAYPOINTS_EVENT, CONNECTION_LAYOUT_EVENT);
	}

	@Override
	public void handleObjectScopeEvent(ScopeEvent event) {
		_updateTimer.schedule(150);
	}

	private Changes popChanges() {
		return _scope.popChanges();
	}

	private Root createDisplayRootNode() {
		Root root = _diagram.getElementFactory().createRoot();
		_diagram.getCanvas().setRootElement(root);

		return root;
	}

	private void checkArguments(Object[] args) {
		if (args.length < 2) {
			GWT.log("Two arguments are expected."
				+ "The first argument represents the graph status in json."
				+ "The second argument is a flag to indicate if hidden elements should be displayed.");
		}
	}

	void addShapesToCanvas(Canvas canvas, Collection<Label> shapes, Base parent) {
		shapes.stream().forEach(shape -> canvas.addShape(shape, parent));
	}

	private void initSharedGraphModel(String jsonGraphModel) {
		try (JsonReader reader = new JsonReader(new StringR(jsonGraphModel))) {
			_scope.readFrom(reader);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed parsing graph data: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void invoke(String command, Object[] args) {
		switch (command) {
			case GraphControlCommon.UPDATE_CLIENT_GRAPH_COMMAND:
				String updateJSON = (String) args[0];
				Changes changes = ChangeIO.readChanges(updateJSON);
				_scope.update(changes);
				break;
			case GraphControlCommon.SHOW_HIDDEN_ELEMENTS_COMMAND:
				setShowHiddenElements(Boolean.parseBoolean((String) args[0]));
				break;
			default:
				throw new IllegalArgumentException("Command '" + command + "' not supported.");
		}
	}

	private void setShowHiddenElements(boolean showHiddenElements) {
		_diagram.getLayouter().setShowHiddenElements(showHiddenElements);
	}

	/**
	 * The top level DOM element for this graph.
	 */
	public Element getRootElement() {
		return _rootElement;
	}

	/**
	 * @see #getRootElement()
	 */
	public void setRootElement(Element rootElement) {
		_rootElement = rootElement;
	}

}

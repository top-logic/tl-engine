/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Timer;

import com.top_logic.ajax.client.control.AbstractJSControl;
import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.model.Identified;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.SVGDropEvent;
import com.top_logic.graphic.flow.callback.DiagramContext;
import com.top_logic.graphic.flow.control.JSDiagramControlCommon;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.DropRegion;
import com.top_logic.graphic.flow.data.MouseButton;
import com.top_logic.graphic.flow.data.Widget;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.observer.Observable;
import elemental2.core.JsArray;
import elemental2.core.JsMath;
import elemental2.core.JsNumber;
import elemental2.dom.DomGlobal;
import elemental2.dom.DragEvent;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.Image;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.ResizeObserver;
import elemental2.dom.ResizeObserverCallback;
import elemental2.dom.ResizeObserverEntry;
import elemental2.dom.Response;
import elemental2.dom.WheelEvent;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;

/**
 * Client-side logic of a flow diagram control.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSDiagramControl extends AbstractJSControl
		implements JSDiagramControlCommon, DiagramContext {

	private class Scope extends DefaultScope {

		private double _changeTimeout;

		private boolean _lazyRequest;

		private final double _lazyRequestID;

		/**
		 * Creates a new Scope.
		 */
		public Scope(int totalParticipants, int participantId) {
			super(totalParticipants, participantId);
			reset();
			_lazyRequestID = createLazyRequestID();
		}

		private native double createLazyRequestID() /*-{
			return $wnd.services.ajax.createLazyRequestID();
		}-*/;

		@Override
		public void afterChanged(Observable obj, String property) {
			super.afterChanged(obj, property);
			if (_processServerUpdate) {
				_dirtyNodes.add((SharedGraphNode) obj);
			} else {
				if (_lazyRequest && obj.equals(_diagram)) {
					_lazyRequest =
						Diagram.VIEW_BOX_HEIGHT__PROP.equals(property) ||
							Diagram.VIEW_BOX_WIDTH__PROP.equals(property) ||
							Diagram.VIEW_BOX_Y__PROP.equals(property) ||
							Diagram.VIEW_BOX_X__PROP.equals(property);
				} else {
					_lazyRequest = false;
				}

				if (_changeTimeout == 0) {
					_changeTimeout = DomGlobal.setTimeout(JSDiagramControl.this::onChange, 10, this);
				}
			}
		}

		public void reset() {
			_changeTimeout = 0;
			_lazyRequest = true;
		}

		boolean lazyRequestPossible() {
			return _lazyRequest;
		}

		double lazyRequestId() {
			return _lazyRequestID;
		}

	}

	private OMSVGDocument _svgDoc;

	private Element _control;

	private OMSVGSVGElement _svg;

	private RenderContext _renderContext = new JSRenderContext();

	private Scope _scope;

	private Diagram _diagram;

	final SubIdGenerator _nextId;

	private HTMLDivElement _zoomDisplay;

	private Timer hideZoomDisplay = null;

	int zoomLevel;

	float controlW;

	float controlH;

	float newCtrlW;

	float newCtrlH;

	double dragStartX, dragStartY;

	boolean draggingToPan;

	private OMSVGRect _viewbox;
	
	private ResizeObserver _observer;

	/** Flag to indicate that currently a server side triggered diagram update is applied. */
	boolean _processServerUpdate;

	/**
	 * The diagram nodes which are touched during a server side triggered diagram update.
	 * 
	 * @see #_processServerUpdate
	 */
	final Set<SharedGraphNode> _dirtyNodes = new HashSet<>();

	/**
	 * Creates a {@link JSDiagramControl}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 */
	public JSDiagramControl(String id) {
		super(id);
		_nextId = new SubIdGenerator(id);
	}

	@Override
	public void init(Object[] args) {
		super.init(args);

		HTMLDocument document = DomGlobal.document;
		_control = document.getElementById(getId());
		String contentUrl = _control.getAttribute(DATA_CONTENT_ATTR);

		_svgDoc = OMSVGParser.currentDocument();
		_svg = _svgDoc.getElementById(controlElement().getId() + SVG_ID_SUFFIX);

		Promise<Response> response = DomGlobal.window.fetch(contentUrl);
		response
			.then(data -> data.text())
			.then(json -> {
				if (json.isEmpty()) {
					// No data yet.
					return null;
				}
				try {
					_scope = new Scope(2, 1);
					Diagram diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(json)));
					diagram.setContext(this);

					diagram.layout(_renderContext);

					if (diagram.getViewBoxWidth() == 0) {
						diagram.setViewBoxWidth(_control.clientWidth);
					}
					if (diagram.getViewBoxHeight() == 0) {
						diagram.setViewBoxHeight(_control.clientHeight);
					}

					diagram.draw(svgBuilder());

					_diagram = diagram;

					_zoomDisplay = (HTMLDivElement) DomGlobal.document.createElement("div");
					_zoomDisplay.classList.add("zoomDisplay");
					_control.appendChild(_zoomDisplay);

					hideZoomDisplay = new Timer() {
						@Override
						public void run() {
							_zoomDisplay.classList.add("invisible");
						}
					};

					controlW = _control.clientWidth;
					controlH = _control.clientHeight;
					newCtrlW = controlW;
					newCtrlH = controlH;
					_viewbox = _svg.getViewBox().getBaseVal();
					calcZoomLevel();
				} catch (IOException ex) {
					DomGlobal.console.error("Failed to fetch diagram data: ", ex.getMessage());
				}
				return null;
			});

		draggingToPan = false;

		EventListener panningSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				if (draggingToPan) {
					DragEvent event = (DragEvent) evt;

					double dragDeltaX = dragStartX - event.clientX;
					double dragDeltaY = dragStartY - event.clientY;

					panSVG(dragDeltaX, dragDeltaY, false);

					dragStartX = event.clientX;
					dragStartY = event.clientY;
					event.stopImmediatePropagation();
					event.preventDefault();
				}
			}
		};

		EventListener startDraggingSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;
				dragStartX = event.clientX;
				dragStartY = event.clientY;

				Image img = (Image) DomGlobal.document.createElement("img");
				img.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=";
				event.dataTransfer.setDragImage(img, 0, 0);

				draggingToPan = true;

				DomGlobal.window.addEventListener("dragover", panningSVG);

				event.stopImmediatePropagation();
			}
		};

		EventListener dropSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				event.stopImmediatePropagation();
				event.preventDefault();
			}
		};

		EventListener endDraggingSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				draggingToPan = false;

				DomGlobal.window.removeEventListener("dragover", panningSVG);

				event.stopImmediatePropagation();
			}
		};

		EventListener zoomOrScrollSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				WheelEvent event = (WheelEvent) evt;
				if (event.ctrlKey) {
					double delta = event.deltaY == 0 ? event.deltaX : event.deltaY;
					double direction = JsMath.sign(delta);

					// Beim Herauszoomen (positives delta) muss der Zoom
					// den Faktor des vorherigen Levels nehmen, daher -10
					int zL = (direction < 0 ? zoomLevel : zoomLevel - 10);
					int level = JsMath.trunc(zL / 100);

					// Anteil für 10% Zoom: 2^x * -10, wobei x immer das aktuell volle 100% Level
					// ist
					double factor = direction / (JsMath.pow(2, level) * (-10));
					if (zL < 100) {
						factor = factor * 2;
					}
					zoomSVG(factor, event.offsetX, event.offsetY);
				} else if (event.shiftKey) {
					double scrollFactor = getWheelScrollFactor(evt);
					double deltaX = event.deltaY * scrollFactor;
					double deltaY = event.deltaX * scrollFactor;
					panSVG(deltaX, deltaY, false);
				} else {
					int scrollFactor = getWheelScrollFactor(evt);
					double deltaX = event.deltaX * scrollFactor;
					double deltaY = event.deltaY * scrollFactor;
					panSVG(deltaX, deltaY, false);
				}
				event.stopImmediatePropagation();
				event.preventDefault();
			}
		};
		
		EventListener resetZoom = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				KeyboardEvent event = (KeyboardEvent) evt;
				if (event.key == "0" && event.ctrlKey) {
					zoomSVG(1, 0, 0);
				}
			}
		};

		EventListener mouseEnter = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DomGlobal.window.addEventListener("keyup", resetZoom);
			}
		};

		EventListener mouseLeave = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DomGlobal.window.removeEventListener("keyup", resetZoom);
			}
		};

		_control.addEventListener("dragstart", startDraggingSVG);

		_control.addEventListener("drop", dropSVG);

		_control.addEventListener("dragend", endDraggingSVG);

		_control.addEventListener("wheel", zoomOrScrollSVG);

		_control.addEventListener("pointerenter", mouseEnter);

		_control.addEventListener("pointerleave", mouseLeave);

		ResizeObserverCallback resize = new ResizeObserverCallback() {
			@Override
			public Object onInvoke(JsArray<ResizeObserverEntry> p0, ResizeObserver p1) {
				newCtrlW = _control.clientWidth;
				newCtrlH = _control.clientHeight;
				if (newCtrlW != controlW || newCtrlH != controlH) {
					zoomSVG(0, 0, 0);
				}
				return null;
			}
		};

		_observer = new ResizeObserver(resize);

		_observer.observe(_control);

	}

	private void calcZoomLevel() {
		int level = 0;
		double fract = 1;
		for (int i = 0; fract >= 1; i++) {
			level = i;
			fract = 2 - (_viewbox.getWidth() / controlW) * JsMath.pow(2, level);
		}
		double factor;
		if (level == 0) {
			factor = controlW / _viewbox.getWidth();
		} else {
			factor = level + fract;
		}
		zoomLevel = JsMath.round(factor * 100);

		_zoomDisplay.textContent = "Zoom: " + zoomLevel + "%";
		_zoomDisplay.classList.remove("invisible");

		hideZoomDisplay.cancel();
		hideZoomDisplay.schedule(5 * 1000);
	}

	private double getFactor() {
		float zL = zoomLevel;
		float level = JsMath.trunc(zL / 100);
		float fract = (float) JsNumber.parseFloat(new JsNumber((zL / 100) - level).toFixed(2));
		// (2 - y) / 2^x -> x = ganze 100%; y = 10% Teile | Bsp. 320% -> x = 3, y = 0.2
		return level == 0 ? _viewbox.getWidth() / controlW
			: (2 - fract) / JsMath.pow(2, level);
	}

	private native int getWheelScrollFactor(Event event) /*-{
		return $wnd.BAL.getWheelScrollFactor(event);
	}-*/;

	void panSVG(double panDeltaX, double panDeltaY, boolean zoom) {

		double factor = 1;
		if (!zoom) {
			factor = getFactor();
		}

		float newX = (float) JsMath.max(0,
			JsMath.min(_viewbox.getX() + (panDeltaX * factor),
				_diagram.getRoot().getWidth() - _viewbox.getWidth()));
		float newY = (float) JsMath.max(0,
			JsMath.min(_viewbox.getY() + (panDeltaY * factor),
				_diagram.getRoot().getHeight() - _viewbox.getHeight()));
		_viewbox.setX(newX);
		_viewbox.setY(newY);

		updateServerViewbox();
	}
	
	void zoomSVG(double zoomFactor, double mouseX, double mouseY) {
		if (_viewbox == null) {
			return;
		}

		if (zoomFactor >= 1) {
			_viewbox.setWidth(controlW);
			_viewbox.setHeight(controlH);
			calcZoomLevel();
			panSVG(-_viewbox.getX(), -_viewbox.getY(), true);
		} else {
			float factor = (float) getFactor();
			float diffW = (newCtrlW - controlW) * factor;
			float diffH = (newCtrlH - controlH) * factor;
			float vbW = _viewbox.getWidth() + diffW;
			float vbH = _viewbox.getHeight() + diffH;
			controlW = newCtrlW;
			controlH = newCtrlH;
			float deltaW = (float) (controlW * zoomFactor);
			float deltaH = (float) (controlH * zoomFactor);
			float newVBW = vbW - deltaW;
			float newVBH = vbH - deltaH;

			if ((zoomLevel < 100 && newVBW < controlW) || (zoomLevel > 100 && newVBW > controlW)
				|| JsMath.abs(new JsNumber((newVBW - controlW) / controlW).toFixed(2)) < 0.05) {
				newVBW = controlW;
				newVBH = controlH;
			}

			float maxW = (float) _diagram.getRoot().getWidth();
			float maxH = (float) _diagram.getRoot().getHeight();
			if (maxW < newVBW && maxH < newVBH) {
				float ratio = controlW / controlH;
				if ((maxW / controlW) < (maxH / controlH)) {
					newVBH = (float) JsMath.max(maxH, controlH);
					newVBW = (newVBH * ratio);
				} else {
					newVBW = (float) JsMath.max(maxW, controlW);
					newVBH = (newVBW / ratio);
				}
			}

			_viewbox.setWidth(newVBW);
			_viewbox.setHeight(newVBH);
			
			calcZoomLevel();

			double deltaX = mouseX * zoomFactor;
			double deltaY = mouseY * zoomFactor;
			panSVG(deltaX, deltaY, true);
		}

		updateServerViewbox();
	}

	private void updateServerViewbox() {
		_diagram.setViewBoxX(_viewbox.getX());
		_diagram.setViewBoxY(_viewbox.getY());
		_diagram.setViewBoxWidth(_viewbox.getWidth());
		_diagram.setViewBoxHeight(_viewbox.getHeight());
	}

	@Override
	public void invoke(String command, Object[] args) {
		switch (command) {
			case DIAGRAM_UPDATE_COMMAND: {
				String patch = (String) args[0];

				DomGlobal.console.info("Apply server-side patch: " + patch);

				JsonReader json = new JsonReader(new StringR(patch));
				// avoid sending changes back to server.
				_processServerUpdate = true;
				try {
					_scope.applyChanges(json);
					if (!_dirtyNodes.isEmpty()) {
						applyScopeChanges(_dirtyNodes);
						_dirtyNodes.clear();
					}
					_scope.dropChanges();
				} catch (IOException ex) {
					String error = "Unable to apply diagram patch: " + ex.getMessage();
					DomGlobal.console.error(error);
					logError(error);
				} finally {
					_processServerUpdate = false;
				}

				break;
			}
			default:
				super.invoke(command, args);
		}
	}

	/**
	 * Called after some user-initiated changes occurred in the UI.
	 */
	void onChange(Object... args) {
		try {
			if (!_scope.hasChanges()) {
				DomGlobal.console.info("No update sent because there are no changes.");
				return;
			}

			if (_scope.lazyRequestPossible()) {
				sendLazyUpdate(getId(), unused -> {
					applyScopeChanges(_scope.getDirty());
					String patch = createPatch();

					DomGlobal.console.info("Sending lazy updates: ", patch);
					return patch;
				}, _scope.lazyRequestId());
			} else {
				applyScopeChanges(_scope.getDirty());

				String patch = createPatch();
				if (!DomGlobal.document.contains(_control)) {
					DomGlobal.console.info("Cancel sending update of patch, because control was removed from client: ",
						patch);
					_observer.disconnect();
					return;
				}

				DomGlobal.console.info("Sending updates: ", patch);

				sendUpdate(getId(), patch, _scope.lazyRequestId());
			}
		} finally {
			_scope.reset();
		}

	}

	private String createPatch() {
		try {
			StringW buffer = new StringW();
			_scope.createPatch(new de.haumacher.msgbuf.json.JsonWriter(buffer));

			return buffer.toString();
		} catch (IOException ex) {
			DomGlobal.console.error("Failed to create update patch.", ex);
			return "[]";
		}
	}

	private void applyScopeChanges(Collection<? extends SharedGraphNode> dirtyNodes) {
		SvgWriter updateWriter = new SVGBuilder(_svgDoc, _svg) {
			@Override
			public void write(Drawable element) {
				// Ignore direct contents.
			}

			@Override
			public void beginGroup(Object model) {
				lookupUpdated(model);
			}

			@Override
			public void beginPath(Object model) {
				lookupUpdated(model);
			}

			private void lookupUpdated(Object model) {
				String id = ((Widget) model).getClientId();
				OMSVGElement updated = getDoc().getElementById(id);
				setParent(updated);
			}
		};

		// Update UI.
		for (SharedGraphNode dirty : dirtyNodes) {
			Widget widget = (Widget) dirty;
			if (widget.getClientId() != null) {
				widget.draw(updateWriter);
			}
		}

	}

	/**
	 * General function class for JavaScript.
	 */
	@JsFunction
	@FunctionalInterface
	public interface JSFunction {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param args
		 *        the function argument
		 * @return the function result
		 */
		Object apply(Object... args);

	}

	private native double sendLazyUpdate(String id, JSFunction patchSupplier, double requestID) /*-{
		if (!$wnd.services.ajax.containsLazyRequest(requestID)) {
			var argsSupplier = function() {
				var patch = patchSupplier();
				return {
					controlCommand : "update",
					controlID : id,
					patch : patch
				};
			};
			$wnd.services.ajax.executeOrUpdateWithLazyData(requestID, "dispatchControlCommand", argsSupplier);
		}
	}-*/;

	private native void sendUpdate(String id, String patch, double requestID) /*-{
		$wnd.services.ajax.dropLazyRequest(requestID);
		$wnd.services.ajax.execute("dispatchControlCommand", {
			controlCommand : "update",
			controlID : id,
			patch : patch
		}, true)
	}-*/;

	private native void logError(String message) /*-{
		$wnd.services.log.error(message, null)
	}-*/;

	private SVGBuilder svgBuilder() {
		return new SVGBuilder(_svgDoc, _svg) {
			@Override
			protected void linkModel(OMSVGElement svgElement, Object model) {
				String id = _nextId.createId();
				svgElement.setId(id);
				((Widget) model).setClientId(id);
			}
		};
	}

	private int getEventY(MouseEvent<?> event) {
		return event.getRelativeY(_svg.getElement());
	}

	private int getEventX(MouseEvent<?> event) {
		return event.getRelativeX(_svg.getElement());
	}

	private OMSVGElement removeDisplay(Identified model) {
		_observer.disconnect();
		OMSVGElement result = _svgDoc.getElementById(model.getId());
		result.getParentNode().removeChild(result);
		return result;
	}

	@Override
	public void processClick(ClickTarget node, List<MouseButton> pressedButtons) {
		int nodeId = _scope.id(node);
		List<String> buttons = pressedButtons.stream().map(x -> x.name()).collect(Collectors.toList());
		dispatchClick(getId(), nodeId, toJsArray(buttons));
	}

	@Override
	public void processDrop(DropRegion node, SVGDropEvent event) {
		int nodeId = _scope.id(node);
		if (hasWindowTlDnD()) {
			dispatchDrop(getId(), nodeId);
		}
	}

	private static JsArrayString toJsArray(List<String> input) {
		JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : input) {
			jsArrayString.push(s);
		}
		return jsArrayString;
	}

	private native boolean hasWindowTlDnD() /*-{
		return !($wnd.tlDnD === undefined);
	}-*/;

	private native void dispatchClick(String id, int nodeId, JsArrayString mouseButtons) /*-{
		$wnd.services.ajax.execute("dispatchControlCommand", {
			controlCommand : "dispatchClick",
			controlID : id,
			nodeId : nodeId,
			mouseButtons : mouseButtons
		}, false)
	}-*/;

	private native void dispatchDrop(String id, int nodeId) /*-{
		$wnd.services.ajax.execute("dispatchControlCommand", {
			controlCommand : "dispatchDrop",
			controlID : id,
			nodeId : nodeId,
			data : $wnd.tlDnD.data
		}, false)
	}-*/;
}

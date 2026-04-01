/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGMatrix;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.impl.SVGGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

import com.top_logic.react.flow.callback.DiagramContext;
import com.top_logic.react.flow.client.bridge.ReactBridge;
import com.top_logic.react.flow.client.bridge.StateListener;
import com.top_logic.react.flow.control.FlowControlCommon;
import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropRegion;
import com.top_logic.react.flow.data.MouseButton;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.model.Drawable;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.svg.event.SVGDropEvent;

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
import elemental2.dom.Image;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.ResizeObserver;
import elemental2.dom.ResizeObserverCallback;
import elemental2.dom.ResizeObserverEntry;
import elemental2.dom.WheelEvent;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * Client-side control for rendering flow diagrams in the React framework.
 *
 * <p>
 * Unlike the legacy {@code JSDiagramControl} which extends {@code AbstractJSControl} and uses AJAX
 * for communication, this control is a standalone class that:
 * </p>
 * <ul>
 * <li>Is mounted from a global JS function ({@code GWT_FlowDiagram.mount(...)})</li>
 * <li>Receives server updates via {@link ReactBridge#subscribe(String, StateListener)}</li>
 * <li>Sends commands to server via {@link ReactBridge#sendCommand}</li>
 * </ul>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@JsType
public class FlowDiagramClientControl implements DiagramContext {

	private static final int DEBOUNCE_DELAY_MS = 150;

	private class Scope extends DefaultScope {

		private double _changeTimeout;

		/**
		 * Creates a new Scope.
		 */
		public Scope(int totalParticipants, int participantId) {
			super(totalParticipants, participantId);
			reset();
		}

		@Override
		public void afterChanged(Observable obj, String property) {
			super.afterChanged(obj, property);
			if (_processServerUpdate) {
				_dirtyNodes.add((SharedGraphNode) obj);
			} else {
				if (_changeTimeout == 0) {
					_changeTimeout =
						DomGlobal.setTimeout(FlowDiagramClientControl.this::onChange, DEBOUNCE_DELAY_MS, this);
				}
			}
		}

		public void reset() {
			_changeTimeout = 0;
		}
	}

	private OMSVGDocument _svgDoc;

	private Element _control;

	private OMSVGSVGElement _svg;

	private RenderContext _renderContext = new JSRenderContext();

	private Scope _scope;

	private Diagram _diagram;

	private final String _controlId;

	private final String _windowName;

	private final String _contextPath;

	final SubIdGenerator _nextId;

	private HTMLDivElement _zoomDisplay;

	private Timer _hideZoomDisplay = null;

	/** The current percentage showing how much the SVG is zoomed in by. */
	int _zoomLevel;

	/** The current size of the control (available space for the SVG). */
	float _controlW, _controlH;

	/** The new size of the control after a resize. */
	float _newCtrlW, _newCtrlH;

	/** The mouse position at which the drag started. */
	double _dragStartX, _dragStartY;

	/** Boolean flag if the user is currently dragging the SVG to pan it. */
	boolean _draggingToPan;

	boolean _resizing;

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

	private StateListener _stateListener;

	private EventListener _panningSVG;
	private EventListener _startDraggingSVG;
	private EventListener _dropSVG;
	private EventListener _endDraggingSVG;
	private EventListener _zoomOrScrollSVG;
	private EventListener _resetZoom;
	private EventListener _mouseEnter;
	private EventListener _mouseLeave;

	/**
	 * Creates a {@link FlowDiagramClientControl}.
	 */
	private FlowDiagramClientControl(String controlId, String windowName, String contextPath) {
		_controlId = controlId;
		_windowName = windowName;
		_contextPath = contextPath;
		_nextId = new SubIdGenerator(controlId);
	}

	/**
	 * Mounts the flow diagram control into the given container element.
	 *
	 * @param container
	 *        The DOM element to render into.
	 * @param controlId
	 *        The server-side control ID.
	 * @param windowName
	 *        Browser window identifier.
	 * @param contextPath
	 *        Base path (e.g. "/demo").
	 * @param diagramJson
	 *        The diagram model as JSON string.
	 * @return The control instance (exposes {@link #destroy()} to JavaScript).
	 */
	public static FlowDiagramClientControl mount(HTMLDivElement container, String controlId, String windowName,
			String contextPath, String diagramJson) {
		FlowDiagramClientControl instance = new FlowDiagramClientControl(controlId, windowName, contextPath);
		instance.init(container, diagramJson);
		return instance;
	}

	private void init(HTMLDivElement container, String diagramJson) {
		_control = container;

		_svgDoc = OMSVGParser.currentDocument();

		// Prevent parent containers from showing scrollbars due to SVG content.
		((Element) _control).setAttribute("style",
			((Element) _control).getAttribute("style") + "; overflow: hidden");

		// Create SVG element inside the container.
		String svgId = _controlId + FlowControlCommon.SVG_ID_SUFFIX;
		_svg = _svgDoc.createSVGSVGElement();
		_svg.setId(svgId);
		_svg.setAttribute("width", "100%");
		_svg.setAttribute("height", "100%");
		// Override the grab cursor inherited from draggable="true" on the container.
		_svg.setAttribute("style", "cursor: default");
		_control.appendChild(Js.cast(_svg.getElement()));

		// Enable HTML5 drag events on the container for pan-by-drag.
		((Element) _control).setAttribute("draggable", "true");

		if (diagramJson != null && !diagramJson.isEmpty()) {
			try {
				_scope = new Scope(2, 1);
				Diagram diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(diagramJson)));
				diagram.setContext(this);

				diagram.layout(_renderContext);

				if (diagram.getViewBoxWidth() == 0) {
					double contentW = diagram.getRoot().getWidth();
					double contentH = diagram.getRoot().getHeight();
					double containerW = _control.clientWidth;
					double containerH = _control.clientHeight;

					if (containerW > 0 && containerH > 0 && contentW > 0 && contentH > 0) {
						// Fit entire diagram content into the viewport, preserving aspect ratio.
						// Never zoom in beyond 100% (viewBox >= container).
						double vbW, vbH;
						double containerRatio = containerW / containerH;
						double contentRatio = contentW / contentH;

						if (contentRatio > containerRatio) {
							vbW = contentW;
							vbH = contentW / containerRatio;
						} else {
							vbW = contentH * containerRatio;
							vbH = contentH;
						}

						// Cap at 100% zoom: viewBox must not be smaller than container.
						if (vbW < containerW) {
							vbW = containerW;
							vbH = containerH;
						}

						diagram.setViewBoxWidth(vbW);
						diagram.setViewBoxHeight(vbH);
					} else {
						diagram.setViewBoxWidth(containerW);
						diagram.setViewBoxHeight(containerH);
					}
				}

				diagram.draw(svgBuilder());

				// Drop layout-induced scope changes so they don't trigger an update command.
				_scope.dropChanges();
				_scope.reset();

				_diagram = diagram;

				_zoomDisplay = (HTMLDivElement) DomGlobal.document.createElement("div");
				_zoomDisplay.classList.add("zoomDisplay");
				_control.appendChild(_zoomDisplay);

				_hideZoomDisplay = new Timer() {
					@Override
					public void run() {
						_zoomDisplay.classList.add("invisible");
					}
				};

				_controlW = _control.clientWidth;
				_controlH = _control.clientHeight;
				_newCtrlW = _controlW;
				_newCtrlH = _controlH;
				_viewbox = _svg.getViewBox().getBaseVal();
				calcZoomLevel();

				Element selectedPart = _control.querySelector(".tlSelected");
				if (selectedPart != null) {
					panIntoView(selectedPart);
				}
			} catch (IOException ex) {
				DomGlobal.console.error("Failed to parse diagram data: ", ex.getMessage());
			}
		}

		_draggingToPan = false;

		setupEventListeners();
		setupResizeObserver();
		subscribeToServerUpdates();
	}

	private void setupEventListeners() {
		_panningSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				if (_draggingToPan) {
					DragEvent event = (DragEvent) evt;

					double dragDeltaX = _dragStartX - event.clientX;
					double dragDeltaY = _dragStartY - event.clientY;

					panSVG(dragDeltaX, dragDeltaY, false);

					_dragStartX = event.clientX;
					_dragStartY = event.clientY;
					event.stopImmediatePropagation();
					event.preventDefault();
				}
			}
		};

		_startDraggingSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;
				_dragStartX = event.clientX;
				_dragStartY = event.clientY;

				Image img = (Image) DomGlobal.document.createElement("img");
				img.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=";
				event.dataTransfer.setDragImage(img, 0, 0);

				_draggingToPan = true;

				DomGlobal.window.addEventListener("dragover", _panningSVG);

				event.stopImmediatePropagation();
			}
		};

		_dropSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				event.stopImmediatePropagation();
				event.preventDefault();
			}
		};

		_endDraggingSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				_draggingToPan = false;

				DomGlobal.window.removeEventListener("dragover", _panningSVG);

				event.stopImmediatePropagation();
			}
		};

		_zoomOrScrollSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				WheelEvent event = (WheelEvent) evt;
				if (event.ctrlKey) {
					double delta = event.deltaY == 0 ? event.deltaX : event.deltaY;
					double direction = JsMath.sign(delta);

					int zL = (direction < 0 ? _zoomLevel : _zoomLevel - 10);
					int level = JsMath.trunc(zL / 100);

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

		_resetZoom = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				KeyboardEvent event = (KeyboardEvent) evt;
				if (event.key == "0" && event.ctrlKey) {
					zoomSVG(1, 0, 0);
				}
			}
		};

		_mouseEnter = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DomGlobal.window.addEventListener("keyup", _resetZoom);
			}
		};

		_mouseLeave = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				DomGlobal.window.removeEventListener("keyup", _resetZoom);
			}
		};

		_control.addEventListener("dragstart", _startDraggingSVG);
		_control.addEventListener("drop", _dropSVG);
		_control.addEventListener("dragend", _endDraggingSVG);
		_control.addEventListener("wheel", _zoomOrScrollSVG);
		_control.addEventListener("pointerenter", _mouseEnter);
		_control.addEventListener("pointerleave", _mouseLeave);
	}

	private void setupResizeObserver() {
		ResizeObserverCallback resize = new ResizeObserverCallback() {
			@Override
			public Object onInvoke(JsArray<ResizeObserverEntry> p0, ResizeObserver p1) {
				if (!DomGlobal.document.contains(_control)) {
					_observer.disconnect();
				} else {
					_newCtrlW = _control.clientWidth;
					_newCtrlH = _control.clientHeight;
					if ((_newCtrlW != 0 && _newCtrlW != _controlW) || (_newCtrlH != 0 && _newCtrlH != _controlH)) {
						_resizing = true;
						zoomSVG(0, 0, 0);
						_resizing = false;
					}
				}
				return null;
			}
		};

		_observer = new ResizeObserver(resize);
		_observer.observe(_control);
	}

	private void subscribeToServerUpdates() {
		_stateListener = state -> onServerState(state);
		ReactBridge.subscribe(_controlId, _stateListener);
	}

	/**
	 * Handle a server state update received via SSE.
	 */
	private void onServerState(Object state) {
		String patch = extractDiagramPatch(state);
		if (patch == null || patch.isEmpty()) {
			return;
		}

		DomGlobal.console.info("Apply server-side patch: " + patch);

		if (_scope == null) {
			DomGlobal.console.error("No scope available for applying patch.");
			return;
		}

		try (JsonReader json = new JsonReader(new StringR(patch))) {
			_processServerUpdate = true;
			try {
				_scope.applyChanges(json);
				if (!_dirtyNodes.isEmpty()) {
					applyScopeChanges(_dirtyNodes);
					_dirtyNodes.clear();
				}
				_scope.dropChanges();
			} finally {
				_processServerUpdate = false;
			}
		} catch (IOException ex) {
			DomGlobal.console.error("Unable to apply diagram patch: " + ex.getMessage());
		}
	}

	/**
	 * Extract the "diagramPatch" field from the SSE state object.
	 */
	private static native String extractDiagramPatch(Object state) /*-{
		if (state && state.diagramPatch) {
			return state.diagramPatch;
		}
		return null;
	}-*/;

	/**
	 * Pans the SVG (moves the viewbox) to show a given element in the center.
	 *
	 * @param elementToPanTo
	 *        The element the viewbox should be moved to.
	 */
	public void panIntoView(Element elementToPanTo) {
		SVGGElement element = (SVGGElement) _svg.getElementById(elementToPanTo.id).getElement();
		OMSVGRect bbox = element.getBBox();
		OMSVGMatrix ctm = element.getCTM();
		double factorX = JsMath.sqrt(ctm.getA() * ctm.getA() + ctm.getB() * ctm.getB());
		double factorY = JsMath.sqrt(ctm.getC() * ctm.getC() + ctm.getD() * ctm.getD());
		if (factorX == 0 || factorY == 0) {
			return;
		}
		double left = ctm.getE() / factorX;
		double top = ctm.getF() / factorY;
		double deltaX = left + bbox.getCenterX() - (_viewbox.getCenterX() - _viewbox.getX());
		double deltaY = top + bbox.getCenterY() - (_viewbox.getCenterY() - _viewbox.getY());
		panSVG(deltaX, deltaY, true);
	}

	private void calcZoomLevel() {
		int level = 0;
		double fract = 1;
		for (int i = 0; fract >= 1; i++) {
			if (_controlW == 0) {
				return;
			}
			level = i;
			fract = 2 - (_viewbox.getWidth() / _controlW) * JsMath.pow(2, level);
		}
		double factor;
		if (level == 0) {
			factor = _controlW / _viewbox.getWidth();
		} else {
			factor = level + fract;
		}
		_zoomLevel = JsMath.round(factor * 100);

		_zoomDisplay.textContent = "Zoom: " + _zoomLevel + "%";
		_zoomDisplay.classList.remove("invisible");

		_hideZoomDisplay.cancel();
		_hideZoomDisplay.schedule(5 * 1000);
	}

	private double getFactor() {
		float zL = _zoomLevel;
		float level = JsMath.trunc(zL / 100);
		float fract = (float) JsNumber.parseFloat(new JsNumber((zL / 100) - level).toFixed(2));
		return level == 0 ? _viewbox.getWidth() / _controlW
			: (2 - fract) / JsMath.pow(2, level);
	}

	private native int getWheelScrollFactor(Event event) /*-{
		if ($wnd.BAL && $wnd.BAL.getWheelScrollFactor) {
			return $wnd.BAL.getWheelScrollFactor(event);
		}
		// Fallback: pixel-based scrolling factor.
		return 1;
	}-*/;

	/**
	 * Pans the displayed section of the SVG by changing the viewbox.
	 *
	 * @param panDeltaX
	 *        Pixels to pan the SVG by horizontally (x-axis).
	 * @param panDeltaY
	 *        Pixels to pan the SVG by vertically (y-axis).
	 * @param zoomApplied
	 *        Boolean if the current zoom factor is already applied to the delta values.
	 */
	void panSVG(double panDeltaX, double panDeltaY, boolean zoomApplied) {
		double factor = 1;
		if (!zoomApplied) {
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
			_viewbox.setWidth(_controlW);
			_viewbox.setHeight(_controlH);
			calcZoomLevel();
			panSVG(-_viewbox.getX(), -_viewbox.getY(), true);
		} else {
			float factor = (float) getFactor();
			float diffW = (_newCtrlW - _controlW) * factor;
			float diffH = (_newCtrlH - _controlH) * factor;
			float vbW = _viewbox.getWidth() + diffW;
			float vbH = _viewbox.getHeight() + diffH;
			_controlW = _newCtrlW;
			_controlH = _newCtrlH;
			float deltaW = (float) (_controlW * zoomFactor);
			float deltaH = (float) (_controlH * zoomFactor);
			float newVBW = vbW - deltaW;
			float newVBH = vbH - deltaH;

			if ((_zoomLevel < 100 && newVBW < _controlW) || (_zoomLevel > 100 && newVBW > _controlW)
				|| JsMath.abs(new JsNumber((newVBW - _controlW) / _controlW).toFixed(2)) < 0.05) {
				newVBW = _controlW;
				newVBH = _controlH;
			}

			float maxW = (float) _diagram.getRoot().getWidth();
			float maxH = (float) _diagram.getRoot().getHeight();
			if (maxW < newVBW && maxH < newVBH) {
				float ratio = _controlW / _controlH;
				if ((maxW / _controlW) < (maxH / _controlH)) {
					newVBH = (float) JsMath.max(maxH, JsMath.min(newVBH, _controlH));
					newVBW = (newVBH * ratio);
				} else {
					newVBW = (float) JsMath.max(maxW, JsMath.min(newVBW, _controlW));
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
		if (_resizing) {
			// Don't push viewBox changes to the model during resize — it would dirty the
			// scope and trigger a server round-trip, which causes a re-render loop.
			return;
		}
		_diagram.setViewBoxX(_viewbox.getX());
		_diagram.setViewBoxY(_viewbox.getY());
		_diagram.setViewBoxWidth(_viewbox.getWidth());
		_diagram.setViewBoxHeight(_viewbox.getHeight());
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

			applyScopeChanges(_scope.getDirty());

			String patch = createPatch();
			if (!DomGlobal.document.contains(_control)) {
				DomGlobal.console.info(
					"Cancel sending update of patch, because control was removed from client: ", patch);
				_observer.disconnect();
				return;
			}

			DomGlobal.console.info("Sending updates: ", patch);

			sendUpdateCommand(patch);
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

			/** Cache of ID'd elements detached during update. */
			private Map<String, Element> _idCache = null;

			/** Nesting depth tracker for cache lifecycle. */
			private int _updateDepth = 0;

			@Override
			public void write(Drawable element) {
				if (_idCache != null && element instanceof Widget) {
					String id = ((Widget) element).getClientId();
					if (id != null) {
						Element cached = _idCache.remove(id);
						if (cached != null) {
							// Re-insert preserved sub-widget at current position.
							getParent().appendChild(Js.uncheckedCast(cached));
							return;
						}
					}
				}
				// New sub-widget: draw it for real.
				super.write(element);
			}

			// --- Hierarchical begin/end (change parent) ---

			@Override
			public void beginGroup(Object model) {
				if (tryLookupAndPrepare(model)) return;
				if (_updateDepth > 0) _updateDepth++;
				super.beginGroup(model);
			}

			@Override
			public void endGroup() {
				endUpdatedScope();
				super.endGroup();
			}

			@Override
			public void beginClipPath(Object model) {
				if (tryLookupAndPrepare(model)) return;
				if (_updateDepth > 0) _updateDepth++;
				super.beginClipPath(model);
			}

			@Override
			public void endClipPath() {
				endUpdatedScope();
				super.endClipPath();
			}

			// --- Leaf begin/end (don't change parent) ---

			@Override
			public void beginPath(Object model) {
				if (tryLookupAndPrepare(model)) return;
				super.beginPath(model);
			}

			@Override
			public void beginPolyline(Object model) {
				if (tryLookupAndPrepare(model)) return;
				super.beginPolyline(model);
			}

			@Override
			public void beginPolygon(Object model) {
				if (tryLookupAndPrepare(model)) return;
				super.beginPolygon(model);
			}

			// --- Shared logic ---

			private void endUpdatedScope() {
				if (_updateDepth > 0) {
					_updateDepth--;
					if (_updateDepth == 0) {
						// Update of the looked-up element is complete.
						_idCache = null;
					}
				}
			}

			private boolean tryLookupAndPrepare(Object model) {
				if (model == null) return false;
				String id = ((Widget) model).getClientId();
				if (id == null) return false;
				OMSVGElement existing = getDoc().getElementById(id);
				if (existing == null) return false;

				// Use elemental2 view for DOM traversal.
				Element existingEl = Js.uncheckedCast(existing);

				// Cache all ID'd descendants before clearing.
				_idCache = new HashMap<>();
				collectIdDescendants(existingEl, _idCache);

				// Clear all children — anonymous structure will be redrawn.
				while (existingEl.firstChild != null) {
					existingEl.removeChild(existingEl.lastChild);
				}

				setParent(existing);
				_updateDepth = 1;
				return true;
			}

			private void collectIdDescendants(Element parent, Map<String, Element> cache) {
				for (Element child = parent.firstElementChild; child != null;
						child = child.nextElementSibling) {

					String childId = child.id;
					if (childId != null && !childId.isEmpty()) {
						cache.put(childId, child);
						// Don't recurse into ID'd elements —
						// they manage their own children.
					} else {
						collectIdDescendants(child, cache);
					}
				}
			}
		};

		for (SharedGraphNode dirty : dirtyNodes) {
			Widget widget = (Widget) dirty;
			if (widget.getClientId() != null) {
				widget.draw(updateWriter);
			}
		}
	}

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

	@Override
	public void processClick(ClickTarget node, List<MouseButton> pressedButtons) {
		int nodeId = _scope.id(node);
		List<String> buttons = pressedButtons.stream().map(x -> x.name()).collect(Collectors.toList());
		JavaScriptObject args = createClickArgs(nodeId, toJsStringArray(buttons));
		ReactBridge.sendCommand(_contextPath, _controlId, "dispatchClick", _windowName, args);
	}

	@Override
	public void processDrop(DropRegion node, SVGDropEvent event) {
		int nodeId = _scope.id(node);
		if (hasWindowTlDnD()) {
			JavaScriptObject args = createDropArgs(nodeId);
			ReactBridge.sendCommand(_contextPath, _controlId, "dispatchDrop", _windowName, args);
		}
	}

	private void sendUpdateCommand(String patch) {
		JavaScriptObject args = createUpdateArgs(patch);
		ReactBridge.sendCommand(_contextPath, _controlId, "update", _windowName, args);
	}

	private static native JavaScriptObject createClickArgs(int nodeId, JavaScriptObject mouseButtons) /*-{
		return { nodeId: nodeId, mouseButtons: mouseButtons };
	}-*/;

	private static native JavaScriptObject createDropArgs(int nodeId) /*-{
		var data = null;
		if ($wnd.services && $wnd.services.ajax && $wnd.services.ajax.mainLayout
				&& $wnd.services.ajax.mainLayout.tlDnD) {
			data = $wnd.services.ajax.mainLayout.tlDnD.data;
		}
		return { nodeId: nodeId, data: data };
	}-*/;

	private static native JavaScriptObject createUpdateArgs(String patch) /*-{
		return { patch: patch };
	}-*/;

	private static native JavaScriptObject toJsStringArray(List<String> input) /*-{
		var arr = [];
		var iter = input.@java.util.List::iterator()();
		while (iter.@java.util.Iterator::hasNext()()) {
			arr.push(iter.@java.util.Iterator::next()());
		}
		return arr;
	}-*/;

	private native boolean hasWindowTlDnD() /*-{
		return !!($wnd.services && $wnd.services.ajax && $wnd.services.ajax.mainLayout
				&& $wnd.services.ajax.mainLayout.tlDnD);
	}-*/;

	/**
	 * Destroys this control, removing all event listeners and subscriptions.
	 */
	@JsMethod
	public void destroy() {
		ReactBridge.unsubscribe(_controlId, _stateListener);

		if (_observer != null) {
			_observer.disconnect();
		}

		if (_hideZoomDisplay != null) {
			_hideZoomDisplay.cancel();
		}

		// Remove event listeners from the control element.
		_control.removeEventListener("dragstart", _startDraggingSVG);
		_control.removeEventListener("drop", _dropSVG);
		_control.removeEventListener("dragend", _endDraggingSVG);
		_control.removeEventListener("wheel", _zoomOrScrollSVG);
		_control.removeEventListener("pointerenter", _mouseEnter);
		_control.removeEventListener("pointerleave", _mouseLeave);

		// Clean up global listeners.
		DomGlobal.window.removeEventListener("dragover", _panningSVG);
		DomGlobal.window.removeEventListener("keyup", _resetZoom);

		// Remove SVG and zoom display from the container so a re-mount starts clean.
		if (_svg != null) {
			_svg.getElement().removeFromParent();
		}
		if (_zoomDisplay != null) {
			_zoomDisplay.remove();
		}
	}

}

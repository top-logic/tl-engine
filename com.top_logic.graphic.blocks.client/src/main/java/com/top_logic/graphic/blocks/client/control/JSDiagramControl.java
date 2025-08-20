/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.i18n.client.NumberFormat;

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
import elemental2.core.JsArray;
import elemental2.core.JsMath;
import elemental2.dom.DomGlobal;
import elemental2.dom.DragEvent;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDocument;
import elemental2.dom.Image;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.ResizeObserver;
import elemental2.dom.ResizeObserverCallback;
import elemental2.dom.ResizeObserverEntry;
import elemental2.dom.Response;
import elemental2.dom.WheelEvent;
import elemental2.promise.Promise;

/**
 * Client-side logic of a flow diagram control.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSDiagramControl extends AbstractJSControl
		implements JSDiagramControlCommon, DiagramContext {

	private static final String SELECTED_CLASS = "tlbSelected";

	private OMSVGDocument _svgDoc;

	private Element _control;

	private OMSVGSVGElement _svg;

	private RenderContext _renderContext = new JSRenderContext();

	private DefaultScope _scope;

	private Diagram _diagram;

	final SubIdGenerator _nextId;

	double _changeTimeout;

	double dragStartX, dragStartY;

	private OMSVGRect viewbox;

	boolean draggingToPan;
	
	private ResizeObserver _observer;

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

		Element ctrlParent = _control.parentElement;
		com.google.gwt.dom.client.Element ctrl = jsinterop.base.Js.cast(_control);

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
					_scope = new DefaultScope(2, 1) {
						@Override
						protected void beforeChange() {
							if (_changeTimeout != 0) {
								DomGlobal.clearTimeout(_changeTimeout);
							}
						}
					};
					Diagram diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(json)));
					diagram.setContext(this);

					diagram.layout(_renderContext);

					if (diagram.getViewBoxWidth() == 0) {
						diagram.setViewBoxWidth(ctrlParent.clientWidth);
					}
					if (diagram.getViewBoxHeight() == 0) {
						diagram.setViewBoxHeight(ctrlParent.clientHeight - 5);
					}

					diagram.draw(svgBuilder());

					_diagram = diagram;
				} catch (IOException ex) {
					DomGlobal.console.error("Failed to fetch diagram data: ", ex.getMessage());
				}
				return null;
			});

		NumberFormat nf = NumberFormat.getDecimalFormat();

		draggingToPan = false;

		EventListener panningSVG = new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				if (draggingToPan) {
					DragEvent event = (DragEvent) evt;

					double dragDeltaX = dragStartX - event.clientX;
					double dragDeltaY = dragStartY - event.clientY;

					panSVG(dragDeltaX, dragDeltaY);

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
					double factor = JsMath.sign(delta) / -10;
					zoomSVG(factor, event.offsetX, event.offsetY);
				} else if (event.shiftKey) {
					int scrollFactor = getWheelScrollFactor(evt);
					double deltaX = event.deltaY * scrollFactor;
					double deltaY = event.deltaX * scrollFactor;
					panSVG(deltaX, deltaY);
				} else {
					int scrollFactor = getWheelScrollFactor(evt);
					double deltaX = event.deltaX * scrollFactor;
					double deltaY = event.deltaY * scrollFactor;
					panSVG(deltaX, deltaY);
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

		_control.addEventListener("mouseenter", mouseEnter);

		_control.addEventListener("mouseleave", mouseLeave);

		ResizeObserverCallback resize = new ResizeObserverCallback() {
			@Override
			public Object onInvoke(JsArray<ResizeObserverEntry> p0, ResizeObserver p1) {
				zoomSVG(0, 0, 0);
				return null;
			}
		};

		_observer = new ResizeObserver(resize);

		_observer.observe(ctrlParent);

	}

	private native int getWheelScrollFactor(Event event) /*-{
		return $wnd.BAL.getWheelScrollFactor(event);
	}-*/;

	void panSVG(double panDeltaX, double panDeltaY) {
		viewbox = _svg.getViewBox().getBaseVal();
		float newX = (float) JsMath.max(0,
			JsMath.min(viewbox.getX() + panDeltaX, _diagram.getRoot().getWidth() - viewbox.getWidth()));
		float newY = (float) JsMath.max(0,
			JsMath.min(viewbox.getY() + panDeltaY, _diagram.getRoot().getHeight() - viewbox.getHeight()));
		viewbox.setX(newX);
		viewbox.setY(newY);

		_diagram.setViewBoxX(viewbox.getX());
		_diagram.setViewBoxY(viewbox.getY());
	}
	
	void zoomSVG(double zoomFactor, double mouseX, double mouseY) {
		viewbox = _svg.getViewBox().getBaseVal();
		float parentW = _control.parentElement.clientWidth;
		float parentH = _control.parentElement.clientHeight - 5;
		if (zoomFactor >= 1) {
			viewbox.setWidth(parentW);
			viewbox.setHeight(parentH);
			panSVG(-viewbox.getX(), -viewbox.getY());
		} else {
			float ratio = parentW / parentH;
			float vbW = viewbox.getWidth();
			float vbH = viewbox.getHeight();
			if (ratio != vbW / vbH) {
				vbH = (vbW / ratio);
			}
			float deltaW = (float) (vbW * zoomFactor);
			float deltaH = (float) (vbH * zoomFactor);

			viewbox.setWidth(vbW - deltaW);
			viewbox.setHeight(vbH - deltaH);

			double deltaX = deltaW * mouseX / parentW;
			double deltaY = deltaH * mouseY / parentH;
			panSVG(deltaX, deltaY);
		}

		_diagram.setViewBoxWidth(viewbox.getWidth());
		_diagram.setViewBoxHeight(viewbox.getHeight());
	}

	/**
	 * Called after some user-initiated changes occurred in the UI.
	 */
	void onChange(Object... args) {
		_changeTimeout = 0;

		if (_scope.getDirty().isEmpty()) {
			return;
		}

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
		for (SharedGraphNode dirty : _scope.getDirty()) {
			Widget widget = (Widget) dirty;
			if (widget.getClientId() != null) {
				widget.draw(updateWriter);
			}
		}

		try {
			StringW buffer = new StringW();
			_scope.createPatch(new de.haumacher.msgbuf.json.JsonWriter(buffer));

			String patch = buffer.toString();
			DomGlobal.console.info("Sending updates: ", patch);

			sendUpdate(getId(), patch, true);
		} catch (IOException ex) {
			DomGlobal.console.error("Faild to write updates.", ex);
		}
	}

	private native void sendUpdate(String id, String patch, boolean showWait) /*-{
		$wnd.services.ajax.execute("dispatchControlCommand", {
			controlCommand : "update",
			controlID : id,
			patch : patch
		}, showWait)
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

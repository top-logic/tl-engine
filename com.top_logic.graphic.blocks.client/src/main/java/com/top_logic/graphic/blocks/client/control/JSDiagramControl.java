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
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.MouseEvent;

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
import elemental2.dom.DomGlobal;
import elemental2.dom.DragEvent;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDocument;
import elemental2.dom.Image;
import elemental2.dom.Response;
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

	boolean draggingSVG;

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
					_scope = new DefaultScope(2, 1) {
						@Override
						protected void beforeChange() {
							if (_changeTimeout != 0) {
								DomGlobal.clearTimeout(_changeTimeout);
							}
							_changeTimeout = DomGlobal.setTimeout(JSDiagramControl.this::onChange, 10);
						}
					};
					Diagram diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(json)));
					diagram.setContext(this);

					diagram.layout(_renderContext);
					diagram.draw(svgBuilder());

					_diagram = diagram;
				} catch (IOException ex) {
					DomGlobal.console.error("Failed to fetch diagram data: ", ex.getMessage());
				}
				return null;
			});

		draggingSVG = false;

		_control.addEventListener("dragstart", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;
				dragStartX = event.clientX;
				dragStartY = event.clientY;

				Image img = (Image) DomGlobal.document.createElement("img");
				img.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=";
				event.dataTransfer.setDragImage(img, 0, 0);

				draggingSVG = true;

				event.stopImmediatePropagation();
			}
		});

		_control.addEventListener("dragend", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				draggingSVG = false;

				event.stopImmediatePropagation();
			}
		});

		_control.addEventListener("drop", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				DragEvent event = (DragEvent) evt;

				event.stopImmediatePropagation();
				event.preventDefault();
			}
		});

		_control.addEventListener("dragover", new EventListener() {

			@Override
			public void handleEvent(Event evt) {
				if (draggingSVG) {
					DragEvent event = (DragEvent) evt;

					double dragDeltaX = dragStartX - event.clientX;
					double dragDeltaY = dragStartY - event.clientY;

					Element ctrlParent = _control.parentElement;

					double scrollX = ctrlParent.scrollLeft;
					double scrollY = ctrlParent.scrollTop;

					ctrlParent.scrollTo(scrollX + dragDeltaX, scrollY + dragDeltaY);

					dragStartX = event.clientX;
					dragStartY = event.clientY;
					event.stopImmediatePropagation();
					event.preventDefault();
				}
			}
		});
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

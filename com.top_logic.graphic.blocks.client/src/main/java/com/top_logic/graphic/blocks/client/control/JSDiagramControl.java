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
import de.haumacher.msgbuf.observer.Observable;
import elemental2.core.JsMath;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.DragEvent;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDocument;
import elemental2.dom.Image;
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

	double scrollX, scrollY;

	boolean draggingToPan;

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
							if (!_processServerUpdate) {
								if (_changeTimeout != 0) {
									DomGlobal.clearTimeout(_changeTimeout);
								}
								_changeTimeout = DomGlobal.setTimeout(JSDiagramControl.this::onChange, 10);
							}
<<<<<<< Upstream, based on origin/PREVIEW/ffa2
=======
							_changeTimeout = DomGlobal.setTimeout(JSDiagramControl.this::onChange, 100);
>>>>>>> 34b9985 Ticket #25918: Store viewbox values in diagram to survive repainting the control.
						}
						
						@Override
						public void afterChanged(Observable obj, String property) {
							super.afterChanged(obj, property);
							if (_processServerUpdate) {
								_dirtyNodes.add((SharedGraphNode) obj);
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

					float dragDeltaX = (float) (dragStartX - event.clientX);
					float dragDeltaY = (float) (dragStartY - event.clientY);

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
				scrollX = ctrlParent.scrollLeft;
				scrollY = ctrlParent.scrollTop;

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
					double factor = JsMath.sign(event.deltaY) / -10;
					double oldZoom = nf.parse(ctrl.getStyle().getProperty("scale"));
					double newZoom = oldZoom + factor;

					DOMRect parentRect = ctrlParent.getBoundingClientRect();
					double cursorSVGLeft = event.clientX - parentRect.left;
					double cursorSVGTop = event.clientY - parentRect.top;

					double newScrollTop = (((cursorSVGTop + ctrlParent.scrollTop) / oldZoom) * newZoom) - cursorSVGTop;
					double newScrollLeft =
						(((cursorSVGLeft + ctrlParent.scrollLeft) / oldZoom) * newZoom) - cursorSVGLeft;

					ctrl.getStyle().setProperty("scale", nf.format(newZoom));
					ctrlParent.scrollTop = Math.round(newScrollTop);
					ctrlParent.scrollLeft = Math.round(newScrollLeft);

					event.stopImmediatePropagation();
					event.preventDefault();
				} else if (event.shiftKey) {
					int scrollFactor = getWheelScrollFactor(evt);
					float deltaX = (float) event.deltaY * scrollFactor;
					float deltaY = (float) event.deltaX * scrollFactor;
					panSVG(deltaX, deltaY);
				} else {
					int scrollFactor = getWheelScrollFactor(evt);
					float deltaX = (float) event.deltaX * scrollFactor;
					float deltaY = (float) event.deltaY * scrollFactor;
					panSVG(deltaX, deltaY);
				}
			}
		};

		ctrl.getStyle().setProperty("scale", "1");

		_control.addEventListener("dragstart", startDraggingSVG);

		_control.addEventListener("drop", dropSVG);

		_control.addEventListener("dragend", endDraggingSVG);

		_control.addEventListener("wheel", zoomOrScrollSVG);
	}

	private native int getWheelScrollFactor(Event event) /*-{
		return $wnd.BAL.getWheelScrollFactor(event);
	}-*/;

	void panSVG(float panDeltaX, float panDeltaY) {
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
		_changeTimeout = 0;

		applyScopeChanges(_scope.getDirty());

		try {
			StringW buffer = new StringW();
			_scope.createPatch(new de.haumacher.msgbuf.json.JsonWriter(buffer));

			String patch = buffer.toString();
			DomGlobal.console.info("Sending updates: ", patch);

			sendUpdate(getId(), patch, true);
			
			_svg.setWidth(Unit.PX, _control.parentElement.clientWidth);
			_svg.setHeight(Unit.PX, _control.parentElement.clientHeight - 5);
			_svg.setViewBox(0, 0, _control.parentElement.clientWidth, _control.parentElement.clientHeight - 5);

		} catch (IOException ex) {
			DomGlobal.console.error("Failed to write updates.", ex);
		}
	}

	private void applyScopeChanges(Collection<? extends SharedGraphNode> dirtyNodes) {
		if (dirtyNodes.isEmpty()) {
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
		for (SharedGraphNode dirty : dirtyNodes) {
			Widget widget = (Widget) dirty;
			if (widget.getClientId() != null) {
				widget.draw(updateWriter);
			}
		}

<<<<<<< Upstream, based on origin/PREVIEW/ffa2
=======
		try {
			StringW buffer = new StringW();
			_scope.createPatch(new de.haumacher.msgbuf.json.JsonWriter(buffer));

			String patch = buffer.toString();
			DomGlobal.console.info("Sending updates: ", patch);

			sendUpdate(getId(), patch, true);

		} catch (IOException ex) {
			DomGlobal.console.error("Faild to write updates.", ex);
		}
>>>>>>> 34b9985 Ticket #25918: Store viewbox values in diagram to survive repainting the control.
	}

	private native void sendUpdate(String id, String patch, boolean showWait) /*-{
		$wnd.services.ajax.execute("dispatchControlCommand", {
			controlCommand : "update",
			controlID : id,
			patch : patch
		}, showWait)
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

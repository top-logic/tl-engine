/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import java.io.IOException;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;

import com.top_logic.ajax.client.control.AbstractJSControl;
import com.top_logic.graphic.blocks.control.JSBlocksControlCommon;
import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.model.Identified;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.control.JSDiagramControlCommon;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.Widget;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import elemental2.dom.DomGlobal;
import elemental2.dom.Response;
import elemental2.promise.Promise;

/**
 * Client-side logic of a {@link JSBlocksControlCommon}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSDiagramControl extends AbstractJSControl
		implements JSDiagramControlCommon, ClickHandler {

	private static final String SELECTED_CLASS = "tlbSelected";

	private OMSVGDocument _svgDoc;

	private OMSVGSVGElement _svg;

	private RenderContext _renderContext = new JSRenderContext();

	private DefaultScope _scope;

	private Diagram _diagram;

	final SubIdGenerator _nextId;

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
		
		Element controlElement = Document.get().getElementById(getId());
		String contentUrl = controlElement.getAttribute(DATA_CONTENT_ATTR);
		
		_svgDoc = OMSVGParser.currentDocument();
		_svg = _svgDoc.getElementById(controlElement().getId());

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
							DomGlobal.setTimeout(JSDiagramControl.this::onChange, 10);
						}
					};
					Diagram diagram = Diagram.readDiagram(_scope, new JsonReader(new StringR(json)));

					diagram.layout(_renderContext);
					diagram.draw(svgBuilder());

					_diagram = diagram;
				} catch (IOException ex) {
					DomGlobal.console.error("Failed to fetch diagram data: " + ex.getMessage());
				}
				return null;
			});

		_svg.addClickHandler(this);
	}

	/**
	 * Called after some user-initiated changes occurred in the UI.
	 */
	void onChange(Object... args) {
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

			DomGlobal.console.info("Sending updates: ", buffer.toString());
		} catch (IOException ex) {
			DomGlobal.console.error("Faild to write updates.", ex);
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
	public void onClick(ClickEvent event) {
		Element element = event.getNativeEvent().getEventTarget().cast();
		String id = element.getId();
		// DomGlobal.alert("Clicked: " + id);
	}

}

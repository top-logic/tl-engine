/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.control;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.ajax.server.util.JSControlUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.flow.control.JSDiagramControlCommon;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.Scope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;
import de.haumacher.msgbuf.server.io.WriterAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link Control} for displaying diagrams.
 */
public class DiagramControl extends AbstractControlBase implements JSDiagramControlCommon, ContentHandler {

	private Diagram _diagram;

	private Scope _graphScope;

	/**
	 * Creates a {@link DiagramControl}.
	 */
	public DiagramControl() {
		super();
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
		requestRepaint();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		getFrameScope().registerContentHandler(null, this);
	}

	@Override
	protected void internalDetach() {
		getFrameScope().deregisterContentHandler(this);

		super.internalDetach();
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected String getTypeCssClass() {
		return "tlDiagram";
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);

		out.writeAttribute(DATA_CONTENT_ATTR, getFrameScope().getURL(context, this).getURL());
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SVG);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.endTag(SVG);

		JSControlUtil.writeCreateJSControlScript(out, JSDiagramControlCommon.CONTROL_TYPE, getID());
	}

	@Override
	protected boolean hasUpdates() {
		// TODO: Automatically created
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// TODO: Automatically created

	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		HttpServletResponse response = context.asResponse();
		if (_diagram == null) {
			response.setContentType("text/plain");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write("");
			return;
		}

		_graphScope = new ExternalScope(2, 0);
		response.setContentType("text/json");
		response.setCharacterEncoding("utf-8");
		_diagram.writeTo(_graphScope, new JsonWriter(new WriterAdapter(response.getWriter())));

		ExternalScope graphScope = new ExternalScope(2, 0);
		StringW buffer = new StringW();
		_diagram.writeTo(graphScope, new JsonWriter(buffer));
		Logger.info("Diagram: \n" + buffer.toString(), DiagramControl.class);
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

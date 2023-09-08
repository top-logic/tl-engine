/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateClassPropertyGraphCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateConnectionGraphCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateEnumerationCommand;
import com.top_logic.graph.diagramjs.server.commands.DeleteGraphPartCommand;
import com.top_logic.graph.diagramjs.server.commands.GoToDefinitionCommand;
import com.top_logic.graph.diagramjs.server.handler.DiagramHandler;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.graph.server.ui.AbstractGraphControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLType;

/**
 * Server-side <code>DiagramJS</code> graph control writes a graph init script to execute the
 * UIService init method to create the appropriate client-side graph control.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphControl extends AbstractGraphControl implements DiagramHandler {

	private DiagramHandler _diagramHandler;

	/**
	 * Creates a {@link DiagramJSGraphControl} for the given {@link GraphData}.
	 */
	public DiagramJSGraphControl(GraphData data) {
		super(data, createCommandMap(GRAPH_COMMANDS, CreateConnectionGraphCommand.INSTANCE,
			CreateClassPropertyGraphCommand.INSTANCE, DeleteGraphPartCommand.INSTANCE, CreateClassCommand.INSTANCE,
			CreateEnumerationCommand.INSTANCE, GoToDefinitionCommand.INSTANCE));
	}

	/**
	 * Creates a {@link DiagramJSGraphControl} for the given {@link GraphData} and
	 * {@link DiagramHandler}.
	 */
	public DiagramJSGraphControl(GraphData data, DiagramHandler diagramHandler) {
		this(data);

		_diagramHandler = diagramHandler;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeGraphDOMElement(out);
		writeGraphInitScript(out, GraphControlCommon.DIAGRAMJS_GRAPH_CONTROL);

		out.endTag(DIV);
	}

	@Override
	public void createReference(String type, TLType source, TLType target) {
		_diagramHandler.createReference(type, source, target);
	}

	@Override
	public void createInheritance(TLClass source, TLClass target) {
		_diagramHandler.createInheritance(source, target);
	}

	@Override
	public void createClassProperty(TLClass clazz) {
		_diagramHandler.createClassProperty(clazz);
	}

	@Override
	public void createClass(Bounds bounds) {
		_diagramHandler.createClass(bounds);
	}

	@Override
	public void createEnumeration(Bounds bounds) {
		_diagramHandler.createEnumeration(bounds);
	}

	@Override
	public void gotoDefinition(TLModelPart modelPart) {
		_diagramHandler.gotoDefinition(modelPart);
	}

}

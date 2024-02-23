/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import static com.top_logic.ajax.shared.api.NamingConstants.*;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.ajax.server.util.JSControlUtil;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateClassPropertyGraphCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateConnectionGraphCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateEnumerationCommand;
import com.top_logic.graph.diagramjs.server.commands.DeleteGraphPartCommand;
import com.top_logic.graph.diagramjs.server.commands.GoToDefinitionCommand;
import com.top_logic.graph.diagramjs.server.commands.ToggleElementVisibilityCommand;
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
 * @author <a href="mailto:sfo@top-logic.com">Sven F�rster</a>
 */
public class DiagramJSGraphControl extends AbstractGraphControl
		implements DiagramHandler, HiddenElementsVisibilityListener {

	private DiagramJSGraphComponent _graphComponent;

	private boolean _showHiddenElements;

	/**
	 * Creates a {@link DiagramJSGraphControl} for the given {@link GraphData}.
	 */
	public DiagramJSGraphControl(GraphData data) {
		super(data, createCommandMap(GRAPH_COMMANDS, CreateConnectionGraphCommand.INSTANCE,
			CreateClassPropertyGraphCommand.INSTANCE, DeleteGraphPartCommand.INSTANCE, CreateClassCommand.INSTANCE,
			CreateEnumerationCommand.INSTANCE, GoToDefinitionCommand.INSTANCE, ToggleElementVisibilityCommand.INSTANCE));
	}

	/**
	 * Creates a {@link DiagramJSGraphControl} for the given {@link DiagramJSGraphComponent}.
	 */
	public DiagramJSGraphControl(DiagramJSGraphComponent graphComponent) {
		this(graphComponent.getGraphData());

		_graphComponent = graphComponent;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_graphComponent.addListener(DiagramJSGraphComponent.SHOW_HIDDEN_ELEMENTS_EVENT, this);
	}

	@Override
	protected void internalDetach() {
		_graphComponent.removeListener(DiagramJSGraphComponent.SHOW_HIDDEN_ELEMENTS_EVENT, this);

		super.internalDetach();
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

	/**
	 * These are the arguments that are received by the <code>DiagramJSGraphControl#init</code> of
	 * the client graph control.
	 */
	@Override
	protected void writeGraphInitScript(TagWriter out, String type) throws IOException {
		JSControlUtil.writeCreateJSControlScript(out, type, getID(), retrieveStateAsJSON(), _showHiddenElements);
	}

	@Override
	public void createReference(String type, TLType source, TLType target) {
		_graphComponent.createReference(type, source, target);
	}

	@Override
	public void createInheritance(TLClass source, TLClass target) {
		_graphComponent.createInheritance(source, target);
	}

	@Override
	public void createClassProperty(TLClass clazz) {
		_graphComponent.createClassProperty(clazz);
	}

	@Override
	public void createClass(Bounds bounds) {
		_graphComponent.createClass(bounds);
	}

	@Override
	public void createEnumeration(Bounds bounds) {
		_graphComponent.createEnumeration(bounds);
	}

	@Override
	public void gotoDefinition(TLModelPart modelPart) {
		_graphComponent.gotoDefinition(modelPart);
	}

	@Override
	public void setElementsVisibility(Collection<Object> graphPartModels, boolean isVisible) {
		_graphComponent.setElementsVisibility(graphPartModels, isVisible);
	}

	@Override
	public void handleHiddenElementsVisibility(Object sender, Boolean oldValue, Boolean newValue) {
		_showHiddenElements = newValue.booleanValue();

		if (!isInvalid()) {
			String objectPath = SERVICE_NAMESPACE + "." + SERVICE_NAME;
			String methodName = INVOKE;
			getFrameScope().addClientAction(new JSFunctionCall(getID(), objectPath, methodName,
				GraphControlCommon.SHOW_HIDDEN_ELEMENTS_COMMAND, newValue.toString()));
		}
	}

}

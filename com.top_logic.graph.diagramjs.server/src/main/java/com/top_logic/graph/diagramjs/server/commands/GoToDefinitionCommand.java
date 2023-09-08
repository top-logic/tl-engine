/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphControl;
import com.top_logic.graph.diagramjs.server.I18NConstants;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.model.TLModelPart;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} to go the definition of a graph part.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class GoToDefinitionCommand extends ControlCommand {

	/**
	 * Singleton instance of {@link GoToDefinitionCommand}.
	 */
	public static final GoToDefinitionCommand INSTANCE = new GoToDefinitionCommand();

	private GoToDefinitionCommand() {
		super(DiagramJSGraphControlCommon.GO_TO_DEFINITION_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.GO_TO_DEFINITION_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;
		GraphPart graphPart = getGraphPart((String) arguments.get("id"), graphControl);
		graphControl.gotoDefinition((TLModelPart) graphPart.getTag());

		return HandlerResult.DEFAULT_RESULT;
	}

	SharedGraph getSharedGraph(DiagramJSGraphControl graphControl) {
		return graphControl.getModel().getGraph();
	}

	GraphPart getGraphPart(String id, DiagramJSGraphControl graphControl) {
		ObjectScope objectScope = getObjectScope(graphControl);

		return (GraphPart) objectScope.obj(id);
	}

	private ObjectScope getObjectScope(DiagramJSGraphControl graphControl) {
		return getSharedGraph(graphControl).data().scope();
	}
}

/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphControl;
import com.top_logic.graph.diagramjs.server.I18NConstants;
import com.top_logic.graph.diagramjs.server.handler.ElementsVisibilityHandler;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} executed when elements on the client diagram have changed their
 * visibility.
 * 
 * @see ElementsVisibilityHandler
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class ElementVisibilityCommand extends ControlCommand {

	/**
	 * Singleton instance of {@link ElementVisibilityCommand}.
	 */
	public static final ElementVisibilityCommand INSTANCE = new ElementVisibilityCommand();

	private ElementVisibilityCommand() {
		super(DiagramJSGraphControlCommon.ELEMENT_VISIBILITY_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.ELEMENT_VISIBILITY_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;
		graphControl.setElementsVisibility(getGraphPartModels(arguments, graphControl), getVisibility(arguments));

		return HandlerResult.DEFAULT_RESULT;
	}

	private boolean getVisibility(Map<String, Object> arguments) {
		return (Boolean) arguments.get("visibility");
	}

	private Set<Object> getGraphPartModels(Map<String, Object> arguments, DiagramJSGraphControl graphControl) {
		Set<Object> graphPartModels = new HashSet<>();

		for (String id : getElementIDs(arguments)) {
			graphPartModels.add(DeleteGraphPartCommand.getGraphPart(id, graphControl).getTag());
		}

		return graphPartModels;
	}

	@SuppressWarnings("unchecked")
	private List<String> getElementIDs(Map<String, Object> arguments) {
		return (List<String>) arguments.get("ids");
	}

}

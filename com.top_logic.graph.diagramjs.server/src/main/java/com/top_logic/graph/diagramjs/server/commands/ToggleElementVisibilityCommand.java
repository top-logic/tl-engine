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
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} executed when the diagram element has changed its visibility.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class ToggleElementVisibilityCommand extends ControlCommand {

	/**
	 * Singleton instance of {@link ToggleElementVisibilityCommand}.
	 */
	public static final ToggleElementVisibilityCommand INSTANCE = new ToggleElementVisibilityCommand();

	private ToggleElementVisibilityCommand() {
		super(DiagramJSGraphControlCommon.ELEMENT_VISIBILITY_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.TOGGLE_ELEMENT_VISIBILITY_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;
		boolean isVisible = (Boolean) arguments.get("visibility");
		graphControl.setElementsVisibility(getGraphPartModels(arguments, graphControl), isVisible);

		return HandlerResult.DEFAULT_RESULT;
	}

	private Set<Object> getGraphPartModels(Map<String, Object> arguments, DiagramJSGraphControl graphControl) {
		List<String> ids = (List<String>) arguments.get("ids");
		Set<Object> graphPartModels = new HashSet<>();

		for (int i = 0; i < ids.size(); i++) {
			graphPartModels.add(DeleteGraphPartCommand.getGraphPart(ids.get(i), graphControl).getTag());
		}

		return graphPartModels;
	}

}

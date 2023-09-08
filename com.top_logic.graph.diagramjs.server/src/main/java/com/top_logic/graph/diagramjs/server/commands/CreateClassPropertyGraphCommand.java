/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.impl.DefaultNode;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphControl;
import com.top_logic.graph.diagramjs.server.I18NConstants;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} for creating class properties.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateClassPropertyGraphCommand extends ControlCommand {

	private static final String GRAPH_PART_ID = "id";

	/**
	 * Singleton instance of {@link CreateClassPropertyGraphCommand}.
	 */
	public static final CreateClassPropertyGraphCommand INSTANCE = new CreateClassPropertyGraphCommand();

	private CreateClassPropertyGraphCommand() {
		super(DiagramJSGraphControlCommon.CREATE_CLASS_PROPERTY_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CREATE_CLASS_PROPERTY_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;

		ObjectScope scope = getObjectScope(graphControl);

		graphControl.createClassProperty(getTLClass((String) arguments.get(GRAPH_PART_ID), scope));

		return HandlerResult.DEFAULT_RESULT;
	}

	private ObjectScope getObjectScope(DiagramJSGraphControl graphControl) {
		return graphControl.getModel().getGraph().data().scope();
	}

	private TLClass getTLClass(String id, ObjectScope scope) {
		return (TLClass) ((DefaultNode) scope.obj(id)).getTag();
	}

}

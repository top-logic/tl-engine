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
import com.top_logic.graph.diagramjs.util.GraphLayoutConstants;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ControlCommand} for creating connections.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateConnectionGraphCommand extends ControlCommand {

	private static final String TARGET_ID = "targetID";

	private static final String SOURCE_ID = "sourceID";

	private static final String TYPE = "type";

	/**
	 * Singleton instance of {@link CreateConnectionGraphCommand}.
	 */
	public static final CreateConnectionGraphCommand INSTANCE = new CreateConnectionGraphCommand();

	private CreateConnectionGraphCommand() {
		super(DiagramJSGraphControlCommon.CREATE_CONNECTION_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CREATE_CONNECTION_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;

		String type = (String) arguments.get(TYPE);
		
		if (GraphLayoutConstants.EDGE_INHERITANCE_TYPE.equals(type)) {
			createInheritanceConnection(arguments, graphControl);
		} else {
			createReferenceConnection(arguments, graphControl, type);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void createInheritanceConnection(Map<String, Object> arguments, DiagramJSGraphControl graphControl) {
		String sourceID = (String) arguments.get(SOURCE_ID);
		String targetID = (String) arguments.get(TARGET_ID);

		ObjectScope scope = getObjectScope(graphControl);

		TLType source = getTLType(sourceID, scope);
		TLType target = getTLType(targetID, scope);

		if (source instanceof TLClass && target instanceof TLClass) {
			graphControl.createInheritance((TLClass) source, (TLClass) target);
		} else {
			throw new TopLogicException(I18NConstants.ERROR_INHERITANCE_ONLY_BETWEEN_CLASSES);
		}
	}

	private void createReferenceConnection(Map<String, Object> arguments, DiagramJSGraphControl control, String type) {
		String sourceID = (String) arguments.get(SOURCE_ID);
		String targetID = (String) arguments.get(TARGET_ID);

		ObjectScope scope = getObjectScope(control);

		TLType source = getTLType(sourceID, scope);

		if (source instanceof TLClass) {
			TLType target = getTLType(targetID, scope);

			control.createReference(type, source, target);
		}
	}

	private ObjectScope getObjectScope(DiagramJSGraphControl graphControl) {
		return graphControl.getModel().getGraph().data().scope();
	}

	private TLType getTLType(String id, ObjectScope scope) {
		return (TLType) ((DefaultNode) scope.obj(id)).getTag();
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAX;
import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.graph.common.model.impl.DefaultNode;
import com.top_logic.graph.diagramjs.client.ajax.GraphPartCommandArguments;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;

/**
 * Triggers server side create dialog for creating a class property.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class CreateClassPropertyEventHandler implements EventHandler {

	private static final String COMMAND_ID_DISPATCH = "dispatchControlCommand";

	String _controlID;

	/**
	 * Creates a {@link CreateClassPropertyEventHandler}.
	 */
	public CreateClassPropertyEventHandler(String controlID) {
		_controlID = controlID;
	}

	@Override
	public void call(Event event) {
		GraphPartCommandArguments arguments = JavaScriptObject.createObject().cast();

		arguments.setID(getNodeID((DefaultNode) getShape(event).getSharedGraphPart()));
		arguments.setControlID(_controlID);
		arguments.setControlCommand(DiagramJSGraphControlCommon.CREATE_CLASS_PROPERTY_COMMAND);

		AJAX.execute(COMMAND_ID_DISPATCH, arguments);
	}

	private String getNodeID(DefaultNode sharedGraphPart) {
		return sharedGraphPart.data().id();
	}

	private native Shape getShape(Event event) /*-{
		return event.context.shape;
	}-*/;

}

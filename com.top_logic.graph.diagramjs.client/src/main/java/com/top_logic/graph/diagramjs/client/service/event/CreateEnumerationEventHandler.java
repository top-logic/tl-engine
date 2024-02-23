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
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.graph.diagramjs.client.ajax.CreateNodeCommandArguments;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;

/**
 * {@link EventHandler} to trigger server side class node creation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class CreateEnumerationEventHandler implements EventHandler {

	private static final String COMMAND_ID_DISPATCH = "dispatchControlCommand";

	String _controlID;

	/**
	 * Creates a {@link CreateEnumerationEventHandler}.
	 */
	public CreateEnumerationEventHandler(String controlID) {
		_controlID = controlID;
	}

	@Override
	public void call(Event event) {
		CreateNodeCommandArguments arguments = JavaScriptObject.createObject().cast();

		Bounds bounds = getBounds(event);

		Position position = bounds.getPosition();
		Dimension dimension = bounds.getDimension();

		arguments.setX(position.getX());
		arguments.setY(position.getY());
		arguments.setWidth(dimension.getWidth());
		arguments.setHeight(dimension.getHeight());
		arguments.setControlID(_controlID);
		arguments.setControlCommand(DiagramJSGraphControlCommon.CREATE_ENUMERATION_COMMAND);

		AJAX.execute(COMMAND_ID_DISPATCH, arguments);
	}

	private native Bounds getBounds(Event event) /*-{
		return event.context.bounds;
	}-*/;

}

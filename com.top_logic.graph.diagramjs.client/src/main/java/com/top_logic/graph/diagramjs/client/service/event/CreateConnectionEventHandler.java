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
import com.top_logic.client.diagramjs.model.util.ConnectionContext;
import com.top_logic.graph.common.model.impl.DefaultNode;
import com.top_logic.graph.diagramjs.client.ajax.CreateConnectionCommandArguments;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;

/**
 * Triggers server side create connection dialog.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class CreateConnectionEventHandler implements EventHandler {

	private static final String COMMAND_ID_DISPATCH = "dispatchControlCommand";

	String _controlID;

	/**
	 * Creates a {@link CreateConnectionEventHandler}.
	 */
	public CreateConnectionEventHandler(String controlID) {
		_controlID = controlID;
	}

	@Override
	public void call(Event event) {
		CreateConnectionCommandArguments arguments = JavaScriptObject.createObject().cast();

		ConnectionContext connectionContext = getCreateConnectionContext(event);

		String sourceID = getSourceNodeID(connectionContext);
		String targetID = getTargetNodeID(connectionContext);

		arguments.setType(connectionContext.getType());
		arguments.setSourceID(sourceID);
		arguments.setTargetID(targetID);
		arguments.setControlID(_controlID);
		arguments.setControlCommand(DiagramJSGraphControlCommon.CREATE_CONNECTION_COMMAND);

		AJAX.execute(COMMAND_ID_DISPATCH, arguments);
	}

	private String getTargetNodeID(ConnectionContext connectionContext) {
		return getNodeID((DefaultNode) connectionContext.getTarget().getSharedGraphPart());
	}

	private String getSourceNodeID(ConnectionContext connectionContext) {
		return getNodeID((DefaultNode) connectionContext.getSource().getSharedGraphPart());
	}

	private String getNodeID(DefaultNode sharedGraphPart) {
		return sharedGraphPart.data().id();
	}

	private native ConnectionContext getCreateConnectionContext(Event event) /*-{
		return event.context;
	}-*/;

}

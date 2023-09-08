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
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.graph.diagramjs.client.ajax.GraphPartCommandArguments;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;

/**
 * Handler to delete graph parts.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class DeleteGraphPartEventHandler implements EventHandler {

	private static final String COMMAND_ID_DISPATCH = "dispatchControlCommand";

	String _controlID;

	/**
	 * Creates a {@link DeleteGraphPartEventHandler}.
	 */
	public DeleteGraphPartEventHandler(String controlID) {
		_controlID = controlID;
	}

	@Override
	public void call(Event event) {
		Base element = getElement(event);

		GraphPartCommandArguments arguments = JavaScriptObject.createObject().cast();

		arguments.setID(getID((DefaultSharedObject) element.getSharedGraphPart()));
		arguments.setControlID(_controlID);
		arguments.setControlCommand(DiagramJSGraphControlCommon.DELETE_GRAPH_PART_COMMAND);

		AJAX.execute(COMMAND_ID_DISPATCH, arguments);
	}

	private String getID(DefaultSharedObject sharedGraphPart) {
		return sharedGraphPart.data().id();
	}

	private native Base getElement(Event event) /*-{
		return event.context.element;
	}-*/;

}

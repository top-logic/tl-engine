/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAX;
import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.graph.diagramjs.client.ajax.VisibilityCommandArguments;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;

/**
 * Handler that is executed when the user toggles a diagram elements visibility.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public final class ElementVisibilityEventHandler implements EventHandler {

	String _controlID;

	/**
	 * Creates a {@link ElementVisibilityEventHandler}.
	 */
	public ElementVisibilityEventHandler(String controlID) {
		_controlID = controlID;
	}

	@Override
	public void call(Event event) {
		VisibilityCommandArguments arguments = JavaScriptObject.createObject().cast();

		Base[] elements = getElement(event);
		List<String> ids = new ArrayList<>();
		for(int i = 0; i < elements.length; i++) {
			ids.add(getID((DefaultSharedObject) elements[i].getSharedGraphPart()));
		}
		arguments.setVisibility(getVisibility(event));
		arguments.setIDs(ids.toArray(new String[elements.length]));
		arguments.setControlID(_controlID);
		arguments.setControlCommand(DiagramJSGraphControlCommon.ELEMENT_VISIBILITY_COMMAND);

		AJAX.execute("dispatchControlCommand", arguments);
	}

	private String getID(DefaultSharedObject sharedGraphPart) {
		return sharedGraphPart.data().id();
	}

	private native Base[] getElement(Event event) /*-{
		return event.context.elements;
	}-*/;

	private native boolean getVisibility(Event event) /*-{
		return event.context.visibility;
	}-*/;

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event.listener;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import com.top_logic.graph.diagramjs.client.service.DiagramJSGraphControl;

/**
 * Dispatches to the graph control which handle the drop event.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DropEventListener implements EventListener {

	private final DiagramJSGraphControl _graphControl;

	/**
	 * Creates a {@link DropEventListener}.
	 */
	public DropEventListener(DiagramJSGraphControl graphControl) {
		_graphControl = graphControl;
	}

	@Override
	public void onBrowserEvent(Event event) {
		_graphControl.onDrop(event);
	}

}

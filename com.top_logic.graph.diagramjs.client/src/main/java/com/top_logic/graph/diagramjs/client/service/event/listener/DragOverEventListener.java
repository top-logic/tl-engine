/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event.listener;

import com.google.gwt.dom.client.DataTransfer.DropEffect;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * Stop event propagation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DragOverEventListener implements EventListener {

	/**
	 * Singleton instance.
	 */
	public static final DragOverEventListener INSTANCE = new DragOverEventListener();

	private DragOverEventListener() {
		// Singleton.
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		event.getDataTransfer().setDropEffect(DropEffect.COPY);
		
		event.stopPropagation();
		event.preventDefault();
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;

/**
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class ElementsMoveEventHandler implements EventHandler {

	@Override
	public void call(Event event) {
		Shape[] shapes = getShapes(event);

		for (int i = 0; i < shapes.length; i++) {
			Shape shape = shapes[i];

			DefaultDiagramJSClassNode node = (DefaultDiagramJSClassNode) shape.getSharedGraphPart();

			node.setX(shape.getX());
			node.setY(shape.getY());
		}

	}

	private native Shape[] getShapes(Event event) /*-{
		return event.context.shapes;
	}-*/;
}

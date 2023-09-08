/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;

/**
 * Handler for resized diagramJS objects.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class ShapeResizeEventHandler implements EventHandler {

	@Override
	public void call(Event event) {
		Shape shape = getShape(event);
		Bounds bounds = getBounds(event);

		Position position = bounds.getPosition();
		Dimension dimension = bounds.getDimension();

		DefaultDiagramJSClassNode node = (DefaultDiagramJSClassNode) shape.getSharedGraphPart();

		node.setHeight(dimension.getHeight());
		node.setWidth(dimension.getWidth());
		node.setX(position.getX());
		node.setY(position.getY());
	}

	private native Shape getShape(Event event) /*-{
		return event.context.shape;
	}-*/;

	private native Bounds getBounds(Event event) /*-{
		return event.context.newBounds;
	}-*/;
}

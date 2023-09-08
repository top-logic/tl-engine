/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.util.Waypoint;
import com.top_logic.client.diagramjs.util.DiagramUtil;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSEdge;

/**
 * Handler to update connection waypoints.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class UpdatedWaypointsEventHandler implements EventHandler {

	@Override
	public void call(Event event) {
		Connection connection = getConnection(event);

		DefaultDiagramJSEdge sharedGraphPart = (DefaultDiagramJSEdge) connection.getSharedGraphPart();

		sharedGraphPart.setWaypoints(DiagramUtil.transformWaypoints(getNewWaypoints(event)));
	}

	native Waypoint[] getNewWaypoints(Event event) /*-{
		return event.context.connection.waypoints;
	}-*/;

	native Connection getConnection(Event event) /*-{
		return event.context.connection;
	}-*/;

}

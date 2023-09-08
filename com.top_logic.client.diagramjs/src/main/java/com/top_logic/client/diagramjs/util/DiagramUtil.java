/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.util.Waypoint;

/**
 * General diagram utilities.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramUtil {

	/**
	 * @see #transformWaypoints(List)
	 */
	public static List<List<Double>> transformWaypoints(Waypoint[] newWaypoints) {
		return transformWaypoints(Arrays.asList(newWaypoints));
	}

	/**
	 * Transform a list of waypoints, got by umljs, to a list of a list with two elements (x and y),
	 * to use it in the shared graph model.
	 */
	public static List<List<Double>> transformWaypoints(List<Waypoint> waypoints) {
		return waypoints.stream().map(waypoint -> {
			return Arrays.asList(waypoint.getX(), waypoint.getY());
		}).collect(Collectors.toList());
	}

	/**
	 * Reversed transformation of {@link #transformWaypoints(List)}
	 */
	public static Waypoint[] transformWaypointsBack(List<List<Double>> waypoints) {
		return waypoints.stream().map(waypoint -> {
			Waypoint waypointObject = JavaScriptObject.createObject().cast();

			waypointObject.setX(waypoint.get(0));
			waypointObject.setY(waypoint.get(1));

			return waypointObject;
		}).collect(Collectors.toList()).toArray(new Waypoint[] {});
	}

}

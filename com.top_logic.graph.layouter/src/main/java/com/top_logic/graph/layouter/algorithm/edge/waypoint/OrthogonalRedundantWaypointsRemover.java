/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.edge.waypoint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.graph.layouter.model.Waypoint;

/**
 * Removes redunant waypoints.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class OrthogonalRedundantWaypointsRemover implements RemoveRedundantWaypointsAlgorithm {

	/**
	 * Two dimensional line directions.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public enum OrthogonalLineDirection {

		/**
		 * Straight line from bottom to top.
		 */
		TOP,

		/**
		 * Straight line from top to bottom.
		 */
		BOTTOM,

		/**
		 * Straight line from right to left.
		 */
		LEFT,

		/**
		 * Straight line from left to right.
		 */
		RIGHT,

		/**
		 * Not an orthogonal straight line.
		 */
		NOT_ORTHOGONAL
	}

	/**
	 * Singleton instance for {@link OrthogonalRedundantWaypointsRemover}.
	 */
	public static final OrthogonalRedundantWaypointsRemover INSTANCE = new OrthogonalRedundantWaypointsRemover();

	@Override
	public void removeRedundantWaypoints(List<Waypoint> waypoints) {
		Set<Waypoint> toRemovedItems = new HashSet<>();

		if (waypoints.size() > 2) {
			OrthogonalLineDirection currentDirection = getLineDirection(waypoints.get(0), waypoints.get(1));

			for (int i = 1; i + 1 < waypoints.size(); i++) {
				OrthogonalLineDirection nextDirection = getLineDirection(waypoints.get(i), waypoints.get(i + 1));

				if (nextDirection == currentDirection) {
					toRemovedItems.add(waypoints.get(i));
				} else {
					currentDirection = nextDirection;
				}
			}
		}

		waypoints.removeAll(toRemovedItems);
	}

	private OrthogonalLineDirection getLineDirection(Waypoint waypoint1, Waypoint waypoint2) {
		if (waypoint1.getX() == waypoint2.getX()) {
			if (waypoint1.getY() < waypoint2.getY()) {
				return OrthogonalLineDirection.BOTTOM;
			} else {
				return OrthogonalLineDirection.TOP;
			}
		} else if (waypoint1.getY() == waypoint2.getY()) {
			if (waypoint1.getX() < waypoint2.getX()) {
				return OrthogonalLineDirection.RIGHT;
			} else {
				return OrthogonalLineDirection.LEFT;
			}
		} else {
			return OrthogonalLineDirection.NOT_ORTHOGONAL;
		}
	}

}

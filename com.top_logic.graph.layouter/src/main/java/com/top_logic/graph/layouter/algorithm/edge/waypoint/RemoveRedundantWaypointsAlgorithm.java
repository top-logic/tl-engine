/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.edge.waypoint;

import java.util.List;

import com.top_logic.graph.layouter.model.Waypoint;

/**
 * Algorithm to remove redunant waypoints.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface RemoveRedundantWaypointsAlgorithm {

	/**
	 * Removes redunant waypoints for the given waypoints.
	 */
	void removeRedundantWaypoints(List<Waypoint> waypoints);

}

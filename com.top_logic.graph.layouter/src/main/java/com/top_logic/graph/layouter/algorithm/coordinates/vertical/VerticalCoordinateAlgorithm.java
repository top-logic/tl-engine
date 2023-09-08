/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.vertical;

import java.util.Map;

import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;

/**
 * Algorithm to set the vertical coordinates for the given layering.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface VerticalCoordinateAlgorithm {
	/**
	 * @param ordering
	 *        Ordered layering of a graph.
	 */
	void setVerticalNodeCoordinates(Map<Integer, DefaultAlternatingLayer> ordering);
}

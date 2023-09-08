/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.crossing;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;

/**
 * Crossing between two edges.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CrossingEdge {
	private Object _firstEdge;

	private Object _secondEdge;

	private int _weight;

	/**
	 * Creates a {@link CrossingEdge} between the given edges.
	 */
	public CrossingEdge(Object firstEdge, Object secondEdge) {
		_firstEdge = firstEdge;
		_secondEdge = secondEdge;

		_weight = getCrossingWeight(firstEdge, secondEdge);
	}

	/**
	 * Crossing weight, product of weights of each edge. A {@link VirtualSegmentEdge} has
	 *         the number of segments in the corresponding {@link SegmentContainer} as weight,
	 *         otherwise the weight is 1.
	 */
	public int getWeight() {
		return _weight;
	}

	/**
	 * @see #getWeight()
	 */
	public void setWeight(int weight) {
		_weight = weight;
	}

	/**
	 * Second object of this crossing.
	 */
	public Object getSecondEdge() {
		return _secondEdge;
	}

	/**
	 * @see CrossingEdge#getSecondEdge()
	 */
	public void setSecondEdge(LayoutEdge secondEdge) {
		_secondEdge = secondEdge;
	}

	/**
	 * First object of this crossing.
	 */
	public Object getFirstEdge() {
		return _firstEdge;
	}

	/**
	 * @see #getFirstEdge()
	 */
	public void setFirstEdge(LayoutEdge firstEdge) {
		_firstEdge = firstEdge;
	}

	private int getCrossingWeight(Object firstEdge, Object secondEdge) {
		int firstEdgeWeight = getWeight(firstEdge);
		int secondEdgeWeight = getWeight(secondEdge);

		return firstEdgeWeight * secondEdgeWeight;
	}

	private int getWeight(Object edge) {
		if (edge instanceof LayoutEdge) {
			return 1;
		} else if (edge instanceof VirtualSegmentEdge) {
			return ((VirtualSegmentEdge) edge).getWeight();
		} else {
			return 0;
		}
	}
}

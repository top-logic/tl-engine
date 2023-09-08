/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;
import java.util.Map;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link Comparator} for {@link LayoutNode} according to their measure.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutNodeMeasureComparator implements Comparator<LayoutNode> {

	private Map<LayoutNode, Double> _measure;

	/**
	 * Creates a {@link LayoutNodeMeasureComparator} for the given measure.
	 */
	public LayoutNodeMeasureComparator(Map<LayoutNode, Double> measure) {
		_measure = measure;
	}

	@Override
	public int compare(LayoutNode firstNode, LayoutNode secondNode) {
		if (_measure.get(firstNode) < _measure.get(secondNode)) {
			return -1;
		} else if (_measure.get(firstNode) > _measure.get(secondNode)) {
			return 1;
		} else {
			return 0;
		}
	}

}

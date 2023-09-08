/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.layer.VirtualSegmentEdge;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Compares {@link LayoutEdge}s lexicographic from bottom to top.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LexicographicEdgeReversedComparator implements Comparator<Object> {
	Map<Object, Integer> _targetOrder;

	List<Object> _sourceOrder;

	LayoutDirection _direction;

	/**
	 * Creates a {@link LexicographicEdgeComparator}.
	 */
	public LexicographicEdgeReversedComparator(Map<Object, Integer> targetOrder, List<Object> sourceOrder,
			LayoutDirection direction) {
		_targetOrder = targetOrder;
		_sourceOrder = sourceOrder;

		_direction = direction;
	}

	@Override
	public int compare(Object firstEdge, Object secondEdge) {
		int firstSourceOrder = _sourceOrder.indexOf(getEdgeSource(firstEdge));
		int secondSourceOrder = _sourceOrder.indexOf(getEdgeSource(secondEdge));

		if (firstSourceOrder < secondSourceOrder) {
			return -1;
		} else if (firstSourceOrder == secondSourceOrder) {
			return compareTargets(firstEdge, secondEdge);
		} else {
			return 1;
		}
	}

	private int compareTargets(Object firstEdge, Object secondEdge) {
		int firstTargetOrder = _targetOrder.get(getEdgeTarget(firstEdge));
		int secondTargetOrder = _targetOrder.get(getEdgeTarget(secondEdge));

		if (firstTargetOrder < secondTargetOrder) {
			return -1;
		} else {
			return 1;
		}
	}

	private Object getEdgeSource(Object edge) {
		if (edge instanceof LayoutEdge) {
			return LayoutGraphUtil.getTopNode(_direction, (LayoutEdge) edge);
		} else if (edge instanceof VirtualSegmentEdge) {
			return LayoutGraphUtil.getTopNode(_direction, (VirtualSegmentEdge) edge);
		} else {
			return null;
		}
	}

	private Object getEdgeTarget(Object edge) {
		if (edge instanceof LayoutEdge) {
			return LayoutGraphUtil.getBottomNode(_direction, (LayoutEdge) edge);
		} else if (edge instanceof VirtualSegmentEdge) {
			return LayoutGraphUtil.getBottomNode(_direction, (VirtualSegmentEdge) edge);
		} else {
			return null;
		}
	}
}

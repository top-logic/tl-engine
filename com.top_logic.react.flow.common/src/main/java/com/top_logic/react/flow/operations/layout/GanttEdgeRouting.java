/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.react.flow.data.GanttLayout;

/**
 * Places the horizontal segments of the dependency edges of a {@link GanttLayout}.
 *
 * <p>
 * An edge leaves its source item with a horizontal stub, runs vertically to the height of its
 * horizontal segment, horizontally to the stub of its target, and vertically to the target. The
 * horizontal segment of an edge between two different rows lies in the gap between the source
 * row and its neighbor towards the target row (below the source row if the target is below,
 * otherwise above): the bottom padding of the upper and the top padding of the lower row. So an
 * edge between non-adjacent rows does not cross the items of the rows in between.
 * </p>
 *
 * <p>
 * Edges whose horizontal segments pass through the same gap with overlapping X ranges are put on
 * separate lanes, centered in the gap, at most
 * {@link GanttLayoutOperations#EDGE_LANE_SPACING} apart. The row heights do not change. The
 * horizontal segment of an edge within a single row (or with an endpoint in an unknown row)
 * runs halfway between the heights of its endpoints.
 * </p>
 */
final class GanttEdgeRouting {

	/**
	 * The horizontal segment of an edge.
	 */
	static final class Segment {

		private final double _minX;

		private final double _maxX;

		private double _y;

		private int _lane;

		Segment(double x1, double x2, double y) {
			_minX = Math.min(x1, x2);
			_maxX = Math.max(x1, x2);
			_y = y;
		}

		/** The Y coordinate of the segment, valid after {@link GanttEdgeRouting#route()}. */
		double y() {
			return _y;
		}
	}

	private final GanttRowLayout _rows;

	/** The segments passing through each gap, by the index of the row above the gap. */
	private final Map<Integer, List<Segment>> _segmentsByGap = new HashMap<>();

	/**
	 * Creates a {@link GanttEdgeRouting}.
	 *
	 * @param rows
	 *        The placed rows of the chart.
	 */
	GanttEdgeRouting(GanttRowLayout rows) {
		_rows = rows;
	}

	/**
	 * Adds the horizontal segment of an edge.
	 *
	 * @param sourceRow
	 *        The display index of the row of the source item, -1 if unknown.
	 * @param targetRow
	 *        The display index of the row of the target item, -1 if unknown.
	 * @param x1
	 *        The X coordinate where the segment starts (the end of the source stub).
	 * @param x2
	 *        The X coordinate where the segment ends (the end of the target stub).
	 * @param sourceY
	 *        The Y coordinate of the source endpoint.
	 * @param targetY
	 *        The Y coordinate of the target endpoint.
	 * @return The segment, whose {@link Segment#y()} is set by {@link #route()}.
	 */
	Segment add(int sourceRow, int targetRow, double x1, double x2, double sourceY, double targetY) {
		Segment segment = new Segment(x1, x2, (sourceY + targetY) / 2.0);
		if (sourceRow >= 0 && targetRow >= 0 && sourceRow != targetRow) {
			int gap = targetRow > sourceRow ? sourceRow : sourceRow - 1;
			_segmentsByGap.computeIfAbsent(Integer.valueOf(gap), key -> new ArrayList<>()).add(segment);
		}
		return segment;
	}

	/**
	 * Assigns the lanes in each gap and sets the Y coordinates of the segments.
	 */
	void route() {
		for (Map.Entry<Integer, List<Segment>> entry : _segmentsByGap.entrySet()) {
			routeGap(entry.getKey().intValue(), entry.getValue());
		}
	}

	private void routeGap(int gap, List<Segment> segments) {
		int laneCount = assignLanes(segments);

		double top = _rows.gapTop(gap);
		double bottom = _rows.gapBottom(gap);
		double height = bottom - top;
		double spacing = Math.max(0, Math.min(GanttLayoutOperations.EDGE_LANE_SPACING, height / (laneCount + 1)));
		double center = (top + bottom) / 2.0;
		for (Segment segment : segments) {
			segment._y = center + (segment._lane - (laneCount - 1) / 2.0) * spacing;
		}
	}

	/**
	 * Distributes the given segments greedily on lanes, so that segments with overlapping X ranges
	 * are on different lanes.
	 *
	 * @return The number of lanes used.
	 */
	private static int assignLanes(List<Segment> segments) {
		List<Segment> sorted = new ArrayList<>(segments);
		sorted.sort((a, b) -> {
			int result = Double.compare(a._minX, b._minX);
			return result != 0 ? result : Double.compare(a._maxX, b._maxX);
		});

		// The largest X coordinate occupied on each lane.
		List<Double> laneEnds = new ArrayList<>();
		for (Segment segment : sorted) {
			int lane = 0;
			while (lane < laneEnds.size() && laneEnds.get(lane).doubleValue() >= segment._minX) {
				lane++;
			}
			if (lane == laneEnds.size()) {
				laneEnds.add(Double.valueOf(segment._maxX));
			} else {
				laneEnds.set(lane, Double.valueOf(segment._maxX));
			}
			segment._lane = lane;
		}
		return laneEnds.size();
	}

}

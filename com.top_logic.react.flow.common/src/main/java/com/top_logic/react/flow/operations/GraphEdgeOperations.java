/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.List;

import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.data.OffsetPosition;
import com.top_logic.react.flow.operations.util.DiagramUtil;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for a {@link GraphEdge}.
 */
public interface GraphEdgeOperations extends WidgetOperations {

	@Override
	GraphEdge self();

	@Override
	default void draw(SvgWriter out) {
		List<GraphWaypoint> waypoints = self().getWaypoints();
		if (waypoints.size() < 2) {
			return;
		}

		out.beginGroup(self());

		out.beginPath();
		setStroke(out);
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(DiagramUtil.doubleArray(self().getDashes()));
		}
		out.setFill("none");
		out.beginData();
		{
			GraphWaypoint first = waypoints.get(0);
			out.moveToAbs(first.getX(), first.getY());
			for (int n = 1, cnt = waypoints.size(); n < cnt; n++) {
				GraphWaypoint wp = waypoints.get(n);
				out.lineToAbs(wp.getX(), wp.getY());
			}
		}
		out.endData();
		out.endPath();

		drawDecorations(out, waypoints);

		out.endGroup();
	}

	/**
	 * Draws all decorations placed along the edge.
	 */
	default void drawDecorations(SvgWriter out, List<GraphWaypoint> waypoints) {
		List<EdgeDecoration> decorations = self().getDecorations();
		if (decorations.isEmpty()) {
			return;
		}

		// Pre-compute cumulative segment lengths for position interpolation.
		int cnt = waypoints.size();
		double[] segLen = new double[cnt - 1];
		double totalLen = 0.0;
		for (int n = 0; n < cnt - 1; n++) {
			GraphWaypoint a = waypoints.get(n);
			GraphWaypoint b = waypoints.get(n + 1);
			double dx = b.getX() - a.getX();
			double dy = b.getY() - a.getY();
			segLen[n] = Math.sqrt(dx * dx + dy * dy);
			totalLen += segLen[n];
		}

		for (EdgeDecoration decoration : decorations) {
			double[] pos = positionOnPolyline(waypoints, segLen, totalLen, decoration.getLinePosition());
			double x = pos[0];
			double y = pos[1];

			// Adjust for offsetPosition within the decoration content box
			if (decoration.hasContent()) {
				x += offsetX(decoration.getOffsetPosition(), decoration.getContent().getWidth());
				y += offsetY(decoration.getOffsetPosition(), decoration.getContent().getHeight());
			}

			out.beginGroup();
			out.translate(x, y);
			{
				decoration.draw(out);
			}
			out.endGroup();
		}
	}

	/**
	 * Computes absolute [x,y] coordinates at a given ratio along a polyline.
	 *
	 * @param waypoints
	 *        The waypoints of the polyline.
	 * @param segLen
	 *        Pre-computed segment lengths.
	 * @param totalLen
	 *        Total polyline length.
	 * @param ratio
	 *        A value in [0, 1] where 0 is the start and 1 is the end.
	 * @return A two-element array [x, y].
	 */
	default double[] positionOnPolyline(List<GraphWaypoint> waypoints, double[] segLen, double totalLen, double ratio) {
		if (totalLen == 0) {
			GraphWaypoint wp = waypoints.get(0);
			return new double[] { wp.getX(), wp.getY() };
		}
		double target = ratio * totalLen;
		double accumulated = 0.0;
		for (int n = 0; n < segLen.length; n++) {
			double next = accumulated + segLen[n];
			if (next >= target || n == segLen.length - 1) {
				double t = (segLen[n] == 0) ? 0 : (target - accumulated) / segLen[n];
				GraphWaypoint a = waypoints.get(n);
				GraphWaypoint b = waypoints.get(n + 1);
				return new double[] {
					a.getX() + t * (b.getX() - a.getX()),
					a.getY() + t * (b.getY() - a.getY())
				};
			}
			accumulated = next;
		}
		GraphWaypoint last = waypoints.get(waypoints.size() - 1);
		return new double[] { last.getX(), last.getY() };
	}

	/**
	 * Placement offset in X direction for an {@link OffsetPosition}.
	 */
	default double offsetX(OffsetPosition offsetPosition, double width) {
		switch (offsetPosition) {
			case TOP_LEFT:
			case CENTER_LEFT:
			case BOTTOM_LEFT:
				return 0;
			case TOP_RIGHT:
			case CENTER_RIGHT:
			case BOTTOM_RIGHT:
				return -width;
			case CENTER_TOP:
			case CENTER:
			case CENTER_BOTTOM:
				return -width / 2;
		}
		throw new IllegalArgumentException("No such position: " + offsetPosition);
	}

	/**
	 * Placement offset in Y direction for an {@link OffsetPosition}.
	 */
	default double offsetY(OffsetPosition offsetPosition, double height) {
		switch (offsetPosition) {
			case TOP_LEFT:
			case TOP_RIGHT:
			case CENTER_TOP:
				return 0;
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
			case CENTER_BOTTOM:
				return -height;
			case CENTER_LEFT:
			case CENTER_RIGHT:
			case CENTER:
				return -height / 2;
		}
		throw new IllegalArgumentException("No such position: " + offsetPosition);
	}

	/**
	 * Sets the connection stroke style.
	 */
	default void setStroke(SvgWriter out) {
		out.setStrokeWidth(self().getThickness());
		String strokeStyle = self().getStrokeStyle();
		if (strokeStyle != null && !strokeStyle.isEmpty()) {
			out.setStroke(strokeStyle);
		}
	}

}

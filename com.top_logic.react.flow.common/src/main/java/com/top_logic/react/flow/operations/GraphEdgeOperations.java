/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.data.OffsetPosition;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.util.DiagramUtil;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.svg.event.MouseButton;
import com.top_logic.react.flow.svg.event.Registration;
import com.top_logic.react.flow.svg.event.SVGClickEvent;
import com.top_logic.react.flow.svg.event.SVGClickHandler;

/**
 * Operations for a {@link GraphEdge}.
 *
 * <p>
 * When a {@link GraphEdge#getUserObject() userObject} is set, the edge becomes selectable: a wider
 * invisible click target is drawn along the polyline, and clicking it updates the
 * {@link Diagram#getSelection() diagram selection}.
 * </p>
 */
public interface GraphEdgeOperations extends WidgetOperations, SVGClickHandler {

	@Override
	GraphEdge self();

	/** Minimum click target width in pixels for edge selection. */
	static final double CLICK_TARGET_WIDTH = 10;

	@Override
	default void draw(SvgWriter out) {
		List<GraphWaypoint> waypoints = self().getWaypoints();
		if (waypoints.size() < 2) {
			return;
		}

		ConnectorSymbol sourceSymbol = self().getSourceSymbol();
		ConnectorSymbol targetSymbol = self().getTargetSymbol();
		double thickness = self().getThickness();
		String strokeStyle = self().getStrokeStyle();
		double scale = thickness / 2;
		boolean selectable = self().getUserObject() != null;

		// Cancel previous click handler.
		Registration clickHandler = self().getClickHandler();
		if (clickHandler != null) {
			clickHandler.cancel();
		}

		out.beginGroup(self());

		if (selectable) {
			String cssClass = self().isSelected()
				? SelectableBoxOperations.TL_SELECTED
				: SelectableBoxOperations.TL_CAN_SELECT;
			if (!StringServicesShared.isEmpty(self().getCssClass())) {
				cssClass += " " + self().getCssClass();
			}
			out.writeCssClass(cssClass);
			self().setClickHandler(out.attachOnClick(this, self()));
		}

		// Draw the polyline path, shortened by symbol insets at both ends.
		out.beginPath();
		setStroke(out);
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(DiagramUtil.doubleArray(self().getDashes()));
		}
		out.setFill("none");
		out.beginData();
		{
			// First point, shifted by source symbol inset.
			GraphWaypoint first = waypoints.get(0);
			GraphWaypoint second = waypoints.get(1);
			double srcInset = ConnectorSymbolRenderer.inset(sourceSymbol, scale);
			double[] srcDir = segmentDirection(first, second);
			out.moveToAbs(first.getX() + srcDir[0] * srcInset, first.getY() + srcDir[1] * srcInset);

			// Middle points.
			for (int n = 1, cnt = waypoints.size() - 1; n < cnt; n++) {
				GraphWaypoint wp = waypoints.get(n);
				out.lineToAbs(wp.getX(), wp.getY());
			}

			// Last point, shifted by target symbol inset.
			GraphWaypoint last = waypoints.get(waypoints.size() - 1);
			GraphWaypoint beforeLast = waypoints.get(waypoints.size() - 2);
			double tgtInset = ConnectorSymbolRenderer.inset(targetSymbol, scale);
			double[] tgtDir = segmentDirection(last, beforeLast);
			out.lineToAbs(last.getX() + tgtDir[0] * tgtInset, last.getY() + tgtDir[1] * tgtInset);
		}
		out.endData();
		out.endPath();

		// Draw symbols at endpoints.
		if (sourceSymbol != null && sourceSymbol != ConnectorSymbol.NONE) {
			GraphWaypoint first = waypoints.get(0);
			GraphWaypoint second = waypoints.get(1);
			double[] dir = segmentDirection(second, first);
			ConnectorSymbolRenderer.drawSymbol(out,
				first.getX(), first.getY(), dir[0], dir[1],
				sourceSymbol, strokeStyle, thickness);
		}

		if (targetSymbol != null && targetSymbol != ConnectorSymbol.NONE) {
			GraphWaypoint last = waypoints.get(waypoints.size() - 1);
			GraphWaypoint beforeLast = waypoints.get(waypoints.size() - 2);
			double[] dir = segmentDirection(beforeLast, last);
			ConnectorSymbolRenderer.drawSymbol(out,
				last.getX(), last.getY(), dir[0], dir[1],
				targetSymbol, strokeStyle, thickness);
		}

		// Draw invisible wider click target for easier edge selection.
		// pointer-events:all is required because transparent strokes are not "visible"
		// and SVG's default pointer-events:visiblePainted would ignore them.
		if (selectable) {
			out.beginPath();
			out.writeAttribute("pointer-events", "all");
			out.setStrokeWidth(Math.max(CLICK_TARGET_WIDTH, thickness));
			out.setStroke("transparent");
			out.setFill("none");
			out.beginData();
			{
				GraphWaypoint first = waypoints.get(0);
				out.moveToAbs(first.getX(), first.getY());
				for (int n = 1; n < waypoints.size(); n++) {
					GraphWaypoint wp = waypoints.get(n);
					out.lineToAbs(wp.getX(), wp.getY());
				}
			}
			out.endData();
			out.endPath();
		}

		drawDecorations(out, waypoints);

		out.endGroup();
	}

	@Override
	default void onClick(SVGClickEvent event) {
		if (!event.getButton(MouseButton.LEFT)) {
			return;
		}

		// Navigate to the Diagram via the source node (GraphEdge is not in the Box hierarchy).
		Box source = self().getSource();
		if (source == null) {
			return;
		}
		Diagram diagram = source.getDiagram();
		if (diagram == null) {
			return;
		}

		if (self().isSelected()) {
			if (event.isCtrlKey()) {
				diagram.getSelection().remove(self());
				self().setSelected(false);
			} else if (!event.isShiftKey()) {
				for (Widget selected : diagram.getSelection()) {
					if (selected != self()) {
						SelectionUtil.setSelected(selected, false);
					}
				}
				diagram.setSelection(Collections.singletonList(self()));
			}
		} else {
			if (diagram.isMultiSelect() && (event.isShiftKey() || event.isCtrlKey())) {
				diagram.getSelection().add(self());
			} else {
				for (Widget selected : diagram.getSelection()) {
					SelectionUtil.setSelected(selected, false);
				}
				diagram.setSelection(Collections.singletonList(self()));
			}
			self().setSelected(true);
		}
		event.stopPropagation();
	}

	/**
	 * Normalized direction vector from point a to point b.
	 *
	 * @return [dx, dy] where each is -1, 0, or 1.
	 */
	default double[] segmentDirection(GraphWaypoint a, GraphWaypoint b) {
		double dx = b.getX() - a.getX();
		double dy = b.getY() - a.getY();
		return new double[] { Math.signum(dx), Math.signum(dy) };
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
			int[] segIndex = new int[1];
			double[] pos = positionOnPolyline(waypoints, segLen, totalLen, decoration.getLinePosition(), segIndex);
			double x = pos[0];
			double y = pos[1];

			// Determine the offset position: use explicit if set, otherwise auto-compute
			// from local edge direction. Convention: labels to the right of the edge.
			OffsetPosition offset = decoration.getOffsetPosition();
			if (offset == null || offset == OffsetPosition.CENTER) {
				offset = autoOffsetPosition(waypoints, segIndex[0], decoration.getLinePosition());
			}

			if (decoration.hasContent()) {
				x += offsetX(offset, decoration.getContent().getWidth());
				y += offsetY(offset, decoration.getContent().getHeight());
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
	 * Auto-computes the offset position based on local edge direction.
	 *
	 * <p>
	 * Convention: labels are placed to the right of the edge. The vertical placement depends on
	 * whether the decoration is near the start or end of the edge:
	 * </p>
	 * <ul>
	 * <li>Near start (linePosition &lt; 0.5): label below the source endpoint → TOP_LEFT</li>
	 * <li>Near end (linePosition &gt;= 0.5): label above the target endpoint → BOTTOM_LEFT</li>
	 * </ul>
	 *
	 * <p>
	 * For horizontal edge segments, left/right is determined by the segment direction.
	 * </p>
	 */
	default OffsetPosition autoOffsetPosition(List<GraphWaypoint> waypoints, int segIndex, double linePosition) {
		GraphWaypoint a = waypoints.get(segIndex);
		GraphWaypoint b = waypoints.get(Math.min(segIndex + 1, waypoints.size() - 1));
		double dx = b.getX() - a.getX();
		double dy = b.getY() - a.getY();

		boolean nearStart = linePosition < 0.5;

		if (Math.abs(dy) >= Math.abs(dx)) {
			// Vertical segment: labels to the right of the edge.
			if (nearStart) {
				// Near source: label below source node.
				return dy >= 0 ? OffsetPosition.TOP_LEFT : OffsetPosition.BOTTOM_LEFT;
			} else {
				// Near target: label above target node.
				return dy >= 0 ? OffsetPosition.BOTTOM_LEFT : OffsetPosition.TOP_LEFT;
			}
		} else {
			// Horizontal segment: labels below the edge.
			if (nearStart) {
				return dx >= 0 ? OffsetPosition.TOP_LEFT : OffsetPosition.TOP_RIGHT;
			} else {
				return dx >= 0 ? OffsetPosition.TOP_RIGHT : OffsetPosition.TOP_LEFT;
			}
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
	 * @param outSegIndex
	 *        If non-null, the segment index at the computed position is written to index 0.
	 * @return A two-element array [x, y].
	 */
	default double[] positionOnPolyline(List<GraphWaypoint> waypoints, double[] segLen, double totalLen,
			double ratio, int[] outSegIndex) {
		if (totalLen == 0) {
			if (outSegIndex != null) {
				outSegIndex[0] = 0;
			}
			GraphWaypoint wp = waypoints.get(0);
			return new double[] { wp.getX(), wp.getY() };
		}
		double target = ratio * totalLen;
		double accumulated = 0.0;
		for (int n = 0; n < segLen.length; n++) {
			double next = accumulated + segLen[n];
			if (next >= target || n == segLen.length - 1) {
				if (outSegIndex != null) {
					outSegIndex[0] = n;
				}
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
		if (outSegIndex != null) {
			outSegIndex[0] = segLen.length - 1;
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

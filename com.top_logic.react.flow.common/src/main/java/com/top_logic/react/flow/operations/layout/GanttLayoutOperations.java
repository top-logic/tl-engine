/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttMilestone;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Layout and rendering operations for {@link GanttLayout}.
 *
 * <p>
 * Places {@link GanttItem} boxes along the X axis according to their {@code start}/{@code end}
 * (or {@code at}) positions, and along the Y axis according to the item's row. Renders axis
 * ticks from {@code axis.currentTicks}, draws row-lane backgrounds, edges (orthogonal), and
 * decorations.
 * </p>
 *
 * <p>
 * Layout uses a two-pass algorithm: pass 1 calls
 * {@link Box#computeIntrinsicSize(RenderContext, double, double)} on every item box and records
 * per-row maximum intrinsic heights. Pass 2 calls
 * {@link Box#distributeSize(RenderContext, double, double, double, double)} on every item box
 * with the final cell dimensions so that all boxes in the same row grow to the same height.
 * Span widths are forced to {@code (end - start) * zoom}; milestone widths are intrinsic.
 * </p>
 */
public interface GanttLayoutOperations extends BoxOperations {

	/** Horizontal stub length for orthogonal edge routing (pixels offset from item edge). */
	double EDGE_HORIZONTAL_STUB = 6.0;

	/** Stroke width for the decoration line marker. */
	double DECORATION_LINE_STROKE_WIDTH = 1.5;

	/** Stroke width for normal (STRICT / WARN) dependency edges. */
	double EDGE_STROKE_WIDTH_NORMAL = 1.2;

	/** Stroke width for informational (NONE) dependency edges. */
	double EDGE_STROKE_WIDTH_THIN = 0.8;

	/**
	 * Geometry of a single row: its Y start (inclusive), Y end (exclusive), and total height.
	 */
	record RowGeometry(double yStart, double yEnd) {

		/** Total height of the row (lane height). */
		double height() {
			return yEnd - yStart;
		}
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		GanttLayout self = (GanttLayout) this;
		GanttAxis axis = self.getAxis();

		// Build depth-first row index map (rowId -> 0-based index) and row list for depth lookup.
		Map<String, Integer> rowIndex = new LinkedHashMap<>();
		List<GanttRow> rowList = new java.util.ArrayList<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter, rowList);
		}
		int totalRows = counter[0];

		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double rowLabelMinWidth = self.getRowLabelMinWidth();
		double rowLabelPadding = self.getRowLabelPadding();
		double rowMinContentHeight = self.getRowMinContentHeight();
		double rowPadding = self.getRowPadding();
		double axisHeight = self.getAxisHeight();
		double indentWidth = self.getIndentWidth();

		// --- Pass 1: compute intrinsic sizes of item boxes, record per-row max content height ---
		double[] rowMaxContentHeight = new double[totalRows];
		for (int i = 0; i < totalRows; i++) {
			rowMaxContentHeight[i] = rowMinContentHeight;
		}

		// Use a preliminary label column width for rough x positions during intrinsic sizing.
		double tmpLabelWidth = rowLabelMinWidth;

		for (GanttItem item : self.getItems()) {
			Integer idx = rowIndex.get(item.getRowId());
			if (idx == null) {
				continue;
			}
			Box box = item.getBox();
			if (box == null) {
				continue;
			}
			// Provide a temporary position for intrinsic sizing (exact position comes in pass 2).
			double tmpX;
			double tmpY = offsetY + axisHeight + rowPadding; // rough y, refined in pass 2
			if (item instanceof GanttSpan span) {
				tmpX = offsetX + tmpLabelWidth + (span.getStart() - rangeMin) * zoom;
			} else if (item instanceof GanttMilestone ms) {
				tmpX = offsetX + tmpLabelWidth + (ms.getAt() - rangeMin) * zoom;
			} else {
				continue;
			}
			box.computeIntrinsicSize(context, tmpX, tmpY);
			double intrinsicHeight = box.getHeight();
			if (intrinsicHeight > rowMaxContentHeight[idx]) {
				rowMaxContentHeight[idx] = intrinsicHeight;
			}
		}

		// --- Pass 1b: compute intrinsic sizes of row label boxes; record max label width ---
		double maxLabelIntrinsicWidth = 0.0;
		for (int i = 0; i < totalRows; i++) {
			GanttRow row = rowList.get(i);
			Box label = row.getLabel();
			if (label == null) {
				continue;
			}
			int depth = rowDepth(row, self.getRootRows());
			// Label x is rough; what matters here is the intrinsic width.
			double tmpX = offsetX + rowLabelPadding + depth * indentWidth;
			double tmpY = offsetY + axisHeight + rowPadding;
			label.computeIntrinsicSize(context, tmpX, tmpY);
			double labelW = label.getWidth() + depth * indentWidth;
			if (labelW > maxLabelIntrinsicWidth) {
				maxLabelIntrinsicWidth = labelW;
			}
		}

		// --- Pass 1c: compute intrinsic sizes of tick label boxes ---
		if (axis != null) {
			for (GanttTick tick : axis.getCurrentTicks()) {
				Box tickLabel = tick.getLabel();
				if (tickLabel != null) {
					tickLabel.computeIntrinsicSize(context, 0, 0);
				}
			}
		}

		// --- Aggregate: compute column width and per-row total heights ---
		double columnWidth = Math.max(rowLabelMinWidth, maxLabelIntrinsicWidth + 2 * rowLabelPadding);

		double[] rowTotalHeight = new double[totalRows];
		double[] rowYStart = new double[totalRows];
		double cumulativeY = offsetY + axisHeight;
		for (int i = 0; i < totalRows; i++) {
			rowTotalHeight[i] = rowMaxContentHeight[i] + 2 * rowPadding;
			rowYStart[i] = cumulativeY;
			cumulativeY += rowTotalHeight[i];
		}
		double totalHeight = cumulativeY - offsetY;

		// --- Pass 2: distribute final positions and sizes for item boxes ---
		for (GanttItem item : self.getItems()) {
			Integer idx = rowIndex.get(item.getRowId());
			if (idx == null) {
				continue;
			}
			Box box = item.getBox();
			if (box == null) {
				continue;
			}
			double contentHeight = rowTotalHeight[idx] - 2 * rowPadding;
			double itemY = rowYStart[idx] + rowPadding;

			if (item instanceof GanttSpan span) {
				double x = offsetX + columnWidth + (span.getStart() - rangeMin) * zoom;
				double w = (span.getEnd() - span.getStart()) * zoom;
				box.distributeSize(context, x, itemY, w, contentHeight);
			} else if (item instanceof GanttMilestone ms) {
				// Milestone: intrinsic width, centred on 'at'.
				double r = contentHeight / 2.0;
				double cx = offsetX + columnWidth + (ms.getAt() - rangeMin) * zoom;
				double msW = box.getWidth(); // intrinsic width from pass 1
				if (msW <= 0) {
					msW = 2 * r;
				}
				double x = cx - msW / 2.0;
				box.distributeSize(context, x, itemY, msW, contentHeight);
			}
		}

		// --- Pass 2b: distribute final positions and sizes for row label boxes ---
		for (int i = 0; i < totalRows; i++) {
			GanttRow row = rowList.get(i);
			Box label = row.getLabel();
			if (label == null) {
				continue;
			}
			int depth = rowDepth(row, self.getRootRows());
			double labelX = offsetX + rowLabelPadding + depth * indentWidth;
			double labelY = rowYStart[i] + rowPadding;
			double labelW = columnWidth - 2 * rowLabelPadding - depth * indentWidth;
			double labelH = rowTotalHeight[i] - 2 * rowPadding;
			label.distributeSize(context, labelX, labelY, Math.max(0, labelW), labelH);
		}

		// --- Pass 2c: distribute final positions and sizes for tick label boxes ---
		if (axis != null) {
			double columnWidth2 = columnWidth;
			double x0 = offsetX + columnWidth2;
			for (GanttTick tick : axis.getCurrentTicks()) {
				Box tickLabel = tick.getLabel();
				if (tickLabel == null) {
					continue;
				}
				double tickX = x0 + (tick.getPosition() - rangeMin) * zoom;
				double labelW = tickLabel.getWidth();
				double labelH = tickLabel.getHeight();
				tickLabel.distributeSize(context, tickX + 2, offsetY + axisHeight - labelH - 2, labelW, labelH);
			}
		}

		self.setX(offsetX);
		self.setY(offsetY);
		self.setColumnWidth(columnWidth);
		self.setWidth(columnWidth + (axis.getRangeMax() - rangeMin) * zoom);
		self.setHeight(totalHeight);
	}

	/**
	 * Returns the depth of the given row within the row forest.
	 * Root rows have depth 0.
	 */
	private static int rowDepth(GanttRow target, List<GanttRow> roots) {
		for (GanttRow root : roots) {
			int d = rowDepthSearch(target, root, 0);
			if (d >= 0) {
				return d;
			}
		}
		return 0;
	}

	private static int rowDepthSearch(GanttRow target, GanttRow current, int depth) {
		if (current == target) {
			return depth;
		}
		for (GanttRow child : current.getChildren()) {
			int d = rowDepthSearch(target, child, depth + 1);
			if (d >= 0) {
				return d;
			}
		}
		return -1;
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		GanttLayout self = (GanttLayout) this;
		computeIntrinsicSize(context, offsetX, offsetY);
		if (width > self.getWidth()) {
			self.setWidth(width);
		}
		if (height > self.getHeight()) {
			self.setHeight(height);
		}
	}

	@Override
	default void draw(SvgWriter out) {
		GanttLayout self = (GanttLayout) this;
		drawAxis(self, out);
		drawRowLanes(self, out);
		drawDecorations(self, out);
		for (Box content : self.getContents()) {
			out.write(content);
		}
		drawEdges(self, out);
	}

	private static void drawAxis(GanttLayout self, SvgWriter out) {
		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double columnWidth = self.getColumnWidth();
		double x0 = self.getX() + columnWidth;
		double y0 = self.getY();
		double w = self.getWidth() - columnWidth;
		double h = self.getAxisHeight();

		// Header background rectangle.
		out.beginGroup();
		out.writeCssClass("tl-gantt-axis");

		out.beginRect(x0, y0, w, h);
		out.setFill("#f8f8f8");
		out.setStroke("#c0c0c0");
		out.setStrokeWidth(1.0);
		out.endRect();

		// For each tick: vertical line + label.
		List<GanttTick> ticks = axis.getCurrentTicks();
		for (GanttTick tick : ticks) {
			double emphasis = tick.getEmphasis();
			double strokeWidth = 0.5 + 1.5 * Math.min(1.0, Math.max(0.0, emphasis));
			double tickX = x0 + (tick.getPosition() - rangeMin) * zoom;

			// Vertical tick line from top to bottom of the axis header.
			out.beginPath();
			out.setStroke("#707070");
			out.setStrokeWidth(strokeWidth);
			out.setFill("none");
			out.beginData();
			out.moveToAbs(tickX, y0);
			out.lineToAbs(tickX, y0 + h);
			out.endData();
			out.endPath();

			// Tick label boxes are drawn via the standard contents dispatch in draw() — see
			// GanttLayout.contents which includes tick label boxes added by FlowFactory.gantt.
		}

		// Bottom border line for the axis header.
		out.beginPath();
		out.setStroke("#c0c0c0");
		out.setStrokeWidth(1.0);
		out.setFill("none");
		out.beginData();
		out.moveToAbs(x0, y0 + h);
		out.lineToAbs(x0 + w, y0 + h);
		out.endData();
		out.endPath();

		out.endGroup();
	}

	private static void drawRowLanes(GanttLayout self, SvgWriter out) {
		double x0 = self.getX();
		double y0 = self.getY() + self.getAxisHeight();
		double totalWidth = self.getWidth();
		double rowMinContentHeight = self.getRowMinContentHeight();
		double rowPadding = self.getRowPadding();
		double columnWidth = self.getColumnWidth();

		// Build row geometry map for per-row Y positions.
		Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, y0);

		out.beginGroup();
		out.writeCssClass("tl-gantt-lanes");

		int[] rowIndex = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			drawRowLane(root, x0, totalWidth, rowMinContentHeight, rowPadding, columnWidth,
				rowGeometry, out, rowIndex);
		}

		out.endGroup();
	}

	private static void drawRowLane(GanttRow row,
			double x0, double totalWidth, double rowMinContentHeight, double rowPadding,
			double columnWidth,
			Map<String, RowGeometry> rowGeometry, SvgWriter out, int[] rowIndex) {
		int idx = rowIndex[0]++;
		RowGeometry geom = rowGeometry.get(row.getId());
		double rowY;
		double rowHeight;
		if (geom != null) {
			rowY = geom.yStart();
			rowHeight = geom.height();
		} else {
			// Fallback (should not happen in normal usage).
			rowY = 0;
			rowHeight = rowMinContentHeight + 2 * rowPadding;
		}

		// Alternating background.
		String fillColor = (idx % 2 == 0) ? "#ffffff" : "#f5f5f5";
		out.beginRect(x0, rowY, totalWidth, rowHeight);
		out.setFill(fillColor);
		out.setStroke("#e0e0e0");
		out.setStrokeWidth(1.0);
		out.endRect();

		// Note: row labels are drawn via the standard contents dispatch in draw() — no inline text
		// rendering here. The label box was positioned by computeIntrinsicSize pass 2b.

		// Vertical separator between label column and chart area.
		double sepX = x0 + columnWidth;
		out.beginPath();
		out.setStroke("#e0e0e0");
		out.setStrokeWidth(1.0);
		out.setFill("none");
		out.beginData();
		out.moveToAbs(sepX, rowY);
		out.lineToAbs(sepX, rowY + rowHeight);
		out.endData();
		out.endPath();

		// Recurse into children.
		for (GanttRow child : row.getChildren()) {
			drawRowLane(child, x0, totalWidth, rowMinContentHeight, rowPadding, columnWidth,
				rowGeometry, out, rowIndex);
		}
	}

	private static void drawDecorations(GanttLayout self, SvgWriter out) {
		List<GanttDecoration> decorations = self.getDecorations();
		if (decorations == null || decorations.isEmpty()) {
			return;
		}

		// Build row geometry (uses already-laid-out item boxes after computeIntrinsicSize).
		double axisHeight = self.getAxisHeight();
		double lanesTop = self.getY() + axisHeight;
		Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, lanesTop);

		// Compute total chart height (from first row start to last row end).
		int totalRows = rowGeometry.size();
		double chartY0 = lanesTop;
		double chartY1 = lanesTop; // will be updated to the bottom of the last row
		if (!rowGeometry.isEmpty()) {
			for (RowGeometry g : rowGeometry.values()) {
				if (g.yEnd() > chartY1) {
					chartY1 = g.yEnd();
				}
			}
		} else {
			chartY1 = chartY0 + totalRows * (self.getRowMinContentHeight() + 2 * self.getRowPadding());
		}

		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double chartX0 = self.getX() + self.getColumnWidth();

		out.beginGroup();
		out.writeCssClass("tl-gantt-decorations");

		for (GanttDecoration decoration : decorations) {
			// Compute vertical extent.
			List<String> relevantFor = decoration.getRelevantFor();
			double y0;
			double y1;
			if (relevantFor == null || relevantFor.isEmpty()) {
				y0 = chartY0;
				y1 = chartY1;
			} else {
				double minY = Double.MAX_VALUE;
				double maxY = Double.MIN_VALUE;
				for (String rowId : relevantFor) {
					RowGeometry geom = rowGeometry.get(rowId);
					if (geom != null) {
						if (geom.yStart() < minY) {
							minY = geom.yStart();
						}
						if (geom.yEnd() > maxY) {
							maxY = geom.yEnd();
						}
					}
				}
				if (minY == Double.MAX_VALUE) {
					// No matching rows — fall back to full chart.
					y0 = chartY0;
					y1 = chartY1;
				} else {
					y0 = minY;
					y1 = maxY;
				}
			}

			if (decoration instanceof GanttLineDecoration line) {
				double x = chartX0 + (line.getAt() - rangeMin) * zoom;
				String color = line.getColor();
				if (color == null || color.isEmpty()) {
					color = "#606060";
				}

				out.beginPath();
				out.setStroke(color);
				out.setStrokeWidth(DECORATION_LINE_STROKE_WIDTH);
				out.setFill("none");
				out.beginData();
				out.moveToAbs(x, y0);
				out.lineToAbs(x, y1);
				out.endData();
				out.endPath();

				String label = line.getLabel();
				if (label != null && !label.isEmpty()) {
					out.beginText(x + 2, y0 + 10, label);
					out.setFill(color);
					out.setStroke("none");
					out.endText();
				}
			} else if (decoration instanceof GanttRangeDecoration range) {
				double xFrom = chartX0 + (range.getFrom() - rangeMin) * zoom;
				double xTo = chartX0 + (range.getTo() - rangeMin) * zoom;
				String color = range.getColor();
				if (color == null || color.isEmpty()) {
					color = "rgba(100,100,100,0.2)";
				}

				out.beginRect(xFrom, y0, xTo - xFrom, y1 - y0);
				out.setFill(color);
				out.setStroke("none");
				out.endRect();

				String label = range.getLabel();
				if (label != null && !label.isEmpty()) {
					out.beginText(xFrom + 2, y0 + 10, label);
					out.setFill("#303030");
					out.setStroke("none");
					out.endText();
				}
			}
		}

		out.endGroup();
	}

	private static void drawEdges(GanttLayout self, SvgWriter out) {
		List<GanttEdge> edges = self.getEdges();
		if (edges == null || edges.isEmpty()) {
			return;
		}

		// Build lookup map from item id to item.
		Map<String, GanttItem> itemById = new HashMap<>();
		for (GanttItem item : self.getItems()) {
			itemById.put(item.getId(), item);
		}

		out.beginGroup();
		out.writeCssClass("tl-gantt-edges");

		for (GanttEdge edge : edges) {
			GanttItem sourceItem = itemById.get(edge.getSourceItemId());
			GanttItem targetItem = itemById.get(edge.getTargetItemId());
			if (sourceItem == null || targetItem == null) {
				continue;
			}

			GanttEndpoint sourceEndpoint = edge.getSourceEndpoint();
			GanttEndpoint targetEndpoint = edge.getTargetEndpoint();

			double sx = endpointX(sourceItem, sourceEndpoint);
			double sy = centerY(sourceItem);
			double tx = endpointX(targetItem, targetEndpoint);
			double ty = centerY(targetItem);

			// Horizontal offset to route outside the box:
			// START side: go left (-EDGE_HORIZONTAL_STUB), END side: go right (+EDGE_HORIZONTAL_STUB).
			double sOffset = (sourceEndpoint == GanttEndpoint.START) ? -EDGE_HORIZONTAL_STUB : EDGE_HORIZONTAL_STUB;
			double tOffset = (targetEndpoint == GanttEndpoint.START) ? -EDGE_HORIZONTAL_STUB : EDGE_HORIZONTAL_STUB;

			double midY = (sy + ty) / 2.0;

			// Stroke style based on enforce mode.
			GanttEnforce enforce = edge.getEnforce();
			String strokeColor;
			double strokeWidth;
			boolean dashed;
			if (enforce == GanttEnforce.WARN) {
				strokeColor = "#d01030";
				strokeWidth = EDGE_STROKE_WIDTH_NORMAL;
				dashed = false;
			} else if (enforce == GanttEnforce.NONE) {
				strokeColor = "#606060";
				strokeWidth = EDGE_STROKE_WIDTH_THIN;
				dashed = true;
			} else {
				// STRICT (default)
				strokeColor = "#606060";
				strokeWidth = EDGE_STROKE_WIDTH_NORMAL;
				dashed = false;
			}

			out.beginPath();
			out.setStroke(strokeColor);
			out.setStrokeWidth(strokeWidth);
			out.setFill("none");
			if (dashed) {
				out.setStrokeDasharray(4.0, 3.0);
			}
			out.beginData();
			out.moveToAbs(sx, sy);
			out.lineToAbs(sx + sOffset, sy);
			out.lineToAbs(sx + sOffset, midY);
			out.lineToAbs(tx + tOffset, midY);
			out.lineToAbs(tx + tOffset, ty);
			out.lineToAbs(tx, ty);
			out.endData();
			out.endPath();
		}

		out.endGroup();
	}

	/**
	 * Builds a map from row ID to {@link RowGeometry} using the same two-pass height computation
	 * as {@link #computeIntrinsicSize}, based on the already-laid-out item boxes.
	 *
	 * <p>
	 * This is called during drawing, after {@code computeIntrinsicSize} has already populated
	 * every item box's {@code height} field.
	 * </p>
	 */
	private static Map<String, RowGeometry> buildRowGeometry(GanttLayout self, double lanesTop) {
		double rowMinContentHeight = self.getRowMinContentHeight();
		double rowPadding = self.getRowPadding();

		// Build ordered row index map.
		LinkedHashMap<String, Integer> rowIndex = new LinkedHashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter);
		}
		int totalRows = counter[0];

		// Compute per-row max content height from already-distributed item boxes.
		double[] rowMaxContentHeight = new double[totalRows];
		for (int i = 0; i < totalRows; i++) {
			rowMaxContentHeight[i] = rowMinContentHeight;
		}
		for (GanttItem item : self.getItems()) {
			Integer idx = rowIndex.get(item.getRowId());
			if (idx == null) {
				continue;
			}
			Box box = item.getBox();
			if (box == null) {
				continue;
			}
			double h = box.getHeight();
			if (h > rowMaxContentHeight[idx]) {
				rowMaxContentHeight[idx] = h;
			}
		}

		// Build geometry map in row-definition order.
		Map<String, RowGeometry> result = new LinkedHashMap<>();
		double[] rowNames = null; // we'll iterate the entry set
		double y = lanesTop;
		int i = 0;
		for (Map.Entry<String, Integer> entry : rowIndex.entrySet()) {
			double rowHeight = rowMaxContentHeight[i] + 2 * rowPadding;
			result.put(entry.getKey(), new RowGeometry(y, y + rowHeight));
			y += rowHeight;
			i++;
		}
		return result;
	}

	private static double endpointX(GanttItem item, GanttEndpoint endpoint) {
		Box b = item.getBox();
		return endpoint == GanttEndpoint.START ? b.getX() : b.getX() + b.getWidth();
	}

	private static double centerY(GanttItem item) {
		Box b = item.getBox();
		return b.getY() + b.getHeight() / 2.0;
	}

	private static void indexRows(GanttRow row, Map<String, Integer> idx, int[] counter) {
		idx.put(row.getId(), counter[0]);
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			indexRows(child, idx, counter);
		}
	}

	private static void indexRows(GanttRow row, Map<String, Integer> idx, int[] counter, List<GanttRow> rowList) {
		idx.put(row.getId(), counter[0]);
		rowList.add(row);
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			indexRows(child, idx, counter, rowList);
		}
	}
}

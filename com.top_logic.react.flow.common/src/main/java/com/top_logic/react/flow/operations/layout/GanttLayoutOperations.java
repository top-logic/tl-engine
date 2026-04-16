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
 */
public interface GanttLayoutOperations extends BoxOperations {

	/** Vertical inset from the row top edge to the item box top edge. */
	double ITEM_TOP_INSET = 2.0;

	/** Total vertical padding consumed by top and bottom insets combined ({@code 2 * ITEM_TOP_INSET}). */
	double ITEM_VERTICAL_PADDING_TOTAL = 2 * ITEM_TOP_INSET;

	/** Left padding between the row x-origin (or indent) and the row-label text. */
	double ROW_LABEL_LEFT_PADDING = 4.0;

	/** Horizontal stub length for orthogonal edge routing (pixels offset from item edge). */
	double EDGE_HORIZONTAL_STUB = 6.0;

	/** Stroke width for the decoration line marker. */
	double DECORATION_LINE_STROKE_WIDTH = 1.5;

	/** Stroke width for normal (STRICT / WARN) dependency edges. */
	double EDGE_STROKE_WIDTH_NORMAL = 1.2;

	/** Stroke width for informational (NONE) dependency edges. */
	double EDGE_STROKE_WIDTH_THIN = 0.8;

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		GanttLayout self = (GanttLayout) this;
		GanttAxis axis = self.getAxis();

		Map<String, Integer> rowIndex = new HashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter);
		}

		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double labelWidth = self.getRowLabelWidth();
		double rowHeight = self.getRowHeight();
		double axisHeight = self.getAxisHeight();

		for (GanttItem item : self.getItems()) {
			Integer idx = rowIndex.get(item.getRowId());
			if (idx == null) {
				continue;
			}
			double y = offsetY + axisHeight + idx * rowHeight + ITEM_TOP_INSET;
			Box box = item.getBox();
			if (box == null) {
				continue;
			}
			if (item instanceof GanttSpan span) {
				double x = offsetX + labelWidth + (span.getStart() - rangeMin) * zoom;
				double w = (span.getEnd() - span.getStart()) * zoom;
				box.computeIntrinsicSize(context, x, y);
				box.setX(x);
				box.setY(y);
				box.setWidth(w);
				box.setHeight(rowHeight - ITEM_VERTICAL_PADDING_TOTAL);
			} else if (item instanceof GanttMilestone ms) {
				double r = (rowHeight - ITEM_VERTICAL_PADDING_TOTAL) / 2.0;
				double cx = offsetX + labelWidth + (ms.getAt() - rangeMin) * zoom;
				box.computeIntrinsicSize(context, cx - r, y);
				box.setX(cx - r);
				box.setY(y);
				box.setWidth(2 * r);
				box.setHeight(2 * r);
			}
		}

		self.setX(offsetX);
		self.setY(offsetY);
		self.setWidth(labelWidth + (axis.getRangeMax() - rangeMin) * zoom);
		self.setHeight(axisHeight + counter[0] * rowHeight);
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
		double x0 = self.getX() + self.getRowLabelWidth();
		double y0 = self.getY();
		double w = self.getWidth() - self.getRowLabelWidth();
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

			// Tick label near the bottom of the axis header.
			String label = tick.getLabel();
			if (label != null && !label.isEmpty()) {
				out.beginText(tickX + 2, y0 + h - 4, label);
				out.setFill("#303030");
				out.setStroke("none");
				out.endText();
			}
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
		double rowHeight = self.getRowHeight();
		double labelWidth = self.getRowLabelWidth();
		double indentWidth = self.getIndentWidth();

		out.beginGroup();
		out.writeCssClass("tl-gantt-lanes");

		int[] rowIndex = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			drawRowLane(root, 0, x0, y0, totalWidth, rowHeight, labelWidth, indentWidth, out, rowIndex);
		}

		out.endGroup();
	}

	private static void drawRowLane(GanttRow row, int depth,
			double x0, double lanesTop, double totalWidth, double rowHeight,
			double labelWidth, double indentWidth, SvgWriter out, int[] rowIndex) {
		int idx = rowIndex[0]++;
		double rowY = lanesTop + idx * rowHeight;

		// Alternating background.
		String fillColor = (idx % 2 == 0) ? "#ffffff" : "#f5f5f5";
		out.beginRect(x0, rowY, totalWidth, rowHeight);
		out.setFill(fillColor);
		out.setStroke("#e0e0e0");
		out.setStrokeWidth(1.0);
		out.endRect();

		// Row label, indented by depth.
		double labelX = x0 + ROW_LABEL_LEFT_PADDING + depth * indentWidth;
		double labelY = rowY + rowHeight / 2.0 + 4.0; // vertically centred (approx.)
		String label = row.getLabel();
		if (label != null && !label.isEmpty()) {
			out.beginText(labelX, labelY, label);
			out.setFill("#303030");
			out.setStroke("none");
			out.endText();
		}

		// Vertical separator between label column and chart area.
		double sepX = x0 + labelWidth;
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
			drawRowLane(child, depth + 1, x0, lanesTop, totalWidth, rowHeight, labelWidth, indentWidth, out, rowIndex);
		}
	}

	private static void drawDecorations(GanttLayout self, SvgWriter out) {
		List<GanttDecoration> decorations = self.getDecorations();
		if (decorations == null || decorations.isEmpty()) {
			return;
		}

		// Build depth-first row index map.
		LinkedHashMap<String, Integer> rowIndex = new LinkedHashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter);
		}
		int totalRows = counter[0];

		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double rowLabelWidth = self.getRowLabelWidth();
		double rowHeight = self.getRowHeight();
		double axisHeight = self.getAxisHeight();
		double chartY0 = self.getY() + axisHeight;
		double chartX0 = self.getX() + rowLabelWidth;

		out.beginGroup();
		out.writeCssClass("tl-gantt-decorations");

		for (GanttDecoration decoration : decorations) {
			// Compute vertical extent.
			List<String> relevantFor = decoration.getRelevantFor();
			double y0;
			double y1;
			if (relevantFor == null || relevantFor.isEmpty()) {
				y0 = chartY0;
				y1 = chartY0 + totalRows * rowHeight;
			} else {
				int minIdx = Integer.MAX_VALUE;
				int maxIdx = Integer.MIN_VALUE;
				for (String rowId : relevantFor) {
					Integer idx = rowIndex.get(rowId);
					if (idx != null) {
						if (idx < minIdx) {
							minIdx = idx;
						}
						if (idx > maxIdx) {
							maxIdx = idx;
						}
					}
				}
				if (minIdx == Integer.MAX_VALUE) {
					// No matching rows — fall back to full chart.
					y0 = chartY0;
					y1 = chartY0 + totalRows * rowHeight;
				} else {
					y0 = chartY0 + minIdx * rowHeight;
					y1 = chartY0 + (maxIdx + 1) * rowHeight;
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
}

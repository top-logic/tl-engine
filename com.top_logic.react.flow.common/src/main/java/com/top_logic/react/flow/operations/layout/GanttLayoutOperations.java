/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttPoint;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.operations.drag.DragController;
import com.top_logic.react.flow.operations.util.DiagramUtil;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Layout and rendering operations for {@link GanttLayout}.
 *
 * <p>
 * Places {@link GanttItem} boxes along the X axis according to their {@code start}/{@code end}
 * (or {@code at}) positions, and along the Y axis according to the item's row. Draws
 * row-lane backgrounds, edges (orthogonal), and decorations.
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
public interface GanttLayoutOperations extends BoxOperations, DragController {

	/** Horizontal stub length for orthogonal edge routing (pixels offset from item edge). */
	double EDGE_HORIZONTAL_STUB = 6.0;

	/** Default stroke width for dependency edges. */
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
			double tmpY = offsetY + rowPadding; // rough y, refined in pass 2
			if (item instanceof GanttSpan span) {
				tmpX = offsetX + tmpLabelWidth + (span.getStart() - rangeMin) * zoom;
				box.computeIntrinsicSize(context, tmpX, tmpY);
				double intrinsicHeight = box.getHeight();
				if (intrinsicHeight > rowMaxContentHeight[idx]) {
					rowMaxContentHeight[idx] = intrinsicHeight;
				}
			} else if (item instanceof GanttPoint pt) {
				box.computeIntrinsicSize(context, 0, 0);
				// Accept whatever intrinsic size the box reports.
				// Width and height may be 0 (e.g. empty FloatingLayout) — that's OK.
				double intrinsicHeight = box.getHeight();
				if (intrinsicHeight > rowMaxContentHeight[idx]) {
					rowMaxContentHeight[idx] = intrinsicHeight;
				}
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
			double tmpY = offsetY + rowPadding;
			label.computeIntrinsicSize(context, tmpX, tmpY);
			double labelW = label.getWidth() + depth * indentWidth;
			if (labelW > maxLabelIntrinsicWidth) {
				maxLabelIntrinsicWidth = labelW;
			}
		}

		// --- Pass 1c: compute intrinsic sizes of decoration label boxes ---
		List<GanttDecoration> decos = self.getDecorations();
		if (decos != null) {
			for (GanttDecoration deco : decos) {
				Box decoLabel = deco.getLabel();
				if (decoLabel != null) {
					decoLabel.computeIntrinsicSize(context, 0, 0);
				}
			}
		}

		// --- Aggregate: compute column width and per-row total heights ---
		double columnWidth = Math.max(rowLabelMinWidth, maxLabelIntrinsicWidth + 2 * rowLabelPadding);

		double[] rowTotalHeight = new double[totalRows];
		double[] rowYStart = new double[totalRows];
		double cumulativeY = offsetY;
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
			} else if (item instanceof GanttPoint pt) {
				double cx = offsetX + columnWidth + (pt.getAt() - rangeMin) * zoom;
				double w = box.getWidth();
				// Give the full row content height so it can grow with siblings.
				box.distributeSize(context, cx - w / 2.0, itemY, w, contentHeight);
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

		// --- Pass 2c: distribute final positions and sizes for decoration label boxes ---
		if (decos != null && !decos.isEmpty()) {
			double chartX0 = offsetX + columnWidth;
			double lanesTop2 = offsetY;
			for (GanttDecoration deco : decos) {
				Box decoLabel = deco.getLabel();
				if (decoLabel == null) {
					continue;
				}
				// Determine y0 for this decoration (top of its vertical extent).
				List<String> relevantFor = deco.getRelevantFor();
				double decoY0;
				if (relevantFor == null || relevantFor.isEmpty()) {
					decoY0 = lanesTop2;
				} else {
					double minY = Double.MAX_VALUE;
					for (String rowId : relevantFor) {
						Integer rowIdx = rowIndex.get(rowId);
						if (rowIdx != null && rowYStart[rowIdx] < minY) {
							minY = rowYStart[rowIdx];
						}
					}
					decoY0 = (minY == Double.MAX_VALUE) ? lanesTop2 : minY;
				}

				double labelX;
				if (deco instanceof GanttLineDecoration line) {
					labelX = chartX0 + (line.getAt() - rangeMin) * zoom + 2.0;
				} else if (deco instanceof GanttRangeDecoration range) {
					labelX = chartX0 + (range.getFrom() - rangeMin) * zoom + 2.0;
				} else {
					labelX = chartX0 + 2.0;
				}
				double labelY = decoY0 + 10.0;
				double labelW = decoLabel.getWidth();
				double labelH = decoLabel.getHeight();
				decoLabel.distributeSize(context, labelX, labelY, labelW, labelH);
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
		drawRowLanes(self, out);
		drawDecorations(self, out);
		for (Box content : self.getContents()) {
			out.write(content);
		}
		drawEdges(self, out);
	}

	private static void drawRowLanes(GanttLayout self, SvgWriter out) {
		double x0 = self.getX();
		double y0 = self.getY();
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
		double lanesTop = self.getY();
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

				double sw = line.getStrokeWidth();
				if (sw <= 0.0) {
					sw = 1.0;
				}

				out.beginPath();
				out.setStroke(color);
				out.setStrokeWidth(sw);
				out.setFill("none");
				List<Double> dashes = line.getDashes();
				if (dashes != null && !dashes.isEmpty()) {
					out.setStrokeDasharray(DiagramUtil.doubleArray(dashes));
				}
				out.beginData();
				out.moveToAbs(x, y0);
				out.lineToAbs(x, y1);
				out.endData();
				out.endPath();

				// Label box is rendered through the standard contents dispatch in draw().
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

				// Label box is rendered through the standard contents dispatch in draw().
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

			// Visual properties from the edge (application-defined).
			// Switch to violated style if constraint is violated and a violated style is defined.
			boolean hasViolatedStyle = edge.getViolatedStrokeColor() != null
				&& !edge.getViolatedStrokeColor().isEmpty();
			boolean violated = hasViolatedStyle
				&& isConstraintViolated(sourceItem, sourceEndpoint, targetItem, targetEndpoint);

			String strokeColor;
			double strokeWidth;
			List<Double> dashes;
			if (violated && edge.getViolatedStrokeColor() != null && !edge.getViolatedStrokeColor().isEmpty()) {
				strokeColor = edge.getViolatedStrokeColor();
				strokeWidth = edge.getViolatedStrokeWidth() > 0 ? edge.getViolatedStrokeWidth() : edge.getStrokeWidth();
				List<Double> vd = edge.getViolatedDashes();
				dashes = (vd != null && !vd.isEmpty()) ? vd : edge.getDashes();
			} else {
				strokeColor = edge.getStrokeColor();
				strokeWidth = edge.getStrokeWidth();
				dashes = edge.getDashes();
			}
			if (strokeColor == null || strokeColor.isEmpty()) strokeColor = "#606060";
			if (strokeWidth <= 0) strokeWidth = EDGE_STROKE_WIDTH_NORMAL;

			out.beginPath();
			out.setStroke(strokeColor);
			out.setStrokeWidth(strokeWidth);
			out.setFill("none");
			if (dashes != null && !dashes.isEmpty()) {
				double[] dashArray = new double[dashes.size()];
				for (int i = 0; i < dashes.size(); i++) dashArray[i] = dashes.get(i);
				out.setStrokeDasharray(dashArray);
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

	/**
	 * Checks whether a dependency constraint is violated based on the endpoint positions.
	 *
	 * <p>
	 * The constraint says: the source endpoint position must be &le; the target endpoint
	 * position (in the time dimension). For FS (END→START): source.end &le; target.start.
	 * For SS (START→START): source.start &le; target.start. Etc.
	 * </p>
	 */
	private static boolean isConstraintViolated(GanttItem source, GanttEndpoint sourceEnd,
			GanttItem target, GanttEndpoint targetEnd) {
		double sourcePos = endpointX(source, sourceEnd);
		double targetPos = endpointX(target, targetEnd);
		return sourcePos > targetPos;
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

	// -----------------------------------------------------------------------
	// DragController implementation
	// -----------------------------------------------------------------------

	/**
	 * Finds the {@link GanttItem} whose box is identical to the given box (identity comparison).
	 * Returns {@code null} if no item owns this box.
	 */
	private static GanttItem findItemByBox(GanttLayout layout, Box box) {
		for (GanttItem item : layout.getItems()) {
			if (item.getBox() == box) {
				return item;
			}
		}
		return null;
	}

	@Override
	default boolean canMove(Box box) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) {
			return false;
		}
		return item.isCanMoveTime() || item.isCanMoveRow();
	}

	@Override
	default boolean canResize(Box box, DragEdge edge) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null || !(item instanceof GanttSpan)) {
			return false;
		}
		GanttSpan span = (GanttSpan) item;
		switch (edge) {
			case W: return span.isCanResizeStart();
			case E: return span.isCanResizeEnd();
			default: return false;
		}
	}

	@Override
	default List<DropArea> getDropAreas(Box box) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) {
			return new ArrayList<>();
		}

		boolean canMoveRow = item.isCanMoveRow();
		boolean canMoveTime = item.isCanMoveTime();
		String currentRowId = item.getRowId();

		// Item-specific drop target restriction (empty = no restriction).
		List<String> validTargetIds = item.getValidDropTargetIds();
		boolean hasTargetRestriction = validTargetIds != null && !validTargetIds.isEmpty();

		GanttAxis axis = self.getAxis();
		double columnWidth = self.getColumnWidth();
		double chartWidth = (axis.getRangeMax() - axis.getRangeMin()) * axis.getCurrentZoom();

		Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());
		Map<String, GanttRow> rowsById = buildRowIndex(self);

		List<DropArea> areas = new ArrayList<>();
		for (Map.Entry<String, RowGeometry> entry : rowGeometry.entrySet()) {
			String rowId = entry.getKey();

			// 1. canMoveRow=false → only current row.
			if (!canMoveRow && !rowId.equals(currentRowId)) {
				continue;
			}

			// 2. Row.acceptsDrop=false → skip (e.g. axis rows).
			GanttRow row = rowsById.get(rowId);
			if (row != null && !row.isAcceptsDrop()) {
				// Always include the item's current row even if acceptsDrop is false,
				// so the item doesn't "jump" out of an axis row it was placed in.
				if (!rowId.equals(currentRowId)) {
					continue;
				}
			}

			// 3. validDropTargetIds set → only listed rows.
			if (hasTargetRestriction && !validTargetIds.contains(rowId)) {
				if (!rowId.equals(currentRowId)) {
					continue;
				}
			}

			RowGeometry geom = entry.getValue();

			double areaX;
			double areaWidth;
			if (canMoveTime) {
				areaX = self.getX() + columnWidth;
				areaWidth = chartWidth;
			} else {
				areaX = box.getX();
				areaWidth = box.getWidth();
			}

			DropArea area = DropArea.create()
				.setX(areaX)
				.setY(geom.yStart())
				.setWidth(areaWidth)
				.setHeight(geom.height());
			areas.add(area);
		}
		return areas;
	}

	/** Builds a map from row ID to GanttRow for the full row forest. */
	private static Map<String, GanttRow> buildRowIndex(GanttLayout layout) {
		Map<String, GanttRow> result = new LinkedHashMap<>();
		for (GanttRow root : layout.getRootRows()) {
			collectRows(root, result);
		}
		return result;
	}

	private static void collectRows(GanttRow row, Map<String, GanttRow> index) {
		index.put(row.getId(), row);
		for (GanttRow child : row.getChildren()) {
			collectRows(child, index);
		}
	}

	@Override
	default double[] constrainMove(Box box, double proposedX, double proposedY) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double snap = axis.getSnapGranularity();
		double columnWidth = self.getColumnWidth();
		double rangeMin = axis.getRangeMin();
		double rangeMax = axis.getRangeMax();
		double chartX0 = self.getX() + columnWidth;
		double chartX1 = self.getX() + columnWidth + (rangeMax - rangeMin) * zoom;

		// Compute snapped X.
		double resultX;
		if (item != null && !item.isCanMoveTime()) {
			resultX = box.getX();
		} else {
			double snapPixels = snap * zoom;
			if (snapPixels > 0) {
				resultX = Math.round((proposedX - chartX0) / snapPixels) * snapPixels + chartX0;
			} else {
				resultX = proposedX;
			}
			// Clamp to chart boundaries.
			resultX = Math.max(chartX0, Math.min(chartX1 - box.getWidth(), resultX));
		}

		// Compute snapped Y: snap to nearest valid row.
		double resultY;
		if (item != null && !item.isCanMoveRow()) {
			resultY = box.getY();
		} else {
			// Find the nearest drop area Y.
			List<DropArea> areas = getDropAreas(box);
			double rowPadding = self.getRowPadding();
			double bestY = proposedY;
			double bestDist = Double.MAX_VALUE;
			for (DropArea area : areas) {
				double candidateY = area.getY() + rowPadding;
				double dist = Math.abs(proposedY - candidateY);
				if (dist < bestDist) {
					bestDist = dist;
					bestY = candidateY;
				}
			}
			resultY = bestY;
		}

		return new double[] { resultX, resultY };
	}

	@Override
	default double constrainResize(Box box, DragEdge edge, double proposedEdgePos) {
		GanttLayout self = (GanttLayout) this;
		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double snap = axis.getSnapGranularity();
		double columnWidth = self.getColumnWidth();
		double chartX0 = self.getX() + columnWidth;

		// Snap to grid.
		double snapPixels = snap * zoom;
		double snapped;
		if (snapPixels > 0) {
			snapped = Math.round((proposedEdgePos - chartX0) / snapPixels) * snapPixels + chartX0;
		} else {
			snapped = proposedEdgePos;
		}

		double minWidth = snapPixels > 0 ? snapPixels : 1.0;

		switch (edge) {
			case W:
				// West edge: constrain so width >= minWidth.
				return Math.min(snapped, box.getX() + box.getWidth() - minWidth);
			case E:
				// East edge: constrain so width >= minWidth.
				return Math.max(snapped, box.getX() + minWidth);
			default:
				return proposedEdgePos;
		}
	}

	@Override
	default void commitDrag(Box box, double finalX, double finalY, double finalWidth, double finalHeight) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) {
			return;
		}

		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double columnWidth = self.getColumnWidth();
		double layoutX = self.getX();

		// Determine if row changed.
		boolean rowChanged = Math.abs(finalY - box.getY()) > 0.5;
		if (rowChanged) {
			// Find target row from Y position using row geometry.
			Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());
			String targetRowId = item.getRowId(); // default to current
			double rowPadding = self.getRowPadding();
			for (Map.Entry<String, RowGeometry> entry : rowGeometry.entrySet()) {
				RowGeometry geom = entry.getValue();
				double rowContentY = geom.yStart() + rowPadding;
				if (Math.abs(finalY - rowContentY) < 0.5) {
					targetRowId = entry.getKey();
					break;
				}
				// Also match if finalY is within the row bounds.
				if (finalY >= geom.yStart() && finalY < geom.yEnd()) {
					targetRowId = entry.getKey();
					break;
				}
			}
			item.setRowId(targetRowId);
		}

		// Update item time position.
		if (item instanceof GanttSpan) {
			GanttSpan span = (GanttSpan) item;
			boolean xChanged = Math.abs(finalX - box.getX()) > 0.5;
			boolean wChanged = Math.abs(finalWidth - box.getWidth()) > 0.5;
			if (xChanged || wChanged) {
				double newStart = (finalX - layoutX - columnWidth) / zoom + rangeMin;
				double newEnd = (finalX + finalWidth - layoutX - columnWidth) / zoom + rangeMin;
				span.setStart(newStart);
				span.setEnd(newEnd);
			}
		} else if (item instanceof GanttPoint) {
			GanttPoint pt = (GanttPoint) item;
			boolean xChanged = Math.abs(finalX - box.getX()) > 0.5;
			if (xChanged) {
				// Center position of the box.
				double cx = finalX + finalWidth / 2.0;
				double newAt = (cx - layoutX - columnWidth) / zoom + rangeMin;
				pt.setAt(newAt);
			}
		}
	}

	@Override
	default void cancelDrag(Box box) {
		// No persistent state to clean up.
	}
}

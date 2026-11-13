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
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttPanTarget;
import com.top_logic.react.flow.data.GanttPoint;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.operations.ConnectorSymbolRenderer;
import com.top_logic.react.flow.operations.drag.DragController;
import com.top_logic.react.flow.operations.util.DiagramUtil;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.svg.event.MouseButton;
import com.top_logic.react.flow.svg.event.SVGClickEvent;
import com.top_logic.react.flow.svg.event.SVGWheelEvent;
import com.top_logic.react.flow.svg.event.SVGWheelHandler;

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
 * {@link Box#computeIntrinsicSize(RenderContext, double, double, double, double)} on every item
 * box and records per-row maximum intrinsic heights. Pass 2 calls
 * {@link Box#distributeSize(RenderContext, double, double, double, double)} on every item box
 * with the final cell dimensions so that all boxes in the same row grow to the same height.
 * Span widths are forced to {@code (end - start) * zoom}; milestone widths are intrinsic.
 * </p>
 *
 * <p>
 * The box of the chart is limited to the size offered by the enclosing layout. Content larger
 * than the box is shown in an internal viewport: the data rows scroll in both directions, the
 * {@link GanttLayout#getFrozenRows() frozen header rows} scroll only horizontally, the row labels
 * only vertically. Wheel gestures and dragging empty space scroll the viewport, Ctrl-wheel zooms
 * the time axis. With {@link GanttLayout#isFitWidth()} the time axis is fitted into the offered
 * width.
 * </p>
 *
 * <p>
 * A viewport shows a scrollbar for each direction with a scroll range: a vertical one right of
 * the data rows, a horizontal one below the time axis. The scrollbars take their space from the
 * viewport, so that the last row and the end of the time range can be scrolled fully into view.
 * Dragging a thumb scrolls proportionally, a click on a track beside its thumb scrolls by one
 * page.
 * </p>
 */
public interface GanttLayoutOperations extends BoxOperations, DragController, SVGWheelHandler {

	/** Horizontal stub length for orthogonal edge routing (pixels offset from item edge). */
	double EDGE_HORIZONTAL_STUB = 6.0;

	/** Default stroke width for dependency edges. */
	double EDGE_STROKE_WIDTH_NORMAL = 1.2;

	/** Stroke width for informational (NONE) dependency edges. */
	double EDGE_STROKE_WIDTH_THIN = 0.8;

	/** Smallest zoom factor reachable by a Ctrl-wheel gesture (unless the fitted zoom is smaller). */
	double MIN_ZOOM = 0.5;

	/** Largest zoom factor reachable by a Ctrl-wheel gesture. */
	double MAX_ZOOM = 100.0;

	/**
	 * Amount in pixels by which the content may exceed the box without activating the internal
	 * viewport (absorbs rounding of a fitted zoom).
	 */
	double VIEWPORT_TOLERANCE = 0.5;

	/** Thickness of the scrollbars of the internal viewport, in pixels. */
	double SCROLLBAR_SIZE = 10.0;

	/** Gap between the track of a scrollbar and its thumb, across the track, in pixels. */
	double SCROLLBAR_THUMB_INSET = 2.0;

	/** Smallest length of a scrollbar thumb, in pixels. */
	double MIN_THUMB_LENGTH = 24.0;

	/** CSS class of the group drawing a scrollbar of the internal viewport. */
	String SCROLLBAR_CSS_CLASS = "tl-gantt-scrollbar";

	/** CSS class of the track of a scrollbar. */
	String SCROLLBAR_TRACK_CSS_CLASS = "tl-gantt-scrollbar-track";

	/** CSS class of the thumb of a scrollbar. */
	String SCROLLBAR_THUMB_CSS_CLASS = "tl-gantt-scrollbar-thumb";

	/**
	 * Suffix of the ID (appended to the chart's client ID) of the group moving with both scroll
	 * offsets: the data rows.
	 */
	String SCROLL_CONTENT_ID_SUFFIX = "-scroll-content";

	/** Suffix of the ID of the group moving with the horizontal scroll offset: the decorations. */
	String SCROLL_DECORATIONS_ID_SUFFIX = "-scroll-deco";

	/**
	 * Suffix of the ID of the group moving with the horizontal scroll offset: the labels of
	 * decorations that start in the frozen rows or span all rows, drawn above the frozen rows at the
	 * top of the data rows.
	 */
	String SCROLL_DECORATION_LABELS_ID_SUFFIX = "-scroll-deco-labels";

	/** Suffix of the ID of the group moving with the vertical scroll offset: the row labels. */
	String SCROLL_SIDEBAR_ID_SUFFIX = "-scroll-sidebar";

	/** Suffix of the ID of the group moving with the horizontal scroll offset: the frozen rows. */
	String SCROLL_HEADER_ID_SUFFIX = "-scroll-header";

	/** Suffix of the ID of the group moving the thumb of the vertical scrollbar. */
	String VERTICAL_THUMB_ID_SUFFIX = "-scrollbar-v-thumb";

	/** Suffix of the ID of the group moving the thumb of the horizontal scrollbar. */
	String HORIZONTAL_THUMB_ID_SUFFIX = "-scrollbar-h-thumb";

	/**
	 * Geometry of a single row: its Y start (inclusive), Y end (exclusive), and total height.
	 *
	 * @param yStart
	 *        The Y coordinate where the row starts (inclusive).
	 * @param yEnd
	 *        The Y coordinate where the row ends (exclusive).
	 */
	record RowGeometry(double yStart, double yEnd) {

		/** Total height of the row (lane height). */
		double height() {
			return yEnd - yStart;
		}
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight) {
		GanttLayout self = (GanttLayout) this;
		layoutContent(self, context, offsetX, offsetY, availableWidth, availableHeight, availableWidth);
		if (self.isFitWidth() && availableWidth < Double.POSITIVE_INFINITY
			&& GanttScrollGeometry.of(self).vertical()) {
			// The vertical scrollbar takes its space from the fitted time axis.
			layoutContent(self, context, offsetX, offsetY, availableWidth, availableHeight,
				availableWidth - SCROLLBAR_SIZE);
		}
	}

	/**
	 * Lays out the chart within the given space.
	 *
	 * @param fitWidth
	 *        The width a {@link GanttLayout#isFitWidth() fitted} time axis fills together with the
	 *        label column.
	 */
	private static void layoutContent(GanttLayout self, RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight, double fitWidth) {
		// Build depth-first row index map (rowId -> 0-based index) and row list for depth lookup.
		Map<String, Integer> rowIndex = new LinkedHashMap<>();
		List<GanttRow> rowList = new java.util.ArrayList<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter, rowList);
		}
		int totalRows = counter[0];

		double rangeMin = self.getRangeMin();
		double rowLabelMinWidth = self.getRowLabelMinWidth();
		double rowLabelPadding = self.getRowLabelPadding();
		double indentWidth = self.getIndentWidth();

		// --- Pass 0: measure the widest variant of each row label; record max label width ---
		// The label column does not depend on the zoom: it is as wide as the widest variant of
		// each row label, whichever variant the zoom selects later.
		RenderContext labelColumnContext = context.withWidestVariants();
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
			double tmpY = offsetY + row.getRowPadding();
			label.computeIntrinsicSize(labelColumnContext, tmpX, tmpY,
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			double labelW = label.getWidth() + depth * indentWidth;
			if (labelW > maxLabelIntrinsicWidth) {
				maxLabelIntrinsicWidth = labelW;
			}
		}

		// The label column does not depend on the zoom, the zoom fitting the time axis into the
		// offered width depends on the label column.
		double columnWidth = Math.max(rowLabelMinWidth, maxLabelIntrinsicWidth + 2 * rowLabelPadding);
		double zoom = layoutZoom(self, columnWidth, fitWidth);
		if (zoom != self.getZoom()) {
			self.setZoom(zoom);
		}

		// Items and row labels see the time axis zoom so that LOD boxes can adapt their content
		// variant to it; decoration labels stay at zoom 1.
		RenderContext itemContext = context.withZoom(zoom);

		// --- Pass 1: compute intrinsic sizes of item boxes, record per-row max content height ---
		double[] rowMaxContentHeight = new double[totalRows];
		for (int i = 0; i < totalRows; i++) {
			rowMaxContentHeight[i] = rowList.get(i).getMinContentHeight();
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
			// Provide a temporary position for intrinsic sizing (exact position comes in pass 2).
			double tmpX;
			GanttRow itemRow = rowList.get(idx);
			double itemRowPadding = itemRow.getRowPadding();
			double tmpY = offsetY + itemRowPadding; // rough y, refined in pass 2
			if (item instanceof GanttSpan span) {
				tmpX = offsetX + columnWidth + (span.getStart() - rangeMin) * zoom;
				double spanWidth = (span.getEnd() - span.getStart()) * zoom;
				box.computeIntrinsicSize(itemContext, tmpX, tmpY, spanWidth, Double.POSITIVE_INFINITY);
				double intrinsicHeight = box.getHeight();
				if (intrinsicHeight > rowMaxContentHeight[idx]) {
					rowMaxContentHeight[idx] = intrinsicHeight;
				}
			} else if (item instanceof GanttPoint pt) {
				box.computeIntrinsicSize(itemContext, 0, 0,
						Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
				// Accept whatever intrinsic size the box reports.
				// Width and height may be 0 (e.g. empty FloatingLayout) — that's OK.
				double intrinsicHeight = box.getHeight();
				if (intrinsicHeight > rowMaxContentHeight[idx]) {
					rowMaxContentHeight[idx] = intrinsicHeight;
				}
			}
		}

		// --- Pass 1c: compute intrinsic sizes of decoration label boxes ---
		List<GanttDecoration> decos = self.getDecorations();
		if (decos != null) {
			for (GanttDecoration deco : decos) {
				Box decoLabel = deco.getLabel();
				if (decoLabel != null) {
					decoLabel.computeIntrinsicSize(context, 0, 0,
							Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
				}
			}
		}

		// --- Aggregate: compute per-row total heights ---
		double[] rowTotalHeight = new double[totalRows];
		double[] rowYStart = new double[totalRows];
		double cumulativeY = offsetY;
		for (int i = 0; i < totalRows; i++) {
			double rp = rowList.get(i).getRowPadding();
			rowTotalHeight[i] = rowMaxContentHeight[i] + 2 * rp;
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
			double rp = rowList.get(idx).getRowPadding();
			double contentHeight = rowTotalHeight[idx] - 2 * rp;
			double itemY = rowYStart[idx] + rp;

			if (item instanceof GanttSpan span) {
				double x = offsetX + columnWidth + (span.getStart() - rangeMin) * zoom;
				double w = (span.getEnd() - span.getStart()) * zoom;
				box.distributeSize(itemContext, x, itemY, w, contentHeight);
			} else if (item instanceof GanttPoint pt) {
				double cx = offsetX + columnWidth + (pt.getAt() - rangeMin) * zoom;
				double w = box.getWidth();
				// Give the full row content height so it can grow with siblings.
				box.distributeSize(itemContext, cx - w / 2.0, itemY, w, contentHeight);
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
			double rp = row.getRowPadding();
			double labelX = offsetX + rowLabelPadding + depth * indentWidth;
			double labelY = rowYStart[i] + rp;
			double labelW = columnWidth - 2 * rowLabelPadding - depth * indentWidth;
			double labelH = rowTotalHeight[i] - 2 * rp;
			// Select the variant for the zoom of the time axis within the reserved column.
			label.computeIntrinsicSize(itemContext, labelX, labelY,
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			label.distributeSize(itemContext, labelX, labelY, Math.max(0, labelW), labelH);
		}

		// Compute frozen header vs. scrollable data region heights.
		int frozenRows = self.getFrozenRows();
		double headerH = 0;
		if (frozenRows > 0 && frozenRows <= self.getRootRows().size()) {
			int frozenFlatCount = countFrozenFlatRows(self.getRootRows(), frozenRows);
			for (int i = 0; i < frozenFlatCount && i < totalRows; i++) {
				headerH += rowTotalHeight[i];
			}
		}
		self.setHeaderHeight(headerH);
		self.setDataHeight(totalHeight - headerH);

		// --- Pass 2c: distribute final positions and sizes for decoration label boxes ---
		if (decos != null && !decos.isEmpty()) {
			double chartX0 = offsetX + columnWidth;
			java.util.Set<String> frozenRowIds = buildFrozenRowIds(self);
			for (GanttDecoration deco : decos) {
				Box decoLabel = deco.getLabel();
				if (decoLabel == null) {
					continue;
				}
				// Determine y0 for this decoration label: the top of the data rows for a label above
				// the frozen rows, otherwise the top of the first relevant row.
				double decoY0;
				if (isPinnedLabel(deco, rowIndex.keySet(), frozenRowIds)) {
					decoY0 = offsetY + headerH;
				} else {
					List<String> relevantFor = deco.getRelevantFor();
					double minY = Double.MAX_VALUE;
					for (String rowId : relevantFor) {
						Integer rowIdx = rowIndex.get(rowId);
						if (rowIdx != null && rowYStart[rowIdx] < minY) {
							minY = rowYStart[rowIdx];
						}
					}
					decoY0 = minY;
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

		// Content larger than the offered area is shown in an internal viewport of the offered size,
		// next to the scrollbars it needs.
		double contentWidth = columnWidth + (self.getRangeMax() - rangeMin) * zoom;
		double maxWidth = Math.max(0, availableWidth);
		double maxHeight = Math.max(0, availableHeight);
		boolean[] scrollbars = GanttScrollGeometry.scrollbars(contentWidth, totalHeight, maxWidth, maxHeight);
		self.setWidth(Math.min(contentWidth + (scrollbars[0] ? SCROLLBAR_SIZE : 0), maxWidth));
		self.setHeight(Math.min(totalHeight + (scrollbars[1] ? SCROLLBAR_SIZE : 0), maxHeight));
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
		computeIntrinsicSize(context, offsetX, offsetY, width, height);
		if (width > self.getWidth()) {
			self.setWidth(width);
		}
		if (height > self.getHeight()) {
			self.setHeight(height);
		}
		clampScroll(self);
	}

	/**
	 * The zoom factor of a layout fitting into the given width.
	 *
	 * <p>
	 * While {@link GanttLayout#isFitWidth()} is set, the time axis is fitted into the given width
	 * next to the label column; a layout with unlimited width (or no room next to the label column)
	 * uses {@link GanttLayout#getInitialZoom()}. Otherwise the current
	 * {@link GanttLayout#getZoom()} is kept.
	 * </p>
	 */
	private static double layoutZoom(GanttLayout self, double columnWidth, double availableWidth) {
		if (!self.isFitWidth()) {
			return self.getZoom();
		}
		double range = self.getRangeMax() - self.getRangeMin();
		double chartWidth = availableWidth - columnWidth;
		if (availableWidth < Double.POSITIVE_INFINITY && range > 0 && chartWidth > 0) {
			return chartWidth / range;
		}
		double initialZoom = self.getInitialZoom();
		return initialZoom > 0 ? initialZoom : 1.0;
	}

	/**
	 * Whether the content of the chart is larger than its box, so that it is shown in an internal
	 * viewport (clipped, scrollable regions with the frozen header rows on top and the row labels
	 * on the left).
	 */
	private static boolean isViewportActive(GanttLayout self) {
		return GanttScrollGeometry.of(self).active();
	}

	/**
	 * Limits the scroll offsets to the scroll range of the current box size and zoom.
	 */
	private static void clampScroll(GanttLayout self) {
		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		double scrollX = clamp(self.getScrollX(), geometry.maxScrollX(self.getZoom()));
		if (scrollX != self.getScrollX()) {
			self.setScrollX(scrollX);
		}
		double scrollY = clamp(self.getScrollY(), geometry.maxScrollY());
		if (scrollY != self.getScrollY()) {
			self.setScrollY(scrollY);
		}
	}

	private static double clamp(double value, double max) {
		return Math.max(0, Math.min(max, value));
	}

	@Override
	default void draw(SvgWriter out) {
		GanttLayout self = (GanttLayout) this;

		// Create a wrapping group linked to the GanttLayout model.
		// This triggers linkModel() in the SVGBuilder, which assigns clientId
		// and attaches __tlWidget for DOM-to-model lookup.
		out.beginGroup();
		out.attachModel(self);
		out.attachOnWheel(this, self);

		double x0 = self.getX();
		double y0 = self.getY();
		double totalW = self.getWidth();
		double totalH = self.getHeight();

		// Background rect ensures pointer/wheel events are captured even over empty areas
		// (SVG <g> elements only receive events where painted content exists).
		// Items are painted AFTER this rect (in the viewport groups), so they are on top
		// in SVG hit-testing and receive clicks normally.
		out.beginRect(x0, y0, totalW, totalH);
		out.setFill("transparent");
		out.writeAttribute("pointer-events", "all");
		out.endRect();
		double colW = self.getColumnWidth();
		double headerH = self.getHeaderHeight();
		double scrollX = self.getScrollX();
		double scrollY = self.getScrollY();
		double zoom = self.getZoom();
		double scrollXPx = scrollX * zoom;

		// Determine frozen row IDs.
		java.util.Set<String> frozenRowIds = buildFrozenRowIds(self);
		java.util.Set<String> rowIds = buildRowIds(self);

		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		if (!geometry.active()) {
			// The whole content fits into the box: no clipping and scrolling.
			drawRowLanes(self, out, null, true, true);
			drawDecorations(self, out);
			for (Box content : self.getContents()) {
				out.write(content);
			}
			drawEdges(self, out);
			out.endGroup();
			return;
		}

		// --- Define clip regions ---
		String ganttId = self.getClientId();
		String clipCorner = ganttId + "-clip-corner";
		String clipHeader = ganttId + "-clip-header";
		String clipSidebar = ganttId + "-clip-sidebar";
		String clipContent = ganttId + "-clip-content";
		String clipDecorations = ganttId + "-clip-deco";

		double viewW = geometry.viewWidth();
		double viewH = geometry.viewHeight();
		emitClipPath(out, clipCorner, x0, y0, colW, headerH);
		emitClipPath(out, clipHeader, x0 + colW, y0, viewW, headerH);
		emitClipPath(out, clipSidebar, x0, y0 + headerH, colW, viewH);
		emitClipPath(out, clipContent, x0 + colW, y0 + headerH, viewW, viewH);
		emitClipPath(out, clipDecorations, x0 + colW, y0, viewW, headerH + viewH);

		// --- 1. Decorations group (scroll X only, spans full height) ---
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipDecorations + ")");
		out.writeCssClass("tl-gantt-vp-decorations");
		out.beginGroup();
		out.writeId(ganttId + SCROLL_DECORATIONS_ID_SUFFIX);
		out.translate(-scrollXPx, 0);
		drawDecorations(self, out);
		out.endGroup();
		out.endGroup();

		// --- 2. Content group (scroll X+Y) ---
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipContent + ")");
		out.writeCssClass("tl-gantt-vp-content");
		out.beginGroup();
		out.writeId(ganttId + SCROLL_CONTENT_ID_SUFFIX);
		out.translate(-scrollXPx, -scrollY);
		drawRowLanes(self, out, frozenRowIds, false, true);
		drawItemBoxes(self, out, frozenRowIds, false);
		drawDecorationLabels(self, out, frozenRowIds, rowIds, false);
		drawEdges(self, out);
		out.endGroup();
		out.endGroup();

		// --- 3. Sidebar group (scroll Y only) ---
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipSidebar + ")");
		out.writeCssClass("tl-gantt-vp-sidebar");
		out.beginRect(x0, y0 + headerH, colW, viewH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		out.beginGroup();
		out.writeId(ganttId + SCROLL_SIDEBAR_ID_SUFFIX);
		out.translate(0, -scrollY);
		drawRowLabels(self, out, frozenRowIds, false);
		out.endGroup();
		out.endGroup();

		// --- 4. Header group (scroll X only) ---
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipHeader + ")");
		out.writeCssClass("tl-gantt-vp-header");
		out.beginRect(x0 + colW, y0, viewW, headerH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		out.beginGroup();
		out.writeId(ganttId + SCROLL_HEADER_ID_SUFFIX);
		out.translate(-scrollXPx, 0);
		drawRowLanes(self, out, frozenRowIds, true, false);
		drawItemBoxes(self, out, frozenRowIds, true);
		out.endGroup();
		out.endGroup();

		// --- 5. Corner group (no scroll) ---
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipCorner + ")");
		out.writeCssClass("tl-gantt-vp-corner");
		out.beginRect(x0, y0, colW, headerH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		drawRowLanes(self, out, frozenRowIds, true, false);
		drawRowLabels(self, out, frozenRowIds, true);
		out.endGroup();

		// --- 6. Labels of decorations above the frozen rows (scroll X only) ---
		// No background: the layer catches events only where a label is drawn.
		out.beginGroup();
		out.writeAttribute("clip-path", "url(#" + clipDecorations + ")");
		out.writeCssClass("tl-gantt-vp-decoration-labels");
		out.beginGroup();
		out.writeId(ganttId + SCROLL_DECORATION_LABELS_ID_SUFFIX);
		out.translate(-scrollXPx, 0);
		drawDecorationLabels(self, out, frozenRowIds, rowIds, true);
		out.endGroup();
		out.endGroup();

		// --- 7. Scrollbars (on top of everything) ---
		drawScrollbars(self, geometry, out);

		out.endGroup(); // Close wrapping group from beginGroup(self).
	}

	/**
	 * Draws the scrollbars of the internal viewport.
	 *
	 * <p>
	 * A thumb is drawn at the start of its track inside a group that is translated to the thumb
	 * position, so that a scroll change only updates the translation of that group, see
	 * {@link #applyScroll(GanttLayout, PanRenderer)}.
	 * </p>
	 */
	private static void drawScrollbars(GanttLayout self, GanttScrollGeometry geometry, SvgWriter out) {
		String ganttId = self.getClientId();
		double inset = SCROLLBAR_THUMB_INSET;
		double thickness = Math.max(0, SCROLLBAR_SIZE - 2 * inset);
		if (geometry.vertical()) {
			double trackX = geometry.verticalTrackX();
			double trackY = geometry.viewY();
			out.beginGroup();
			out.writeCssClass(SCROLLBAR_CSS_CLASS);
			out.beginRect(trackX, trackY, SCROLLBAR_SIZE, geometry.viewHeight());
			out.writeCssClass(SCROLLBAR_TRACK_CSS_CLASS);
			out.attachOnClick(event -> pageScroll(self, event, true), self);
			out.endRect();
			out.beginGroup();
			out.writeId(ganttId + VERTICAL_THUMB_ID_SUFFIX);
			out.translate(0, geometry.verticalThumbOffset());
			out.beginRect(trackX + inset, trackY, thickness, geometry.verticalThumbLength());
			out.writeCssClass(SCROLLBAR_THUMB_CSS_CLASS);
			out.attachOnClick(GanttLayoutOperations::consumeClick, self);
			out.endRect();
			out.endGroup();
			out.endGroup();
		}
		if (geometry.horizontal()) {
			double trackX = geometry.viewX();
			double trackY = geometry.horizontalTrackY();
			out.beginGroup();
			out.writeCssClass(SCROLLBAR_CSS_CLASS);
			out.beginRect(trackX, trackY, geometry.viewWidth(), SCROLLBAR_SIZE);
			out.writeCssClass(SCROLLBAR_TRACK_CSS_CLASS);
			out.attachOnClick(event -> pageScroll(self, event, false), self);
			out.endRect();
			out.beginGroup();
			out.writeId(ganttId + HORIZONTAL_THUMB_ID_SUFFIX);
			out.translate(geometry.horizontalThumbOffset(), 0);
			out.beginRect(trackX, trackY + inset, geometry.horizontalThumbLength(), thickness);
			out.writeCssClass(SCROLLBAR_THUMB_CSS_CLASS);
			out.attachOnClick(GanttLayoutOperations::consumeClick, self);
			out.endRect();
			out.endGroup();
			out.endGroup();
		}
	}

	/**
	 * Scrolls by one page (the visible size of the viewport) towards the position of a click on
	 * the track of a scrollbar.
	 *
	 * @param vertical
	 *        Whether the track of the vertical scrollbar was clicked.
	 */
	private static void pageScroll(GanttLayout self, SVGClickEvent event, boolean vertical) {
		// The click belongs to the scrollbar, not to the elements or the diagram below it.
		event.stopPropagation();
		if (!event.getButton(MouseButton.LEFT)) {
			return;
		}
		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		double direction;
		if (vertical) {
			double thumbStart = geometry.viewY() + geometry.verticalThumbOffset();
			direction = pageDirection(event.getOffsetY(), thumbStart, thumbStart + geometry.verticalThumbLength());
			self.setScrollY(clamp(self.getScrollY() + direction * geometry.viewHeight(), geometry.maxScrollY()));
		} else {
			double thumbStart = geometry.viewX() + geometry.horizontalThumbOffset();
			direction = pageDirection(event.getOffsetX(), thumbStart, thumbStart + geometry.horizontalThumbLength());
			double zoom = self.getZoom();
			self.setScrollX(clamp(self.getScrollX() + direction * geometry.viewWidth() / zoom, geometry.maxScrollX(zoom)));
		}
		applyScroll(self, event::updateTransform);
	}

	/**
	 * The direction of a page scroll for a click at the given position on a track: -1 before the
	 * thumb, 1 after it, 0 on it.
	 */
	private static double pageDirection(double position, double thumbStart, double thumbEnd) {
		if (position < thumbStart) {
			return -1;
		}
		if (position > thumbEnd) {
			return 1;
		}
		return 0;
	}

	/**
	 * Keeps a click on a scrollbar thumb from reaching the elements and the diagram below it.
	 */
	private static void consumeClick(SVGClickEvent event) {
		event.stopPropagation();
	}

	/**
	 * Draws row lane backgrounds and borders.
	 *
	 * @param frozenRowIds set of frozen row IDs (null = no viewport, draw all)
	 * @param drawFrozen if true, draw lanes for rows IN frozenRowIds
	 * @param drawData if true, draw lanes for rows NOT IN frozenRowIds
	 */
	/**
	 * Computes the full virtual content width (column + time range at current zoom).
	 * This may be larger than the viewport width ({@code self.getWidth()}) when zoomed in.
	 */
	private static double virtualTotalWidth(GanttLayout self) {
		return self.getColumnWidth() + (self.getRangeMax() - self.getRangeMin()) * self.getZoom();
	}

	private static void drawRowLanes(GanttLayout self, SvgWriter out,
			java.util.Set<String> frozenRowIds, boolean drawFrozen, boolean drawData) {
		double x0 = self.getX();
		double totalWidth = Math.max(self.getWidth(), virtualTotalWidth(self));
		double columnWidth = self.getColumnWidth();

		Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());

		out.beginGroup();
		out.writeCssClass("tl-gantt-lanes");

		int[] rowIndex = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			drawRowLane(root, x0, totalWidth, columnWidth,
				rowGeometry, out, rowIndex, frozenRowIds, drawFrozen, drawData);
		}

		out.endGroup();
	}

	private static void drawRowLane(GanttRow row,
			double x0, double totalWidth, double columnWidth,
			Map<String, RowGeometry> rowGeometry, SvgWriter out, int[] rowIndex,
			java.util.Set<String> frozenRowIds, boolean drawFrozen, boolean drawData) {
		int idx = rowIndex[0]++;

		boolean isFrozen = frozenRowIds != null && frozenRowIds.contains(row.getId());
		boolean shouldDraw = (isFrozen && drawFrozen) || (!isFrozen && drawData)
			|| frozenRowIds == null;

		if (shouldDraw) {
			RowGeometry geom = rowGeometry.get(row.getId());
			if (geom != null) {
				double rowY = geom.yStart();
				double rowHeight = geom.height();

				String bgColor = row.getBackgroundColor();
				String bdColor = row.getBorderColor();
				if (bgColor != null && !bgColor.isEmpty() || bdColor != null && !bdColor.isEmpty()) {
					out.beginRect(x0, rowY, totalWidth, rowHeight);
					out.setFill(bgColor != null && !bgColor.isEmpty() ? bgColor : "none");
					if (bdColor != null && !bdColor.isEmpty()) {
						out.setStroke(bdColor);
						out.setStrokeWidth(1.0);
					} else {
						out.setStroke("none");
					}
					out.endRect();
				}

				if (bdColor != null && !bdColor.isEmpty()) {
					double sepX = x0 + columnWidth;
					out.beginPath();
					out.setStroke(bdColor);
					out.setStrokeWidth(1.0);
					out.setFill("none");
					out.beginData();
					out.moveToAbs(sepX, rowY);
					out.lineToAbs(sepX, rowY + rowHeight);
					out.endData();
					out.endPath();
				}
			}
		}

		for (GanttRow child : row.getChildren()) {
			drawRowLane(child, x0, totalWidth, columnWidth,
				rowGeometry, out, rowIndex, frozenRowIds, drawFrozen, drawData);
		}
	}

	private static void emitClipPath(SvgWriter out, String id, double x, double y, double w, double h) {
		out.beginClipPath();
		out.writeAttribute("id", id);
		out.beginRect(x, y, w, h);
		out.endRect();
		out.endClipPath();
	}

	/**
	 * Returns the set of row IDs that belong to frozen (header) rows.
	 */
	private static java.util.Set<String> buildFrozenRowIds(GanttLayout self) {
		java.util.Set<String> frozen = new java.util.HashSet<>();
		int frozenCount = self.getFrozenRows();
		List<GanttRow> roots = self.getRootRows();
		for (int i = 0; i < Math.min(frozenCount, roots.size()); i++) {
			collectRowIds(roots.get(i), frozen);
		}
		return frozen;
	}

	private static void collectRowIds(GanttRow row, java.util.Set<String> ids) {
		ids.add(row.getId());
		for (GanttRow child : row.getChildren()) {
			collectRowIds(child, ids);
		}
	}

	/**
	 * Draws item boxes for either frozen or data rows.
	 *
	 * @param frozenRowIds set of frozen row IDs
	 * @param drawFrozen if true, draw items in frozen rows; if false, draw items in data rows
	 */
	private static void drawItemBoxes(GanttLayout self, SvgWriter out,
			java.util.Set<String> frozenRowIds, boolean drawFrozen) {
		for (GanttItem item : self.getItems()) {
			boolean isFrozen = frozenRowIds.contains(item.getRowId());
			if (isFrozen == drawFrozen) {
				Box box = item.getBox();
				if (box != null) {
					out.write(box);
				}
			}
		}
	}

	/**
	 * Draws the labels of either the decorations above the frozen rows or the decorations bound
	 * to data rows.
	 *
	 * @param frozenRowIds set of frozen row IDs
	 * @param rowIds set of the IDs of all rows
	 * @param drawPinned if true, draw the labels drawn above the frozen rows (see
	 *        {@link #isPinnedLabel(GanttDecoration, java.util.Set, java.util.Set)}), otherwise
	 *        the labels moving with their data rows
	 */
	private static void drawDecorationLabels(GanttLayout self, SvgWriter out,
			java.util.Set<String> frozenRowIds, java.util.Set<String> rowIds, boolean drawPinned) {
		List<GanttDecoration> decos = self.getDecorations();
		if (decos == null) {
			return;
		}
		for (GanttDecoration deco : decos) {
			Box label = deco.getLabel();
			if (label != null && isPinnedLabel(deco, rowIds, frozenRowIds) == drawPinned) {
				out.write(label);
			}
		}
	}

	/**
	 * Whether the label of the given decoration is drawn above the frozen rows at the top of the
	 * data rows, so that it stays visible while the data rows scroll vertically.
	 *
	 * <p>
	 * This is the case for a decoration spanning all rows (no relevant row, or none of its
	 * relevant rows exists) and for a decoration whose first relevant row is a frozen row. Since
	 * the frozen rows are the top rows, the latter is a decoration with any frozen relevant row.
	 * The label of every other decoration moves with its first relevant row.
	 * </p>
	 *
	 * @param rowIds set of the IDs of all rows
	 * @param frozenRowIds set of frozen row IDs
	 */
	private static boolean isPinnedLabel(GanttDecoration deco, java.util.Set<String> rowIds,
			java.util.Set<String> frozenRowIds) {
		List<String> relevantFor = deco.getRelevantFor();
		if (relevantFor == null) {
			return true;
		}
		boolean resolved = false;
		for (String rowId : relevantFor) {
			if (frozenRowIds.contains(rowId)) {
				return true;
			}
			resolved |= rowIds.contains(rowId);
		}
		return !resolved;
	}

	/**
	 * Returns the set of the IDs of all rows.
	 */
	private static java.util.Set<String> buildRowIds(GanttLayout self) {
		java.util.Set<String> ids = new java.util.HashSet<>();
		for (GanttRow root : self.getRootRows()) {
			collectRowIds(root, ids);
		}
		return ids;
	}

	/**
	 * Draws row label boxes for either frozen or data rows.
	 *
	 * @param frozenRowIds set of frozen row IDs
	 * @param drawFrozen if true, draw labels for frozen rows; if false, draw labels for data rows
	 */
	private static void drawRowLabels(GanttLayout self, SvgWriter out,
			java.util.Set<String> frozenRowIds, boolean drawFrozen) {
		for (GanttRow root : self.getRootRows()) {
			drawRowLabel(root, out, frozenRowIds, drawFrozen);
		}
	}

	private static void drawRowLabel(GanttRow row, SvgWriter out,
			java.util.Set<String> frozenRowIds, boolean drawFrozen) {
		boolean isFrozen = frozenRowIds.contains(row.getId());
		if (isFrozen == drawFrozen) {
			Box label = row.getLabel();
			if (label != null) {
				out.write(label);
			}
		}
		for (GanttRow child : row.getChildren()) {
			drawRowLabel(child, out, frozenRowIds, drawFrozen);
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
			// Fallback: sum up per-row min content heights (no padding in this rough estimate).
			double fallbackHeight = 0;
			for (GanttRow root : self.getRootRows()) {
				fallbackHeight += sumMinContentHeight(root);
			}
			chartY1 = chartY0 + fallbackHeight;
		}

		double zoom = self.getZoom();
		double rangeMin = self.getRangeMin();
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

			// The first and last segments are horizontal stubs out of the item boxes, to the left
			// of a START and to the right of an END endpoint. A symbol takes the inner part of its
			// stub: the line starts behind it, so it does not show through a closed or filled
			// symbol, and the stub is longer by the symbol inset, so the line runs
			// EDGE_HORIZONTAL_STUB beyond the symbol for every stroke width.
			ConnectorSymbol sourceSymbol = edge.getSourceSymbol();
			ConnectorSymbol targetSymbol = edge.getTargetSymbol();
			double symbolScale = strokeWidth / 2;
			double sInset = ConnectorSymbolRenderer.inset(sourceSymbol, symbolScale);
			double tInset = ConnectorSymbolRenderer.inset(targetSymbol, symbolScale);
			double sDir = (sourceEndpoint == GanttEndpoint.START) ? -1 : 1;
			double tDir = (targetEndpoint == GanttEndpoint.START) ? -1 : 1;
			double sOffset = sDir * (EDGE_HORIZONTAL_STUB + sInset);
			double tOffset = tDir * (EDGE_HORIZONTAL_STUB + tInset);

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
			out.moveToAbs(sx + sDir * sInset, sy);
			out.lineToAbs(sx + sOffset, sy);
			out.lineToAbs(sx + sOffset, midY);
			out.lineToAbs(tx + tOffset, midY);
			out.lineToAbs(tx + tOffset, ty);
			out.lineToAbs(tx + tDir * tInset, ty);
			out.endData();
			out.endPath();

			// Symbols point from the stub into the item box. Each symbol is a path of its own
			// without dash array, so it is drawn solid also for a dashed edge.
			ConnectorSymbolRenderer.drawSymbol(out, sx, sy, -sDir, 0, sourceSymbol, strokeColor, strokeWidth);
			ConnectorSymbolRenderer.drawSymbol(out, tx, ty, -tDir, 0, targetSymbol, strokeColor, strokeWidth);
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
		// Build ordered row index map and row list (to look up per-row padding).
		LinkedHashMap<String, Integer> rowIndex = new LinkedHashMap<>();
		List<GanttRow> rowList = new java.util.ArrayList<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, rowIndex, counter, rowList);
		}
		int totalRows = counter[0];

		// Compute per-row max content height from already-distributed item boxes.
		double[] rowMaxContentHeight = new double[totalRows];
		for (int i = 0; i < totalRows; i++) {
			rowMaxContentHeight[i] = rowList.get(i).getMinContentHeight();
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
		double y = lanesTop;
		int i = 0;
		for (Map.Entry<String, Integer> entry : rowIndex.entrySet()) {
			double rp = rowList.get(i).getRowPadding();
			double rowHeight = rowMaxContentHeight[i] + 2 * rp;
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

	/** Sums the {@link GanttRow#getMinContentHeight()} of a row and all its descendants. */
	private static double sumMinContentHeight(GanttRow row) {
		double sum = row.getMinContentHeight();
		for (GanttRow child : row.getChildren()) {
			sum += sumMinContentHeight(child);
		}
		return sum;
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

		double columnWidth = self.getColumnWidth();
		double chartWidth = (self.getRangeMax() - self.getRangeMin()) * self.getZoom();

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
		double zoom = self.getZoom();
		double snap = self.getSnapTo();
		double columnWidth = self.getColumnWidth();
		double rangeMin = self.getRangeMin();
		double rangeMax = self.getRangeMax();
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
			Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());
			Map<String, GanttRow> rowsById = buildRowIndex(self);
			double bestY = proposedY;
			double bestDist = Double.MAX_VALUE;
			for (DropArea area : areas) {
				// Find the row that starts at area.getY() to get its padding.
				double areaRowPadding = 4.0; // default fallback
				for (Map.Entry<String, RowGeometry> entry : rowGeometry.entrySet()) {
					if (Math.abs(entry.getValue().yStart() - area.getY()) < 0.5) {
						GanttRow areaRow = rowsById.get(entry.getKey());
						if (areaRow != null) {
							areaRowPadding = areaRow.getRowPadding();
						}
						break;
					}
				}
				double candidateY = area.getY() + areaRowPadding;
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
		double zoom = self.getZoom();
		double snap = self.getSnapTo();
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

		double zoom = self.getZoom();
		double rangeMin = self.getRangeMin();
		double columnWidth = self.getColumnWidth();
		double layoutX = self.getX();

		// Determine if row changed.
		boolean rowChanged = Math.abs(finalY - box.getY()) > 0.5;
		if (rowChanged) {
			// Find target row from Y position using row geometry.
			Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());
			Map<String, GanttRow> rowsById = buildRowIndex(self);
			String targetRowId = item.getRowId(); // default to current
			for (Map.Entry<String, RowGeometry> entry : rowGeometry.entrySet()) {
				RowGeometry geom = entry.getValue();
				GanttRow entryRow = rowsById.get(entry.getKey());
				double rp = entryRow != null ? entryRow.getRowPadding() : 4.0;
				double rowContentY = geom.yStart() + rp;
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

	// -----------------------------------------------------------------------
	// SVGWheelHandler implementation (viewport scroll and zoom)
	// -----------------------------------------------------------------------

	@Override
	default void onWheel(SVGWheelEvent event) {
		GanttLayout self = (GanttLayout) this;
		if (event.isCtrlKey()) {
			// Zooming the time axis is always the chart's own gesture.
			event.stopPropagation();
			event.preventDefault();
			handleGanttZoom(self, event);
		} else if (isViewportActive(self)) {
			event.stopPropagation();
			event.preventDefault();
			handleGanttScroll(self, event);
		} else {
			// Nothing to scroll - let the event propagate to the diagram.
		}
	}

	private static void handleGanttScroll(GanttLayout self, SVGWheelEvent event) {
		double zoom = self.getZoom();
		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		double maxScrollX = geometry.maxScrollX(zoom);
		double maxScrollY = geometry.maxScrollY();

		double deltaX;
		double deltaY;
		if (event.isShiftKey()) {
			deltaX = event.getDeltaY();
			deltaY = event.getDeltaX();
		} else {
			deltaX = event.getDeltaX();
			deltaY = event.getDeltaY();
		}

		double newScrollX = self.getScrollX() + deltaX / zoom;
		double newScrollY = self.getScrollY() + deltaY;

		newScrollX = Math.max(0, Math.min(maxScrollX, newScrollX));
		newScrollY = Math.max(0, Math.min(maxScrollY, newScrollY));

		self.setScrollX(newScrollX);
		self.setScrollY(newScrollY);

		applyScroll(self, event::updateTransform);
	}

	private static void handleGanttZoom(GanttLayout self, SVGWheelEvent event) {
		double currentZoom = self.getZoom();

		double delta = event.getDeltaY() == 0 ? event.getDeltaX() : event.getDeltaY();
		double direction = Math.signum(delta);
		double factor = 1.0 - direction * 0.1;
		double newZoom = currentZoom * factor;

		// A time axis fitted below the minimum zoom stays reachable without a jump.
		double minZoom = Math.min(MIN_ZOOM, fittingZoom(self));
		newZoom = Math.max(minZoom, Math.min(MAX_ZOOM, newZoom));

		if (Math.abs(newZoom - currentZoom) < 0.001) {
			return;
		}

		// Adjust scrollX to keep the mouse position stable.
		// getOffsetX() returns SVG coordinates (converted from client coords via CTM).
		double colW = self.getColumnWidth();
		double mouseXInChart = Math.max(0, event.getOffsetX() - self.getX() - colW);

		double scrollXPxBefore = self.getScrollX() * currentZoom;
		double timeUnderMouse = (mouseXInChart + scrollXPxBefore) / currentZoom + self.getRangeMin();

		double newScrollXPx = (timeUnderMouse - self.getRangeMin()) * newZoom - mouseXInChart;
		double newScrollX = newScrollXPx / newZoom;

		newScrollX = clamp(newScrollX, GanttScrollGeometry.of(self).maxScrollX(newZoom));

		self.setFitWidth(false);
		self.setZoom(newZoom);
		self.setScrollX(newScrollX);

		event.requestRelayout();
	}

	/**
	 * The zoom at which the time axis exactly fills the current box next to the label column (and
	 * the vertical scrollbar), or
	 * {@link Double#POSITIVE_INFINITY} if there is no such zoom.
	 */
	private static double fittingZoom(GanttLayout self) {
		double range = self.getRangeMax() - self.getRangeMin();
		double chartWidth = GanttScrollGeometry.of(self).viewWidth();
		if (range <= 0 || chartWidth <= 0) {
			return Double.POSITIVE_INFINITY;
		}
		return chartWidth / range;
	}

	@Override
	default double[] svgToLayout(double svgX, double svgY) {
		GanttLayout self = (GanttLayout) this;
		double scrollXPx = self.getScrollX() * self.getZoom();
		double scrollY = self.getScrollY();
		return new double[] { svgX + scrollXPx, svgY + scrollY };
	}

	@Override
	default double[] layoutToSvg(double layoutX, double layoutY) {
		GanttLayout self = (GanttLayout) this;
		double scrollXPx = self.getScrollX() * self.getZoom();
		double scrollY = self.getScrollY();
		return new double[] { layoutX - scrollXPx, layoutY - scrollY };
	}

	// -----------------------------------------------------------------------
	// DragController pan support (drag-to-pan on empty space, dragging a scrollbar thumb)
	// -----------------------------------------------------------------------

	@Override
	default boolean canPan() {
		return isViewportActive((GanttLayout) this);
	}

	/**
	 * Every press in an active viewport starts a pan, except one on a scrollbar track beside its
	 * thumb: that is a click scrolling by a page.
	 */
	@Override
	default boolean canPan(double svgX, double svgY) {
		GanttScrollGeometry geometry = GanttScrollGeometry.of((GanttLayout) this);
		if (!geometry.active()) {
			return false;
		}
		if (geometry.inVerticalTrack(svgX, svgY) && !geometry.inVerticalThumb(svgX, svgY)) {
			return false;
		}
		if (geometry.inHorizontalTrack(svgX, svgY) && !geometry.inHorizontalThumb(svgX, svgY)) {
			return false;
		}
		return true;
	}

	@Override
	default void startPan(double svgX, double svgY) {
		GanttLayout self = (GanttLayout) this;
		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		GanttPanTarget target;
		if (geometry.inVerticalThumb(svgX, svgY)) {
			target = GanttPanTarget.VERTICAL_THUMB;
		} else if (geometry.inHorizontalThumb(svgX, svgY)) {
			target = GanttPanTarget.HORIZONTAL_THUMB;
		} else {
			target = GanttPanTarget.CONTENT;
		}
		self.setPanTarget(target);
		self.setPanStartX(svgX);
		self.setPanStartY(svgY);
		self.setPanStartScrollX(self.getScrollX());
		self.setPanStartScrollY(self.getScrollY());
	}

	@Override
	default void panTo(double svgX, double svgY) {
		GanttLayout self = (GanttLayout) this;
		double zoom = self.getZoom();
		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);

		double deltaSvgX = svgX - self.getPanStartX();
		double deltaSvgY = svgY - self.getPanStartY();

		double newScrollX = self.getPanStartScrollX();
		double newScrollY = self.getPanStartScrollY();
		switch (self.getPanTarget()) {
			case VERTICAL_THUMB:
				// The thumb follows the pointer along its track.
				newScrollY += deltaSvgY * geometry.verticalScrollPerThumbPixel();
				break;
			case HORIZONTAL_THUMB:
				newScrollX += deltaSvgX * geometry.horizontalScrollPerThumbPixel();
				break;
			case CONTENT:
				// The content follows the pointer: dragging right scrolls left.
				newScrollX -= deltaSvgX / zoom;
				newScrollY -= deltaSvgY;
				break;
		}

		self.setScrollX(clamp(newScrollX, geometry.maxScrollX(zoom)));
		self.setScrollY(clamp(newScrollY, geometry.maxScrollY()));
	}

	@Override
	default void renderPan(PanRenderer renderer) {
		applyScroll((GanttLayout) this, renderer);
	}

	/**
	 * Moves the scrolling groups and the scrollbar thumbs of the viewport to the current scroll
	 * offsets, without drawing the chart again.
	 */
	private static void applyScroll(GanttLayout self, PanRenderer renderer) {
		String ganttId = self.getClientId();
		if (ganttId == null) {
			return;
		}

		double scrollXPx = self.getScrollX() * self.getZoom();
		double scrollY = self.getScrollY();

		renderer.setTranslate(ganttId + SCROLL_DECORATIONS_ID_SUFFIX, -scrollXPx, 0);
		renderer.setTranslate(ganttId + SCROLL_DECORATION_LABELS_ID_SUFFIX, -scrollXPx, 0);
		renderer.setTranslate(ganttId + SCROLL_CONTENT_ID_SUFFIX, -scrollXPx, -scrollY);
		renderer.setTranslate(ganttId + SCROLL_SIDEBAR_ID_SUFFIX, 0, -scrollY);
		renderer.setTranslate(ganttId + SCROLL_HEADER_ID_SUFFIX, -scrollXPx, 0);

		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		if (geometry.vertical()) {
			renderer.setTranslate(ganttId + VERTICAL_THUMB_ID_SUFFIX, 0, geometry.verticalThumbOffset());
		}
		if (geometry.horizontal()) {
			renderer.setTranslate(ganttId + HORIZONTAL_THUMB_ID_SUFFIX, geometry.horizontalThumbOffset(), 0);
		}
	}

	/**
	 * Counts the total number of rows (including descendants) in the first
	 * {@code frozenCount} root rows.
	 */
	private static int countFrozenFlatRows(List<GanttRow> rootRows, int frozenCount) {
		int count = 0;
		for (int i = 0; i < Math.min(frozenCount, rootRows.size()); i++) {
			count += countRowAndDescendants(rootRows.get(i));
		}
		return count;
	}

	private static int countRowAndDescendants(GanttRow row) {
		int count = 1;
		for (GanttRow child : row.getChildren()) {
			count += countRowAndDescendants(child);
		}
		return count;
	}
}

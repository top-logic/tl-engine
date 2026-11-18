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
import java.util.function.Consumer;

import com.top_logic.react.flow.data.Box;
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
import com.top_logic.react.flow.data.Sticky;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.operations.ConnectorSymbolRenderer;
import com.top_logic.react.flow.operations.Interactivity;
import com.top_logic.react.flow.operations.StickyOperations;
import com.top_logic.react.flow.operations.WidgetTraversal;
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
 *
 * <p>
 * A {@link Sticky} box within the box of an item keeps its content at the left edge of the
 * visible time range while the chart scrolls horizontally, see {@link #stickToViewport(GanttLayout)}.
 * </p>
 */
public interface GanttLayoutOperations extends BoxOperations, DragController, SVGWheelHandler {

	/** Horizontal stub length for orthogonal edge routing (pixels offset from item edge). */
	double EDGE_HORIZONTAL_STUB = 6.0;

	/** Default stroke width for dependency edges. */
	double EDGE_STROKE_WIDTH_NORMAL = 1.2;

	/** Stroke width for informational (NONE) dependency edges. */
	double EDGE_STROKE_WIDTH_THIN = 0.8;

	/**
	 * Largest vertical distance between the horizontal segments of edges passing through the same
	 * gap between two rows, in pixels.
	 *
	 * @see GanttEdgeRouting
	 */
	double EDGE_LANE_SPACING = 4.0;

	/** Row padding assumed for a drop area that starts at no row, in pixels. */
	double DEFAULT_ROW_PADDING = 4.0;

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
	 * CSS class of the backdrop drawn behind the label of a decoration that is shown above the
	 * frozen rows, at the top of the data rows.
	 */
	String PINNED_LABEL_BACKGROUND_CSS_CLASS = "tl-gantt-pinned-label-bg";

	/** Space between a label above the frozen rows and the edge of its backdrop, in pixels. */
	double PINNED_LABEL_PADDING = 2.0;

	/** Corner radius of the backdrop of a decoration label above the frozen rows, in pixels. */
	double PINNED_LABEL_RADIUS = 3.0;

	/**
	 * Purpose (see {@link SvgWriter#uniqueId(Widget, String)}) of the group moving with both scroll
	 * offsets: the data rows.
	 */
	String SCROLL_CONTENT_PURPOSE = "scroll-content";

	/** Purpose of the group moving with the horizontal scroll offset: the decorations. */
	String SCROLL_DECORATIONS_PURPOSE = "scroll-deco";

	/**
	 * Purpose of the group moving with the horizontal scroll offset: the labels of decorations
	 * that start in the frozen rows or span all rows, drawn above the frozen rows at the top of
	 * the data rows.
	 */
	String SCROLL_DECORATION_LABELS_PURPOSE = "scroll-deco-labels";

	/** Purpose of the group moving with the vertical scroll offset: the row labels. */
	String SCROLL_SIDEBAR_PURPOSE = "scroll-sidebar";

	/** Purpose of the group moving with the horizontal scroll offset: the frozen rows. */
	String SCROLL_HEADER_PURPOSE = "scroll-header";

	/** Purpose of the group moving the thumb of the vertical scrollbar. */
	String VERTICAL_THUMB_PURPOSE = "scrollbar-v-thumb";

	/** Purpose of the group moving the thumb of the horizontal scrollbar. */
	String HORIZONTAL_THUMB_PURPOSE = "scrollbar-h-thumb";

	/** Purpose of the clip path of the corner above the row labels. */
	String CLIP_CORNER_PURPOSE = "clip-corner";

	/** Purpose of the clip path of the frozen rows. */
	String CLIP_HEADER_PURPOSE = "clip-header";

	/** Purpose of the clip path of the row labels. */
	String CLIP_SIDEBAR_PURPOSE = "clip-sidebar";

	/** Purpose of the clip path of the data rows. */
	String CLIP_CONTENT_PURPOSE = "clip-content";

	/** Purpose of the clip path of the decorations, spanning frozen and data rows. */
	String CLIP_DECORATIONS_PURPOSE = "clip-deco";

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
		GanttRowLayout rows = GanttRowLayout.index(self);
		layoutContent(self, rows, context, offsetX, offsetY, availableWidth, availableHeight, availableWidth);
		if (self.isFitWidth() && availableWidth < Double.POSITIVE_INFINITY
			&& GanttScrollGeometry.of(self).vertical()) {
			// The vertical scrollbar takes its space from the fitted time axis.
			layoutContent(self, rows, context, offsetX, offsetY, availableWidth, availableHeight,
				availableWidth - SCROLLBAR_SIZE);
		}
		self.setRowLayout(rows);
	}

	/**
	 * Lays out the chart within the given space.
	 *
	 * @param rows
	 *        The {@link GanttRowLayout#index(GanttLayout) index} of the rows, placed by this
	 *        method.
	 * @param fitWidth
	 *        The width a {@link GanttLayout#isFitWidth() fitted} time axis fills together with the
	 *        label column.
	 */
	private static void layoutContent(GanttLayout self, GanttRowLayout rows, RenderContext context,
			double offsetX, double offsetY, double availableWidth, double availableHeight, double fitWidth) {
		List<GanttRow> rowList = rows.rows();
		int totalRows = rows.size();

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
			int depth = rows.depth(i);
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
			int idx = rows.indexOf(item.getRowId());
			if (idx < 0) {
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
		for (int i = 0; i < totalRows; i++) {
			double rp = rowList.get(i).getRowPadding();
			rowTotalHeight[i] = rowMaxContentHeight[i] + 2 * rp;
		}
		rows.place(offsetY, rowTotalHeight);
		double totalHeight = rows.bottom(offsetY) - offsetY;

		// --- Pass 2: distribute final positions and sizes for item boxes ---
		for (GanttItem item : self.getItems()) {
			int idx = rows.indexOf(item.getRowId());
			if (idx < 0) {
				continue;
			}
			Box box = item.getBox();
			if (box == null) {
				continue;
			}
			double rp = rowList.get(idx).getRowPadding();
			double contentHeight = rowTotalHeight[idx] - 2 * rp;
			double itemY = rows.yStart(idx) + rp;

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
			int depth = rows.depth(i);
			double rp = row.getRowPadding();
			double labelX = offsetX + rowLabelPadding + depth * indentWidth;
			double labelY = rows.yStart(i) + rp;
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
			for (int i = 0; i < rows.frozenCount(); i++) {
				headerH += rowTotalHeight[i];
			}
		}
		self.setHeaderHeight(headerH);
		self.setDataHeight(totalHeight - headerH);

		// --- Pass 2c: distribute final positions and sizes for decoration label boxes ---
		if (decos != null && !decos.isEmpty()) {
			double chartX0 = offsetX + columnWidth;
			for (GanttDecoration deco : decos) {
				Box decoLabel = deco.getLabel();
				if (decoLabel == null) {
					continue;
				}
				// Determine y0 for this decoration label: the top of the data rows for a label above
				// the frozen rows, otherwise the top of the first relevant row.
				double decoY0;
				if (isPinnedLabel(deco, rows)) {
					decoY0 = offsetY + headerH;
				} else {
					List<String> relevantFor = deco.getRelevantFor();
					double minY = Double.MAX_VALUE;
					for (String rowId : relevantFor) {
						int rowIdx = rows.indexOf(rowId);
						if (rowIdx >= 0 && rows.yStart(rowIdx) < minY) {
							minY = rows.yStart(rowIdx);
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
	 * Places the chart into the given box.
	 *
	 * <p>
	 * If the box is the one the chart took in {@link #computeIntrinsicSize(RenderContext, double,
	 * double, double, double)}, the result of that layout is kept. Otherwise the chart is laid out
	 * again within the given box.
	 * </p>
	 */
	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		GanttLayout self = (GanttLayout) this;
		boolean laidOut = self.getRowLayout() != null
			&& offsetX == self.getX() && offsetY == self.getY()
			&& width == self.getWidth() && height == self.getHeight();
		if (!laidOut) {
			computeIntrinsicSize(context, offsetX, offsetY, width, height);
		}
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
		out.writeAttribute(Interactivity.POINTER_EVENTS_ATTR, Interactivity.POINTER_EVENTS_ALL);
		out.endRect();
		double colW = self.getColumnWidth();
		double headerH = self.getHeaderHeight();
		double scrollX = self.getScrollX();
		double scrollY = self.getScrollY();
		double zoom = self.getZoom();
		double scrollXPx = scrollX * zoom;

		stickToViewport(self);

		GanttRowLayout rows = GanttRowLayout.of(self);

		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		if (!geometry.active()) {
			// The whole content fits into the box: no clipping and scrolling.
			drawRowLanes(self, rows, out, true, true);
			drawDecorations(self, rows, out);
			java.util.Set<Box> decorationLabels = buildDecorationLabels(self);
			for (Box content : self.getContents()) {
				if (decorationLabels.contains(content)) {
					drawDecorationLabel(out, content, false);
				} else {
					out.write(content);
				}
			}
			drawEdges(self, rows, out);
			out.endGroup();
			return;
		}

		// --- Define clip regions ---
		String clipCorner = out.uniqueId(self, CLIP_CORNER_PURPOSE);
		String clipHeader = out.uniqueId(self, CLIP_HEADER_PURPOSE);
		String clipSidebar = out.uniqueId(self, CLIP_SIDEBAR_PURPOSE);
		String clipContent = out.uniqueId(self, CLIP_CONTENT_PURPOSE);
		String clipDecorations = out.uniqueId(self, CLIP_DECORATIONS_PURPOSE);

		double viewW = geometry.viewWidth();
		double viewH = geometry.viewHeight();
		emitClipPath(out, clipCorner, x0, y0, colW, headerH);
		emitClipPath(out, clipHeader, x0 + colW, y0, viewW, headerH);
		emitClipPath(out, clipSidebar, x0, y0 + headerH, colW, viewH);
		emitClipPath(out, clipContent, x0 + colW, y0 + headerH, viewW, viewH);
		emitClipPath(out, clipDecorations, x0 + colW, y0, viewW, headerH + viewH);

		// --- 1. Decorations group (scroll X only, spans full height) ---
		out.beginGroup();
		out.writeClipPathRef(clipDecorations);
		out.writeCssClass("tl-gantt-vp-decorations");
		out.beginGroup();
		out.writeId(out.uniqueId(self, SCROLL_DECORATIONS_PURPOSE));
		out.translate(-scrollXPx, 0);
		drawDecorations(self, rows, out);
		out.endGroup();
		out.endGroup();

		// --- 2. Content group (scroll X+Y) ---
		out.beginGroup();
		out.writeClipPathRef(clipContent);
		out.writeCssClass("tl-gantt-vp-content");
		out.beginGroup();
		out.writeId(out.uniqueId(self, SCROLL_CONTENT_PURPOSE));
		out.translate(-scrollXPx, -scrollY);
		drawRowLanes(self, rows, out, false, true);
		drawItemBoxes(self, rows, out, false);
		drawDecorationLabels(self, rows, out, false);
		drawEdges(self, rows, out);
		out.endGroup();
		out.endGroup();

		// --- 3. Sidebar group (scroll Y only) ---
		out.beginGroup();
		out.writeClipPathRef(clipSidebar);
		out.writeCssClass("tl-gantt-vp-sidebar");
		out.beginRect(x0, y0 + headerH, colW, viewH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		out.beginGroup();
		out.writeId(out.uniqueId(self, SCROLL_SIDEBAR_PURPOSE));
		out.translate(0, -scrollY);
		drawRowLabels(rows, out, false);
		out.endGroup();
		out.endGroup();

		// --- 4. Header group (scroll X only) ---
		out.beginGroup();
		out.writeClipPathRef(clipHeader);
		out.writeCssClass("tl-gantt-vp-header");
		out.beginRect(x0 + colW, y0, viewW, headerH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		out.beginGroup();
		out.writeId(out.uniqueId(self, SCROLL_HEADER_PURPOSE));
		out.translate(-scrollXPx, 0);
		drawRowLanes(self, rows, out, true, false);
		drawItemBoxes(self, rows, out, true);
		out.endGroup();
		out.endGroup();

		// --- 5. Corner group (no scroll) ---
		out.beginGroup();
		out.writeClipPathRef(clipCorner);
		out.writeCssClass("tl-gantt-vp-corner");
		out.beginRect(x0, y0, colW, headerH);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
		drawRowLanes(self, rows, out, true, false);
		drawRowLabels(rows, out, true);
		out.endGroup();

		// --- 6. Labels of decorations above the frozen rows (scroll X only) ---
		// No background, and the pointer passes through the layer except at interactive labels.
		out.beginGroup();
		out.writeClipPathRef(clipDecorations);
		out.writeCssClass("tl-gantt-vp-decoration-labels");
		Interactivity.writePassThrough(out);
		out.beginGroup();
		out.writeId(out.uniqueId(self, SCROLL_DECORATION_LABELS_PURPOSE));
		out.translate(-scrollXPx, 0);
		drawDecorationLabels(self, rows, out, true);
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
			out.writeId(out.uniqueId(self, VERTICAL_THUMB_PURPOSE));
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
			out.writeId(out.uniqueId(self, HORIZONTAL_THUMB_PURPOSE));
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
	 * Computes the full virtual content width (column + time range at current zoom).
	 * This may be larger than the viewport width ({@code self.getWidth()}) when zoomed in.
	 */
	private static double virtualTotalWidth(GanttLayout self) {
		return self.getColumnWidth() + (self.getRangeMax() - self.getRangeMin()) * self.getZoom();
	}

	/**
	 * Draws row lane backgrounds and borders.
	 *
	 * @param drawFrozen if true, draw lanes of the frozen header rows
	 * @param drawData if true, draw lanes of the data rows
	 */
	private static void drawRowLanes(GanttLayout self, GanttRowLayout rows, SvgWriter out,
			boolean drawFrozen, boolean drawData) {
		double x0 = self.getX();
		double totalWidth = Math.max(self.getWidth(), virtualTotalWidth(self));
		double columnWidth = self.getColumnWidth();

		out.beginGroup();
		out.writeCssClass("tl-gantt-lanes");

		for (int i = 0, cnt = rows.size(); i < cnt; i++) {
			if (rows.isFrozen(i) ? drawFrozen : drawData) {
				drawRowLane(rows.row(i), x0, totalWidth, columnWidth, rows.yStart(i), rows.height(i), out);
			}
		}

		out.endGroup();
	}

	private static void drawRowLane(GanttRow row, double x0, double totalWidth, double columnWidth,
			double rowY, double rowHeight, SvgWriter out) {
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

	private static void emitClipPath(SvgWriter out, String id, double x, double y, double w, double h) {
		out.beginClipPath();
		out.writeId(id);
		out.beginRect(x, y, w, h);
		out.endRect();
		out.endClipPath();
	}

	/**
	 * Draws item boxes for either frozen or data rows.
	 *
	 * @param drawFrozen if true, draw items in frozen rows; if false, draw items in data rows
	 */
	private static void drawItemBoxes(GanttLayout self, GanttRowLayout rows, SvgWriter out,
			boolean drawFrozen) {
		for (GanttItem item : self.getItems()) {
			boolean isFrozen = rows.isFrozen(item.getRowId());
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
	 * @param drawPinned if true, draw the labels drawn above the frozen rows (see
	 *        {@link #isPinnedLabel(GanttDecoration, GanttRowLayout)}), otherwise the labels
	 *        moving with their data rows
	 */
	private static void drawDecorationLabels(GanttLayout self, GanttRowLayout rows, SvgWriter out,
			boolean drawPinned) {
		List<GanttDecoration> decos = self.getDecorations();
		if (decos == null) {
			return;
		}
		for (GanttDecoration deco : decos) {
			Box label = deco.getLabel();
			if (label != null && isPinnedLabel(deco, rows) == drawPinned) {
				drawDecorationLabel(out, label, drawPinned);
			}
		}
	}

	/**
	 * Draws the label of a decoration.
	 *
	 * <p>
	 * The label is drawn on top of the items. It receives pointer events only if it contains an
	 * interactive part, see {@link Interactivity#writeOverlayPointerEvents(SvgWriter,
	 * com.top_logic.react.flow.data.Widget)}; otherwise clicks reach the items below it. Its
	 * backdrop counts as part of the label.
	 * </p>
	 *
	 * @param withBackground
	 *        Whether to draw a backdrop behind the label, see
	 *        {@link #drawPinnedLabelBackground(SvgWriter, Box)}.
	 */
	private static void drawDecorationLabel(SvgWriter out, Box label, boolean withBackground) {
		out.beginGroup();
		Interactivity.writeOverlayPointerEvents(out, label);
		if (withBackground) {
			drawPinnedLabelBackground(out, label);
		}
		out.write(label);
		out.endGroup();
	}

	/**
	 * Returns the labels of all decorations.
	 */
	private static java.util.Set<Box> buildDecorationLabels(GanttLayout self) {
		java.util.Set<Box> labels = new java.util.HashSet<>();
		List<GanttDecoration> decos = self.getDecorations();
		if (decos != null) {
			for (GanttDecoration deco : decos) {
				Box label = deco.getLabel();
				if (label != null) {
					labels.add(label);
				}
			}
		}
		return labels;
	}

	/**
	 * Draws the backdrop of a decoration label above the frozen rows, so that the label stays
	 * readable over the bars, milestones and edges of the data row below it.
	 *
	 * <p>
	 * The backdrop is white, which an exported chart keeps; on the screen the
	 * {@link #PINNED_LABEL_BACKGROUND_CSS_CLASS} gives it the surface color of the theme.
	 * </p>
	 */
	private static void drawPinnedLabelBackground(SvgWriter out, Box label) {
		double padding = PINNED_LABEL_PADDING;
		out.beginRect(label.getX() - padding, label.getY() - padding,
			label.getWidth() + 2 * padding, label.getHeight() + 2 * padding,
			PINNED_LABEL_RADIUS, PINNED_LABEL_RADIUS);
		out.writeCssClass(PINNED_LABEL_BACKGROUND_CSS_CLASS);
		out.setFill("#ffffff");
		out.setStroke("none");
		out.endRect();
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
	 */
	private static boolean isPinnedLabel(GanttDecoration deco, GanttRowLayout rows) {
		List<String> relevantFor = deco.getRelevantFor();
		if (relevantFor == null) {
			return true;
		}
		boolean resolved = false;
		for (String rowId : relevantFor) {
			if (rows.isFrozen(rowId)) {
				return true;
			}
			resolved |= rows.contains(rowId);
		}
		return !resolved;
	}

	/**
	 * Draws row label boxes for either frozen or data rows.
	 *
	 * @param drawFrozen if true, draw labels for frozen rows; if false, draw labels for data rows
	 */
	private static void drawRowLabels(GanttRowLayout rows, SvgWriter out, boolean drawFrozen) {
		for (int i = 0, cnt = rows.size(); i < cnt; i++) {
			if (rows.isFrozen(i) == drawFrozen) {
				Box label = rows.row(i).getLabel();
				if (label != null) {
					out.write(label);
				}
			}
		}
	}

	private static void drawDecorations(GanttLayout self, GanttRowLayout rows, SvgWriter out) {
		List<GanttDecoration> decorations = self.getDecorations();
		if (decorations == null || decorations.isEmpty()) {
			return;
		}

		// Vertical extent of the whole chart (from first row start to last row end).
		double chartY0 = self.getY();
		double chartY1 = rows.bottom(chartY0);

		double zoom = self.getZoom();
		double rangeMin = self.getRangeMin();
		double chartX0 = self.getX() + self.getColumnWidth();

		out.beginGroup();
		out.writeCssClass("tl-gantt-decorations");
		// The lines and ranges of decorations are not interactive.
		Interactivity.writePassThrough(out);

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
					RowGeometry geom = rows.geometry(rowId);
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

	/**
	 * Draws the dependency edges with their horizontal segments placed by {@link GanttEdgeRouting}.
	 */
	private static void drawEdges(GanttLayout self, GanttRowLayout rows, SvgWriter out) {
		List<GanttEdge> edges = self.getEdges();
		if (edges == null || edges.isEmpty()) {
			return;
		}

		// Build lookup map from item id to item.
		Map<String, GanttItem> itemById = new HashMap<>();
		for (GanttItem item : self.getItems()) {
			itemById.put(item.getId(), item);
		}

		// The horizontal segments of all edges are placed together, so that edges passing through
		// the same gap between rows get separate lanes.
		GanttEdgeRouting routing = new GanttEdgeRouting(rows);
		List<EdgeRoute> routes = new ArrayList<>();
		for (GanttEdge edge : edges) {
			GanttItem sourceItem = itemById.get(edge.getSourceItemId());
			GanttItem targetItem = itemById.get(edge.getTargetItemId());
			if (sourceItem == null || targetItem == null) {
				continue;
			}
			routes.add(EdgeRoute.create(edge, sourceItem, targetItem, rows, routing));
		}
		routing.route();

		out.beginGroup();
		out.writeCssClass("tl-gantt-edges");
		// A Gantt edge has no interactive part, clicks reach the items below it.
		Interactivity.writePassThrough(out);
		for (EdgeRoute route : routes) {
			route.draw(out);
		}
		out.endGroup();
	}

	/**
	 * The path and style of a dependency edge.
	 *
	 * @param sx
	 *        X coordinate of the source endpoint.
	 * @param sy
	 *        Y coordinate of the source endpoint.
	 * @param tx
	 *        X coordinate of the target endpoint.
	 * @param ty
	 *        Y coordinate of the target endpoint.
	 * @param sDir
	 *        Direction of the source stub, -1 to the left, 1 to the right.
	 * @param tDir
	 *        Direction of the target stub.
	 * @param sInset
	 *        Space taken by the source symbol at the start of the source stub.
	 * @param tInset
	 *        Space taken by the target symbol at the start of the target stub.
	 * @param horizontal
	 *        The horizontal segment between the stubs.
	 */
	record EdgeRoute(GanttEdge edge, double sx, double sy, double tx, double ty, double sDir, double tDir,
			double sInset, double tInset, String strokeColor, double strokeWidth, List<Double> dashes,
			GanttEdgeRouting.Segment horizontal) {

		/**
		 * Computes the route of the given edge and adds its horizontal segment to the given
		 * routing.
		 */
		static EdgeRoute create(GanttEdge edge, GanttItem sourceItem, GanttItem targetItem, GanttRowLayout rows,
				GanttEdgeRouting routing) {
			GanttEndpoint sourceEndpoint = edge.getSourceEndpoint();
			GanttEndpoint targetEndpoint = edge.getTargetEndpoint();

			double sx = endpointX(sourceItem, sourceEndpoint);
			double sy = centerY(sourceItem);
			double tx = endpointX(targetItem, targetEndpoint);
			double ty = centerY(targetItem);

			// Visual properties from the edge (application-defined).
			// Switch to violated style if constraint is violated and a violated style is defined.
			boolean hasViolatedStyle = edge.getViolatedStrokeColor() != null
				&& !edge.getViolatedStrokeColor().isEmpty();
			boolean violated = hasViolatedStyle
				&& isConstraintViolated(sourceItem, sourceEndpoint, targetItem, targetEndpoint);

			String strokeColor;
			double strokeWidth;
			List<Double> dashes;
			if (violated) {
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
			double symbolScale = strokeWidth / 2;
			double sInset = ConnectorSymbolRenderer.inset(edge.getSourceSymbol(), symbolScale);
			double tInset = ConnectorSymbolRenderer.inset(edge.getTargetSymbol(), symbolScale);
			double sDir = (sourceEndpoint == GanttEndpoint.START) ? -1 : 1;
			double tDir = (targetEndpoint == GanttEndpoint.START) ? -1 : 1;

			GanttEdgeRouting.Segment horizontal = routing.add(
				rows.indexOf(sourceItem.getRowId()), rows.indexOf(targetItem.getRowId()),
				sx + sDir * (EDGE_HORIZONTAL_STUB + sInset), tx + tDir * (EDGE_HORIZONTAL_STUB + tInset),
				sy, ty);
			return new EdgeRoute(edge, sx, sy, tx, ty, sDir, tDir, sInset, tInset, strokeColor, strokeWidth,
				dashes, horizontal);
		}

		/** Writes the edge, after its horizontal segment has been routed. */
		void draw(SvgWriter out) {
			double sStubX = sx + sDir * (EDGE_HORIZONTAL_STUB + sInset);
			double tStubX = tx + tDir * (EDGE_HORIZONTAL_STUB + tInset);
			double midY = horizontal.y();

			out.beginPath();
			out.setStroke(strokeColor);
			out.setStrokeWidth(strokeWidth);
			out.setFill("none");
			if (dashes != null && !dashes.isEmpty()) {
				out.setStrokeDasharray(DiagramUtil.doubleArray(dashes));
			}
			out.beginData();
			out.moveToAbs(sx + sDir * sInset, sy);
			out.lineToAbs(sStubX, sy);
			out.lineToAbs(sStubX, midY);
			out.lineToAbs(tStubX, midY);
			out.lineToAbs(tStubX, ty);
			out.lineToAbs(tx + tDir * tInset, ty);
			out.endData();
			out.endPath();

			// Symbols point from the stub into the item box. Each symbol is a path of its own
			// without dash array, so it is drawn solid also for a dashed edge.
			ConnectorSymbolRenderer.drawSymbol(out, sx, sy, -sDir, 0, edge.getSourceSymbol(), strokeColor,
				strokeWidth);
			ConnectorSymbolRenderer.drawSymbol(out, tx, ty, -tDir, 0, edge.getTargetSymbol(), strokeColor,
				strokeWidth);
		}
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

		GanttRowLayout rows = GanttRowLayout.of(self);

		List<DropArea> areas = new ArrayList<>();
		for (int i = 0, cnt = rows.size(); i < cnt; i++) {
			GanttRow row = rows.row(i);
			String rowId = row.getId();

			// 1. canMoveRow=false → only current row.
			if (!canMoveRow && !rowId.equals(currentRowId)) {
				continue;
			}

			// 2. Row.acceptsDrop=false → skip (e.g. axis rows).
			if (!row.isAcceptsDrop()) {
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
				.setY(rows.yStart(i))
				.setWidth(areaWidth)
				.setHeight(rows.height(i));
			areas.add(area);
		}
		return areas;
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
			GanttRowLayout rows = GanttRowLayout.of(self);
			double bestY = proposedY;
			double bestDist = Double.MAX_VALUE;
			for (DropArea area : areas) {
				// The padding of the row that starts at the top of the area.
				double areaRowPadding = rowPaddingAt(rows, area.getY());
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

	/**
	 * The padding of the row starting at the given Y coordinate, or a default for none.
	 */
	private static double rowPaddingAt(GanttRowLayout rows, double yStart) {
		for (int i = 0, cnt = rows.size(); i < cnt; i++) {
			if (Math.abs(rows.yStart(i) - yStart) < 0.5) {
				return rows.row(i).getRowPadding();
			}
		}
		return DEFAULT_ROW_PADDING;
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
			GanttRowLayout rows = GanttRowLayout.of(self);
			String targetRowId = item.getRowId(); // default to current
			for (int i = 0, cnt = rows.size(); i < cnt; i++) {
				double yStart = rows.yStart(i);
				double rowContentY = yStart + rows.row(i).getRowPadding();
				// Match the content top of the row, or any position within the row bounds.
				if (Math.abs(finalY - rowContentY) < 0.5 || (finalY >= yStart && finalY < rows.yEnd(i))) {
					targetRowId = rows.row(i).getId();
					break;
				}
			}
			if (!targetRowId.equals(item.getRowId())) {
				item.setRowId(targetRowId);
				// The rows are placed anew by the next layout pass.
				self.setRowLayout(null);
			}
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
	 * Moves the scrolling groups, the content of the {@link Sticky} boxes and the scrollbar thumbs
	 * of the viewport to the current scroll offsets, without drawing the chart again.
	 */
	private static void applyScroll(GanttLayout self, PanRenderer renderer) {
		double scrollXPx = self.getScrollX() * self.getZoom();
		double scrollY = self.getScrollY();

		renderer.setTranslate(self, SCROLL_DECORATIONS_PURPOSE, -scrollXPx, 0);
		renderer.setTranslate(self, SCROLL_DECORATION_LABELS_PURPOSE, -scrollXPx, 0);
		renderer.setTranslate(self, SCROLL_CONTENT_PURPOSE, -scrollXPx, -scrollY);
		renderer.setTranslate(self, SCROLL_SIDEBAR_PURPOSE, 0, -scrollY);
		renderer.setTranslate(self, SCROLL_HEADER_PURPOSE, -scrollXPx, 0);
		double visibleLeft = visibleLeft(self);
		forEachSticky(self, sticky -> {
			double shift = sticky.stickTo(visibleLeft);
			renderer.setTranslate(sticky, StickyOperations.CONTENT_PURPOSE, shift, 0);
		});

		GanttScrollGeometry geometry = GanttScrollGeometry.of(self);
		if (geometry.vertical()) {
			renderer.setTranslate(self, VERTICAL_THUMB_PURPOSE, 0, geometry.verticalThumbOffset());
		}
		if (geometry.horizontal()) {
			renderer.setTranslate(self, HORIZONTAL_THUMB_PURPOSE, geometry.horizontalThumbOffset(), 0);
		}
	}

	/**
	 * Updates the {@link Sticky#getShift() shift} of all {@link Sticky} boxes within the boxes of
	 * the items to the current horizontal scroll offset, so that their content starts at the left
	 * edge of the visible time range as far as it stays within the box.
	 */
	private static void stickToViewport(GanttLayout self) {
		double visibleLeft = visibleLeft(self);
		forEachSticky(self, sticky -> sticky.stickTo(visibleLeft));
	}

	/**
	 * The X coordinate of the left edge of the visible time range, in the coordinates of the
	 * scrolled content.
	 */
	private static double visibleLeft(GanttLayout self) {
		return self.getX() + self.getColumnWidth() + self.getScrollX() * self.getZoom();
	}

	/**
	 * Calls the given action for each {@link Sticky} box within the boxes of the items, the boxes
	 * scrolling horizontally in the header and data rows.
	 */
	private static void forEachSticky(GanttLayout self, Consumer<Sticky> action) {
		for (GanttItem item : self.getItems()) {
			WidgetTraversal.visitContents(item.getBox(), widget -> {
				if (widget instanceof Sticky sticky) {
					action.accept(sticky);
				}
			});
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttMilestone;
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
			double y = offsetY + axisHeight + idx * rowHeight + 2;
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
				box.setHeight(rowHeight - 4);
			} else if (item instanceof GanttMilestone ms) {
				double r = (rowHeight - 4) / 2.0;
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
		out.writeCssClass("gantt-axis");

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
		// Task 12.
	}

	private static void drawDecorations(GanttLayout self, SvgWriter out) {
		// Task 14.
	}

	private static void drawEdges(GanttLayout self, SvgWriter out) {
		// Task 13.
	}

	private static void indexRows(GanttRow row, Map<String, Integer> idx, int[] counter) {
		idx.put(row.getId(), counter[0]);
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			indexRows(child, idx, counter);
		}
	}
}

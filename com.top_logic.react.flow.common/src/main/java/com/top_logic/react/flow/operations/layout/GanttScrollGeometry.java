/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import static com.top_logic.react.flow.operations.layout.GanttLayoutOperations.*;

import com.top_logic.react.flow.data.GanttLayout;

/**
 * Geometry of the internal viewport of a laid out {@link GanttLayout}: which scrollbars it
 * shows, the visible part of the scrolling data area, the scroll ranges and the scrollbar
 * thumbs.
 */
final class GanttScrollGeometry {

	private final GanttLayout _gantt;

	private final boolean _vertical;

	private final boolean _horizontal;

	/**
	 * Creates a {@link GanttScrollGeometry}.
	 *
	 * @param gantt
	 *        The laid out chart.
	 * @param vertical
	 *        Whether a vertical scrollbar is shown (the data rows are higher than the viewport).
	 * @param horizontal
	 *        Whether a horizontal scrollbar is shown (the time axis is wider than the viewport).
	 */
	private GanttScrollGeometry(GanttLayout gantt, boolean vertical, boolean horizontal) {
		_gantt = gantt;
		_vertical = vertical;
		_horizontal = horizontal;
	}

	/** Whether a vertical scrollbar is shown. */
	boolean vertical() {
		return _vertical;
	}

	/** Whether a horizontal scrollbar is shown. */
	boolean horizontal() {
		return _horizontal;
	}

	/**
	 * Computes the geometry from the box size and the content size of the given chart.
	 */
	static GanttScrollGeometry of(GanttLayout chart) {
		double contentW =
			chart.getColumnWidth() + (chart.getRangeMax() - chart.getRangeMin()) * chart.getZoom();
		double contentH = chart.getHeaderHeight() + chart.getDataHeight();
		boolean[] bars = scrollbars(contentW, contentH, chart.getWidth(), chart.getHeight());
		return new GanttScrollGeometry(chart, bars[0], bars[1]);
	}

	/**
	 * Which scrollbars content of the given size needs in a box of the given size.
	 *
	 * <p>
	 * A scrollbar takes space from the other direction, so a scrollbar in one direction may
	 * require a scrollbar in the other.
	 * </p>
	 *
	 * @return {@code {vertical, horizontal}}
	 */
	static boolean[] scrollbars(double contentW, double contentH, double boxW, double boxH) {
		boolean verticalOverflow = contentH > boxH + VIEWPORT_TOLERANCE;
		boolean horizontal =
			contentW > boxW - (verticalOverflow ? SCROLLBAR_SIZE : 0) + VIEWPORT_TOLERANCE;
		boolean vertical =
			verticalOverflow || (horizontal && contentH > boxH - SCROLLBAR_SIZE + VIEWPORT_TOLERANCE);
		return new boolean[] { vertical, horizontal };
	}

	/** Whether the content is shown in a clipped, scrollable viewport. */
	boolean active() {
		return _vertical || _horizontal;
	}

	/** Left edge of the visible part of the time axis, in SVG coordinates. */
	double viewX() {
		return _gantt.getX() + _gantt.getColumnWidth();
	}

	/** Top edge of the visible part of the data rows, in SVG coordinates. */
	double viewY() {
		return _gantt.getY() + _gantt.getHeaderHeight();
	}

	/** Width of the visible part of the time axis, in pixels. */
	double viewWidth() {
		return Math.max(0,
			_gantt.getWidth() - _gantt.getColumnWidth() - (_vertical ? SCROLLBAR_SIZE : 0));
	}

	/** Height of the visible part of the data rows, in pixels. */
	double viewHeight() {
		return Math.max(0,
			_gantt.getHeight() - _gantt.getHeaderHeight() - (_horizontal ? SCROLLBAR_SIZE : 0));
	}

	/** The largest {@link GanttLayout#getScrollX()} at the given zoom, in position units. */
	double maxScrollX(double zoom) {
		if (zoom <= 0) {
			return 0;
		}
		double range = _gantt.getRangeMax() - _gantt.getRangeMin();
		return Math.max(0, range - viewWidth() / zoom);
	}

	/** The largest {@link GanttLayout#getScrollY()}, in pixels. */
	double maxScrollY() {
		return Math.max(0, _gantt.getDataHeight() - viewHeight());
	}

	/** Length of the thumb of the vertical scrollbar, in pixels. */
	double verticalThumbLength() {
		return thumbLength(viewHeight(), _gantt.getDataHeight());
	}

	/** Offset of the thumb of the vertical scrollbar from the start of its track. */
	double verticalThumbOffset() {
		return thumbTravel(viewHeight(), verticalThumbLength()) * fraction(_gantt.getScrollY(), maxScrollY());
	}

	/** Length of the thumb of the horizontal scrollbar, in pixels. */
	double horizontalThumbLength() {
		return thumbLength(viewWidth(), (_gantt.getRangeMax() - _gantt.getRangeMin()) * _gantt.getZoom());
	}

	/** Offset of the thumb of the horizontal scrollbar from the start of its track. */
	double horizontalThumbOffset() {
		double zoom = _gantt.getZoom();
		return thumbTravel(viewWidth(), horizontalThumbLength())
			* fraction(_gantt.getScrollX(), maxScrollX(zoom));
	}

	/**
	 * The change of {@link GanttLayout#getScrollY()} for moving the vertical thumb by one
	 * pixel.
	 */
	double verticalScrollPerThumbPixel() {
		return perPixel(maxScrollY(), thumbTravel(viewHeight(), verticalThumbLength()));
	}

	/**
	 * The change of {@link GanttLayout#getScrollX()} for moving the horizontal thumb by one
	 * pixel.
	 */
	double horizontalScrollPerThumbPixel() {
		return perPixel(maxScrollX(_gantt.getZoom()), thumbTravel(viewWidth(), horizontalThumbLength()));
	}

	/** Left edge of the vertical scrollbar, in SVG coordinates. */
	double verticalTrackX() {
		return _gantt.getX() + _gantt.getWidth() - SCROLLBAR_SIZE;
	}

	/** Top edge of the horizontal scrollbar, in SVG coordinates. */
	double horizontalTrackY() {
		return _gantt.getY() + _gantt.getHeight() - SCROLLBAR_SIZE;
	}

	/** Whether the given point lies on the track of the vertical scrollbar. */
	boolean inVerticalTrack(double x, double y) {
		return _vertical && x >= verticalTrackX() && x <= verticalTrackX() + SCROLLBAR_SIZE
			&& y >= viewY() && y <= viewY() + viewHeight();
	}

	/** Whether the given point lies on the thumb of the vertical scrollbar. */
	boolean inVerticalThumb(double x, double y) {
		double thumbY = viewY() + verticalThumbOffset();
		return inVerticalTrack(x, y) && y >= thumbY && y <= thumbY + verticalThumbLength();
	}

	/** Whether the given point lies on the track of the horizontal scrollbar. */
	boolean inHorizontalTrack(double x, double y) {
		return _horizontal && y >= horizontalTrackY() && y <= horizontalTrackY() + SCROLLBAR_SIZE
			&& x >= viewX() && x <= viewX() + viewWidth();
	}

	/** Whether the given point lies on the thumb of the horizontal scrollbar. */
	boolean inHorizontalThumb(double x, double y) {
		double thumbX = viewX() + horizontalThumbOffset();
		return inHorizontalTrack(x, y) && x >= thumbX && x <= thumbX + horizontalThumbLength();
	}

	private static double thumbLength(double track, double total) {
		if (total <= 0) {
			return track;
		}
		return Math.min(track, Math.max(MIN_THUMB_LENGTH, track * Math.min(1, track / total)));
	}

	private static double thumbTravel(double track, double thumbLength) {
		return Math.max(0, track - thumbLength);
	}

	private static double fraction(double scroll, double maxScroll) {
		return maxScroll > 0 ? Math.max(0, Math.min(1, scroll / maxScroll)) : 0;
	}

	private static double perPixel(double maxScroll, double travel) {
		return travel > 0 ? maxScroll / travel : 0;
	}
}

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

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.operations.layout.GanttLayoutOperations.RowGeometry;

/**
 * The rows of a {@link GanttLayout} in display order, with their nesting depth and (after a
 * layout pass) their vertical geometry.
 *
 * <p>
 * The row forest is traversed once when the index is {@link #index(GanttLayout) built}. A layout
 * pass {@link #place(double, double[]) places} the rows and stores the result in
 * {@link GanttLayout#getRowLayout()}, so that drawing and dragging use the rows and their
 * geometry of the last layout pass without traversing the forest again.
 * </p>
 */
public final class GanttRowLayout {

	private final List<GanttRow> _rows;

	private final int[] _depths;

	private final Map<String, Integer> _indexById;

	private final int _frozenCount;

	private double[] _yStart;

	private double[] _yEnd;

	private GanttRowLayout(List<GanttRow> rows, int[] depths, Map<String, Integer> indexById, int frozenCount) {
		_rows = rows;
		_depths = depths;
		_indexById = indexById;
		_frozenCount = frozenCount;
	}

	/**
	 * Builds the index of the rows of the given chart in display order (depth first), without
	 * geometry.
	 */
	public static GanttRowLayout index(GanttLayout chart) {
		List<GanttRow> rows = new ArrayList<>();
		List<Integer> depths = new ArrayList<>();
		Map<String, Integer> indexById = new HashMap<>();
		List<GanttRow> roots = chart.getRootRows();
		int frozenRoots = Math.min(Math.max(0, chart.getFrozenRows()), roots.size());
		int frozenCount = 0;
		for (int n = 0; n < roots.size(); n++) {
			collect(roots.get(n), 0, rows, depths, indexById);
			if (n + 1 == frozenRoots) {
				frozenCount = rows.size();
			}
		}
		int[] depthArray = new int[depths.size()];
		for (int n = 0; n < depthArray.length; n++) {
			depthArray[n] = depths.get(n).intValue();
		}
		return new GanttRowLayout(rows, depthArray, indexById, frozenCount);
	}

	private static void collect(GanttRow row, int depth, List<GanttRow> rows, List<Integer> depths,
			Map<String, Integer> indexById) {
		indexById.put(row.getId(), Integer.valueOf(rows.size()));
		rows.add(row);
		depths.add(Integer.valueOf(depth));
		for (GanttRow child : row.getChildren()) {
			collect(child, depth + 1, rows, depths, indexById);
		}
	}

	/**
	 * The rows of the chart as placed by its last layout pass.
	 *
	 * <p>
	 * A chart that has not been laid out yet (or whose rows changed since, see
	 * {@link GanttLayoutOperations#commitDrag(Box, double, double, double, double)}) gets rows
	 * placed from the current heights of the item boxes.
	 * </p>
	 */
	public static GanttRowLayout of(GanttLayout chart) {
		GanttRowLayout result = chart.getRowLayout();
		if (result == null) {
			result = index(chart);
			result.placeByItemHeights(chart, chart.getY());
			chart.setRowLayout(result);
		}
		return result;
	}

	private void placeByItemHeights(GanttLayout chart, double top) {
		int size = size();
		double[] contentHeights = new double[size];
		for (int n = 0; n < size; n++) {
			contentHeights[n] = _rows.get(n).getMinContentHeight();
		}
		for (GanttItem item : chart.getItems()) {
			int idx = indexOf(item.getRowId());
			Box box = item.getBox();
			if (idx < 0 || box == null) {
				continue;
			}
			contentHeights[idx] = Math.max(contentHeights[idx], box.getHeight());
		}
		double[] heights = new double[size];
		for (int n = 0; n < size; n++) {
			heights[n] = contentHeights[n] + 2 * _rows.get(n).getRowPadding();
		}
		place(top, heights);
	}

	/**
	 * Places the rows one below the other.
	 *
	 * @param top
	 *        The Y coordinate of the top of the first row.
	 * @param heights
	 *        The total height (including padding) of each row, in display order.
	 */
	public void place(double top, double[] heights) {
		int size = size();
		_yStart = new double[size];
		_yEnd = new double[size];
		double y = top;
		for (int n = 0; n < size; n++) {
			_yStart[n] = y;
			y += heights[n];
			_yEnd[n] = y;
		}
	}

	/** The number of rows. */
	public int size() {
		return _rows.size();
	}

	/** The rows in display order. */
	public List<GanttRow> rows() {
		return _rows;
	}

	/** The row at the given display index. */
	public GanttRow row(int index) {
		return _rows.get(index);
	}

	/** The row with the given ID, or {@code null} if there is none. */
	public GanttRow row(String rowId) {
		int index = indexOf(rowId);
		return index < 0 ? null : _rows.get(index);
	}

	/** The display index of the row with the given ID, or -1 if there is none. */
	public int indexOf(String rowId) {
		Integer index = _indexById.get(rowId);
		return index == null ? -1 : index.intValue();
	}

	/** Whether a row with the given ID exists. */
	public boolean contains(String rowId) {
		return _indexById.containsKey(rowId);
	}

	/** The nesting depth of the row at the given display index, 0 for a root row. */
	public int depth(int index) {
		return _depths[index];
	}

	/**
	 * The number of rows (including descendants) of the {@link GanttLayout#getFrozenRows() frozen}
	 * root rows. These are the first rows in display order.
	 */
	public int frozenCount() {
		return _frozenCount;
	}

	/** Whether the row at the given display index is a frozen header row. */
	public boolean isFrozen(int index) {
		return index < _frozenCount;
	}

	/** Whether the row with the given ID is a frozen header row. */
	public boolean isFrozen(String rowId) {
		int index = indexOf(rowId);
		return index >= 0 && isFrozen(index);
	}

	/** The Y coordinate where the row at the given display index starts. */
	public double yStart(int index) {
		return _yStart[index];
	}

	/** The Y coordinate where the row at the given display index ends. */
	public double yEnd(int index) {
		return _yEnd[index];
	}

	/** The total height (including padding) of the row at the given display index. */
	public double height(int index) {
		return _yEnd[index] - _yStart[index];
	}

	/** The geometry of the row at the given display index. */
	public RowGeometry geometry(int index) {
		return new RowGeometry(_yStart[index], _yEnd[index]);
	}

	/** The geometry of the row with the given ID, or {@code null} if there is none. */
	public RowGeometry geometry(String rowId) {
		int index = indexOf(rowId);
		return index < 0 ? null : geometry(index);
	}

	/**
	 * The Y coordinate of the bottom of the last row, or the given default if there are no rows.
	 */
	public double bottom(double emptyBottom) {
		int size = size();
		return size == 0 ? emptyBottom : _yEnd[size - 1];
	}

	/**
	 * The upper boundary of the gap between the row at the given index and the row below it: the
	 * bottom of the content area of the row, above its bottom padding.
	 */
	public double gapTop(int index) {
		return _yEnd[index] - _rows.get(index).getRowPadding();
	}

	/**
	 * The lower boundary of the gap between the row at the given index and the row below it: the
	 * top of the content area of the row below, below its top padding.
	 */
	public double gapBottom(int index) {
		return _yStart[index + 1] + _rows.get(index + 1).getRowPadding();
	}

}

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
 * Only the visible rows are shown: the descendants of a {@link GanttRow#isCollapsed() collapsed}
 * row are {@link #hiddenRows() hidden} and take no place in the display order, so that the rows
 * below move up. The items of a hidden row are neither placed nor drawn, see
 * {@link #isHidden(String)}.
 * </p>
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

	private final List<GanttRow> _hiddenRows;

	private final int[] _hiddenDepths;

	private final Map<String, Integer> _hiddenIndexById;

	private final boolean _hierarchical;

	private double[] _yStart;

	private double[] _yEnd;

	private GanttRowLayout(Rows visible, Rows hidden, int frozenCount, boolean hierarchical) {
		_rows = visible._rows;
		_depths = visible.depths();
		_indexById = visible._indexById;
		_hiddenRows = hidden._rows;
		_hiddenDepths = hidden.depths();
		_hiddenIndexById = hidden._indexById;
		_frozenCount = frozenCount;
		_hierarchical = hierarchical;
	}

	/**
	 * Builds the index of the rows of the given chart in display order (depth first), without
	 * geometry.
	 *
	 * <p>
	 * The descendants of a {@link GanttRow#isCollapsed() collapsed} row are not part of the display
	 * order but {@link #hiddenRows() hidden}.
	 * </p>
	 */
	public static GanttRowLayout index(GanttLayout chart) {
		Rows visible = new Rows();
		Rows hidden = new Rows();
		List<GanttRow> roots = chart.getRootRows();
		int frozenRoots = Math.min(Math.max(0, chart.getFrozenRows()), roots.size());
		int frozenCount = 0;
		boolean hierarchical = false;
		for (int n = 0; n < roots.size(); n++) {
			hierarchical |= collect(roots.get(n), 0, visible, hidden);
			if (n + 1 == frozenRoots) {
				frozenCount = visible.size();
			}
		}
		return new GanttRowLayout(visible, hidden, frozenCount, hierarchical);
	}

	/**
	 * Adds the given row and its descendants to the given rows.
	 *
	 * @param target
	 *        The rows to add the given row to.
	 * @param hidden
	 *        The rows to add the descendants of a collapsed row to.
	 * @return Whether the given row or one of its descendants has children.
	 */
	private static boolean collect(GanttRow row, int depth, Rows target, Rows hidden) {
		target.add(row, depth);
		List<GanttRow> children = row.getChildren();
		Rows childTarget = row.isCollapsed() ? hidden : target;
		boolean hierarchical = !children.isEmpty();
		for (GanttRow child : children) {
			hierarchical |= collect(child, depth + 1, childTarget, hidden);
		}
		return hierarchical;
	}

	/**
	 * Rows in traversal order with their depths, indexed by ID.
	 */
	private static final class Rows {

		final List<GanttRow> _rows = new ArrayList<>();

		final List<Integer> _depths = new ArrayList<>();

		final Map<String, Integer> _indexById = new HashMap<>();

		void add(GanttRow row, int depth) {
			_indexById.put(row.getId(), Integer.valueOf(_rows.size()));
			_rows.add(row);
			_depths.add(Integer.valueOf(depth));
		}

		int size() {
			return _rows.size();
		}

		int[] depths() {
			int[] result = new int[_depths.size()];
			for (int n = 0; n < result.length; n++) {
				result[n] = _depths.get(n).intValue();
			}
			return result;
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

	/** Whether a visible row with the given ID exists. */
	public boolean contains(String rowId) {
		return _indexById.containsKey(rowId);
	}

	/**
	 * The rows hidden below a {@link GanttRow#isCollapsed() collapsed} row, in traversal order.
	 *
	 * <p>
	 * They are not part of the display order: they have no index and no geometry.
	 * </p>
	 */
	public List<GanttRow> hiddenRows() {
		return _hiddenRows;
	}

	/** The nesting depth of the {@link #hiddenRows() hidden row} at the given index. */
	public int hiddenDepth(int index) {
		return _hiddenDepths[index];
	}

	/**
	 * Whether the row with the given ID is {@link #hiddenRows() hidden} below a collapsed row.
	 *
	 * <p>
	 * The items of a hidden row and the edges of these items are not drawn.
	 * </p>
	 */
	public boolean isHidden(String rowId) {
		return _hiddenIndexById.containsKey(rowId);
	}

	/**
	 * Whether a row of the chart (visible or {@link #hiddenRows() hidden}) has children.
	 *
	 * <p>
	 * The label column of a hierarchical chart reserves space for the collapse toggles, see
	 * {@link #labelIndent(int)}.
	 * </p>
	 */
	public boolean isHierarchical() {
		return _hierarchical;
	}

	/**
	 * The number of indentation steps before the label of a row at the given depth.
	 *
	 * <p>
	 * In a {@link #isHierarchical() hierarchical} chart, the first step of a row holds the collapse
	 * toggle of a row with children, so that the labels of rows at the same depth line up.
	 * </p>
	 */
	public int labelIndent(int depth) {
		return _hierarchical ? depth + 1 : depth;
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

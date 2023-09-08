/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

/**
 * Value holder class for the current scroll position (row index, column index) of a table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class IndexViewportState {
	
	/**
	 * Static instance of a {@link IndexViewportState}, that does not contain any client side data.
	 */
	public static final IndexViewportState NONE = new IndexViewportState();

	private int _columnAnchor;
	private int _columnOffset;
	private int _rowAnchor;
	private int _rowOffset;
	
	/**
	 * Create a new {@link IndexViewportState}.
	 */
	public IndexViewportState() {
		_columnAnchor = -1;
		_columnOffset = -1;
		_rowAnchor = -1;
		_rowOffset = -1;
	}

	/**
	 * index of the most left visible column.
	 */
	public int getColumnAnchor() {
		return _columnAnchor;
	}

	/**
	 * @see #getColumnAnchor()
	 */
	public void setColumnAnchor(int columnAnchor) {
		_columnAnchor = columnAnchor;
	}

	/**
	 * offset in pixel of the left border of {@link #getColumnAnchor()} to viewport.
	 */
	public int getColumnOffset() {
		return _columnOffset;
	}

	/**
	 * @see #getColumnOffset()
	 */
	public void setColumnOffset(int columnOffset) {
		_columnOffset = columnOffset;
	}

	/**
	 * index of the most top visible row.
	 */
	public int getRowAnchor() {
		return _rowAnchor;
	}

	/**
	 * @see #getRowAnchor()
	 */
	public void setRowAnchor(int rowAnchor) {
		_rowAnchor = rowAnchor;
	}

	/**
	 * offset in pixel of the top border of {@link #getRowAnchor()} to viewport.
	 */
	public int getRowOffset() {
		return _rowOffset;
	}

	/**
	 * @see #getRowOffset()
	 */
	public void setRowOffset(int rowOffset) {
		_rowOffset = rowOffset;
	}

}

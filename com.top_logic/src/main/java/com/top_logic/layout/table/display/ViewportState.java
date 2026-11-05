/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;


/**
 * Definition of anchors, which describe the scrolling offset of the client side's viewport.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ViewportState {

	private RowIndexAnchor rowAnchor;

	private ColumnAnchor columnAnchor;

	private ColumnAnchor lastClickedColumn;

	private RowIndexAnchor lastClickedRow;

	/**
	 * Create a new {@link ViewportState}.
	 */
	public ViewportState() {
		reset();
	}

	/** The row at which the viewport is anchored. */
	public RowIndexAnchor getRowAnchor() {
		return rowAnchor;
	}

	/** Setter for {@link #getRowAnchor()}. */
	public void setRowAnchor(RowIndexAnchor rowAnchor) {
		this.rowAnchor = rowAnchor;
	}

	/** The column at which the viewport is anchored. */
	public ColumnAnchor getColumnAnchor() {
		return columnAnchor;
	}

	/** Setter for {@link #getColumnAnchor()}. */
	public void setColumnAnchor(ColumnAnchor columnAnchor) {
		this.columnAnchor = columnAnchor;
	}

	/** The column the user clicked last. */
	public ColumnAnchor getLastClickedColumn() {
		return lastClickedColumn;
	}

	/** Setter for {@link #getLastClickedColumn()}. */
	public void setLastClickedColumn(ColumnAnchor lastClickedColumn) {
		this.lastClickedColumn = lastClickedColumn;
	}

	/** The row the user clicked last. */
	public RowIndexAnchor getLastClickedRow() {
		return lastClickedRow;
	}

	/** Setter for {@link #getLastClickedRow()}. */
	public void setLastClickedRow(RowIndexAnchor lastClickedRow) {
		this.lastClickedRow = lastClickedRow;
	}

	/**
	 * Removes any stored information about client's viewport state. Thereafter it looks like the
	 * table has been rendered to GUI initially, without any user modifications (e.g. used when
	 * another table page shall be shown, so that any changed scroll position, etc., becomes
	 * invalid).
	 */
	public void reset() {
		rowAnchor = lastClickedRow = RowIndexAnchor.NONE;
		columnAnchor = lastClickedColumn = ColumnAnchor.NONE;
	}
}

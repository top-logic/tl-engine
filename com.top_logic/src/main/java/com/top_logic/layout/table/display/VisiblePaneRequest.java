/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

import com.top_logic.layout.table.TableViewModel;

/**
 * Definition of the rows and columns, which will be displayed at least at client side's viewport
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class VisiblePaneRequest {

	private IndexRange persistentRows;
	private IndexRange transientRows;
	private IndexRange persistentColumns;
	private IndexRange transientColumns;

	/**
	 * Create a new {@link VisiblePaneRequest}, whereby given {@link IndexRange}s are treated as
	 * transient, which means, that they will be cleared after request processing.
	 */
	public VisiblePaneRequest(IndexRange rows, IndexRange columns) {
		this.transientRows = rows;
		this.transientColumns = columns;
		persistentRows = persistentColumns = IndexRange.undefined();
	}

	/**
	 * {@link IndexRange} of requested rows. If an transient row range has been stored
	 *         ({@link #setRowRange(IndexRange)}), that one will be returned. Otherwise any
	 *         persistent row range will be returned ({@link #setPersistentRowRange(IndexRange)}).
	 */
	public IndexRange getRowRange() {
		return getRange(transientRows, persistentRows);
	}

	/**
	 * Setter of an {@link IndexRange} for rows, that shall be visible the very next time after the
	 * client request has been processed. Thereafter this row range request will be removed.
	 */
	public void setRowRange(IndexRange rows) {
		this.transientRows = rows;
	}
	
	/**
	 * Setter of an {@link IndexRange} for rows, that shall be visible the very next time after the
	 * client request has been processed. The requested row range will be stored for the lifetime of
	 * the {@link TableViewModel}, until another invocation of this method. Any requested transient
	 * row range will be removed.
	 */
	public void setPersistentRowRange(IndexRange rows) {
		this.persistentRows = rows;
		transientRows = IndexRange.undefined();
	}

	/**
	 * {@link IndexRange} of requested columns. If an transient column range has been stored
	 *         ({@link #setColumnRange(IndexRange)}), that one will be returned. Otherwise any
	 *         persistent column range will be returned
	 *         ({@link #setPersistentColumnRange(IndexRange)}).
	 */
	public IndexRange getColumnRange() {
		return getRange(transientColumns, persistentColumns);
	}

	/**
	 * Setter of an {@link IndexRange} for columns, that shall be visible the very next time after
	 * the client request has been processed. Thereafter this column range request will be removed.
	 */
	public void setColumnRange(IndexRange columns) {
		this.transientColumns = columns;
	}

	/**
	 * Setter of an {@link IndexRange} for columns, that shall be visible the very next time after
	 * the client request has been processed. The requested column range will be stored for the
	 * lifetime of the {@link TableViewModel}, until another invocation of this method. Any
	 * requested transient column range will be removed.
	 */
	public void setPersistentColumnRange(IndexRange columns) {
		this.persistentColumns = columns;
		transientColumns = IndexRange.undefined();
	}

	/**
	 * Clears all requested transient row ranges and column ranges.
	 */
	public void clearTransientRanges() {
		transientRows = transientColumns = IndexRange.undefined();
	}

	private IndexRange getRange(IndexRange transientRange, IndexRange persistentRange) {
		if (transientRange != IndexRange.undefined()) {
			return transientRange;
		} else {
			return persistentRange;
		}
	}
}

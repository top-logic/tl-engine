/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * Used for table rows, which need an index within the set of all table rows and besides an index
 * within the set of displayed table rows.
 * 
 * <p>
 * {@link RowIndex} are compared by their {@link #getDisplayedIndex() display index}.
 * </p>
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
class RowIndex<T> implements Comparable<RowIndex<T>> {
	
	private static final RowIndex<?>[] NO_ROWS = new RowIndex[0];

	private RowIndex<T> _next = none();

	private RowIndex<T> _previous = none();

	private int _displayedIndex;
	
	private final T _rowObject;

	public RowIndex(T rowObject, int displayedIndex) {
		_displayedIndex = displayedIndex;
		_rowObject = rowObject;
	}

	public int getDisplayedIndex() {
		return _displayedIndex;
	}
	
	public void setDisplayedIndex(int displayedIndex) {
		this._displayedIndex = displayedIndex;
	}

	public T getRowObject() {
		return _rowObject;
	}

	public RowIndex<T> next() {
		return _next;
	}

	public void setNext(RowIndex<T> next) {
		_next = next;
	}

	public RowIndex<T> previous() {
		return _previous;
	}

	public void setPrevious(RowIndex<T> previous) {
		_previous = previous;
	}

	static <T> RowIndex<T> none() {
		return null;
	}
	
	static <T> RowIndex<T>[] noRows(){
		@SuppressWarnings("unchecked")
		RowIndex<T>[] noRows = (RowIndex<T>[]) NO_ROWS;
		return noRows;
	}

	@Override
	public String toString() {
		String next = next() != none() ? next().getRowObject().toString() : "none";
		return "(row object:" + _rowObject + ",displayIndex:" + _displayedIndex + ",next:" + next + ")";
	}

	@Override
	public int compareTo(RowIndex<T> o) {
		if (o == this) {
			return 0;
		}
		if (getDisplayedIndex() < o.getDisplayedIndex()) {
			return -1;
		}
		if (getDisplayedIndex() > o.getDisplayedIndex()) {
			return 1;
		}
		return 0;
	}
}

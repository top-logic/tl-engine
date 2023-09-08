/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.CloseableIteratorBase;

/**
 * {@link CloseableIterator} that bases on an {@link ResultSet} and closes it during
 * {@link #close()}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ResultSetBasedIterator<T> extends CloseableIteratorBase<T> {

	private ResultSet _resultSet;

	/**
	 * Creates a new {@link ResultSetBasedIterator}.
	 * 
	 * @param result
	 *        The result set to be as base for elements.
	 */
	public ResultSetBasedIterator(ResultSet result) {
		_resultSet = result;
	}

	/**
	 * The base {@link ResultSet} to read from.
	 */
	protected final ResultSet resultSet() {
		return _resultSet;
	}

	@Override
	protected final boolean findNext() {
		return findNext(_resultSet);
	}

	/**
	 * Computes the next element for the iterator.
	 * 
	 * <p>
	 * The next element must be set using {@link #setNext(Object)};
	 * </p>
	 * 
	 * @param resultSet
	 *        {@link ResultSet} this iterator based on. The cursor in the {@link ResultSet} is
	 *        untouched by super class, i.e. the implementation must call {@link ResultSet#next()}
	 *        to get next row.
	 * 
	 * @return <code>true</code> iff there is a next element.
	 * 
	 * @see #setNext(Object)
	 */
	protected abstract boolean findNext(ResultSet resultSet);

	@Override
	protected void internalClose() {
		ResultSet resultSet = _resultSet;
		_resultSet = null;
		resetNext();
		try {
			resultSet.close();
		} catch (SQLException ex) {
			Logger.warn("Unable to close result set.", ex, ResultSetBasedIterator.class);
		}
	}
}


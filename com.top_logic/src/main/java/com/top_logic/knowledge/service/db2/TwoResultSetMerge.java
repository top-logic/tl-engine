/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

/**
 * Merges two result sets using a given comparator.
 * 
 * <p>
 * This implementation contains two result sets and merges them by returning the one which is
 * smaller with respect to a given comparator.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class TwoResultSetMerge extends ResultSetMerger {

	/**
	 * If initialised, this pointer points to the {@link ResultSet} that is currently less than the
	 * other.
	 */
	private ResultSet _result1;

	/**
	 * Contains the larger {@link ResultSet} or <code>null</code> if one result set was processed
	 * completely.
	 */
	private ResultSet _result2;

	private boolean _initialized = false;

	private final Comparator<? super ResultSet> _comparator;

	/**
	 * Creates a new {@link TwoResultSetMerge}.
	 */
	TwoResultSetMerge(ResultSet result1, ResultSet result2, Comparator<? super ResultSet> comparator) {
		_result1 = result1;
		_result2 = result2;
		_comparator = comparator;
	}

	@Override
	public ResultSet impl() {
		return _result1;
	}

	@Override
	public boolean next() throws SQLException {
		if (!_initialized) {
			_initialized = true;
			boolean next1 = _result1.next();
			boolean next2 = _result2.next();
			if (next2) {
				handleResult2HasNext(next1);
			} else {
				handleResult2Processed();
			}
			return next1 || next2;
		} else {
			if (_result2 != null) {
				handleResult2HasNext(_result1.next());
				return true;
			} else {
				return _result1.next();
			}
		}
	}

	private void handleResult2HasNext(boolean result1HasNext) throws SQLException {
		// result2 has next.
		if (result1HasNext) {
			// both are not processed complete
			ensureResult1IsLess();
		} else {
			handleResult1Processed();
		}
	}

	private void handleResult2Processed() throws SQLException {
		_result2.close();
		_result2 = null;
	}

	private void handleResult1Processed() throws SQLException {
		_result1.close();
		_result1 = _result2;
		_result2 = null;
	}

	private void ensureResult1IsLess() {
		if (_comparator.compare(_result2, _result1) < 0) {
			// ensure first result is less the second.
			flip();
		}
	}

	private void flip() {
		ResultSet tmp = _result1;
		_result1 = _result2;
		_result2 = tmp;
	}

	@Override
	public void close() throws SQLException {
		try {
			closeResult(_result1);
		} finally {
			closeResult(_result2);
		}

	}

	private void closeResult(ResultSet result) throws SQLException {
		if (result != null) {
			result.close();
		}
	}

}


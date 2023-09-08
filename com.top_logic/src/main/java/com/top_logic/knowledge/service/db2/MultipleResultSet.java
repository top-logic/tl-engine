/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * {@link ResultSet} merging a given sequence of {@link ResultSet} by a given {@link Comparator}.
 * 
 * <p>
 * This implementation contains a {@link PriorityQueue} containing all {@link ResultSet results}
 * such that the head of the queue is the {@link ResultSet} which has currently the least value.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultipleResultSet extends ResultSetMerger {

	/**
	 * {@link ResultSet}s to process.
	 * 
	 * <p>
	 * If a {@link ResultSet} is completely processed, it is closed and replaced by
	 * <code>null</code>.
	 * </p>
	 */
	private final ResultSet[] _results;

	private final Comparator<? super ResultSet> _comparator;

	private PriorityQueue<ResultSet> _queue;

	MultipleResultSet(ResultSet[] results, Comparator<? super ResultSet> comparator) {
		_results = results;
		_comparator = comparator;
	}

	/**
	 * Returns the current {@link ResultSet}.
	 * <p>
	 * Must only be called when {@link #next()} returns <code>true</code>.
	 * </p>
	 */
	@Override
	public ResultSet impl() {
		return _queue.peek();
	}

	@Override
	public boolean next() throws SQLException {
		if (_queue == null) {
			_queue = initQueue(_results);
			return !_queue.isEmpty();
		}
		if (_queue.isEmpty()) {
			return false;
		}
		ResultSet lastResult = _queue.poll();
		if (lastResult.next()) {
			_queue.offer(lastResult);
			return true;
		} else {
			lastResult.close();
			return !_queue.isEmpty();
		}

	}

	@SuppressWarnings("unused")
	private PriorityQueue<ResultSet> initQueue(ResultSet[] results) throws SQLException {
		PriorityQueue<ResultSet> queue = new PriorityQueue<>(results.length, _comparator);
		if (true) {
			// Implementation detail of PriorityQueue: offering occurs in O(log(n)) time.
			for (ResultSet rs : results) {
				if (rs.next()) {
					queue.offer(rs);
				} else {
					rs.close();
				}
			}
		} else {
			// Sort the results first to avoid complexity n*(n*log(n)) of inserting into an ordered
			// list.
			int t = 0;
			for (int read = 0; read < results.length; read++) {
				ResultSet rs = results[read];
				if (rs.next()) {
					results[t++] = rs;
				} else {
					rs.close();
				}
			}
			Arrays.sort(results, 0, t, _comparator);
			for (int i = 0; i < t; i++) {
				queue.offer(results[i]);
			}
		}
		return queue;
	}

	@Override
	public void close() throws SQLException {
		closeResultSets();
	}

	private void closeResultSets() throws SQLException {
		SQLException problem = null;
		for (ResultSet r : _results) {
			if (r == null) {
				continue;
			}
			try {
				r.close();
			} catch (SQLException ex) {
				// last thrown exception is rethrown.
				problem = ex;
			}
		}
		if (problem != null) {
			throw problem;
		}
	}

}


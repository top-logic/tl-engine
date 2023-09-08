/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;

/**
 * A subclass of {@link LoggingDataSourceProxy} that gives access to its internal data structures.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@DeactivatedTest("Not a test itself, but a tool for tests.")
public class TestableLoggingDataSource extends LoggingDataSourceProxy.LoggingAnalyzer {

	static class StatisticsSummary {

		private final long _statementCount, _callCount, _rowCount, _elapsedMillis;

		public StatisticsSummary(long statementCount, long callCount, long rowCount, long elapsedMillis) {
			_statementCount = statementCount;
			_callCount = callCount;
			_rowCount = rowCount;
			_elapsedMillis = elapsedMillis;
		}

		public long getStatementCount() {
			return _statementCount;
		}

		public long getCallCount() {
			return _callCount;
		}

		public long getRowCount() {
			return _rowCount;
		}

		public long getElapsedMillis() {
			return _elapsedMillis;
		}

	}

	private TestableLoggingDataSource.StatisticsSummary _summary;

	private Map<String, Statistics> _entries = new HashMap<>();

	public TestableLoggingDataSource(InstantiationContext context, LoggingDataSourceProxy.LoggingAnalyzerConfig config)
			throws SQLException, ConfigurationException {
		super(context, config);
	}

	@Override
	protected void logStatisticsEntry(Statistics statistics, String sql) {
		assert !_entries.containsKey(sql);
		_entries.put(sql, statistics);
	}

	@Override
	protected void logStatisticsSummary(long statementCount, long callCount, long rowCount, long elapsedMillis) {
		assert _summary == null;
		_summary = new StatisticsSummary(statementCount, callCount, rowCount, elapsedMillis);
	}

	public TestableLoggingDataSource.StatisticsSummary getSummary() {
		return _summary;
	}

	public Map<String, Statistics> getEntries() {
		return _entries;
	}

	public void clear() {
		_summary = null;
		_entries.clear();
	}

}
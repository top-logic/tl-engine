/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static junit.framework.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import test.com.top_logic.basic.sql.dummy.DummyDataSource;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Config;
import com.top_logic.basic.sql.LoggingDataSourceProxy.LoggingAnalyzer;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;

/**
 * Utils for tests of the {@link LoggingDataSourceProxy}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class LoggingDataSourceTestUtil {

	public static void assertIdenticalUpdates(int calls, int rows, String sql, LoggingDataSourceProxy dataSource) {
		assertIdenticalUpdates(calls, rows, sql, dataSource, "");
	}

	public static void assertIdenticalUpdates(int calls, int rows, String sql, LoggingDataSourceProxy dataSource,
			String errorMessagePrefix) {
		TestableLoggingDataSource analyzer = analyzer(dataSource);

		// No summary after a single statement
		assertNull(errorMessagePrefix, analyzer.getSummary());

		if (calls > 0) {
			assertTrue(errorMessagePrefix, analyzer.getEntries().containsKey(sql));
			Statistics statistics = analyzer.getEntries().get(sql);
			assertEquals(errorMessagePrefix, calls, statistics.getCnt());
			assertEquals(errorMessagePrefix, rows, statistics.getRowsSum());
		} else {
			assertFalse(errorMessagePrefix, analyzer.getEntries().containsKey(sql));
		}
	}

	private static TestableLoggingDataSource analyzer(LoggingDataSourceProxy dataSource) {
		TestableLoggingDataSource analyzer = (TestableLoggingDataSource) dataSource.getAnalyzer();
		return analyzer;
	}

	public static void assertDistinctUpdates(int[] count, int[] rows, String[] sql, LoggingDataSourceProxy dataSource) {
		assertDistinctUpdates(count, rows, sql, dataSource, "");
	}

	public static void assertDistinctUpdates(int[] count, int[] rows, String[] sql,
			LoggingDataSourceProxy dataSource, String errorMessagePrefix) {
		if (sql.length != count.length) {
			throw new IllegalArgumentException();
		}
		if (sql.length != rows.length) {
			throw new IllegalArgumentException();
		}
		TestableLoggingDataSource analyzer = analyzer(dataSource);

		TestableLoggingDataSource.StatisticsSummary summary = analyzer.getSummary();
		if (sql.length > 1) {
			assertNotNull(errorMessagePrefix, summary);
			assertEquals(errorMessagePrefix, sql.length, summary.getStatementCount());
			assertEquals(errorMessagePrefix, ArrayUtil.sum(count), summary.getCallCount());
			assertEquals(errorMessagePrefix, ArrayUtil.sum(rows), summary.getRowCount());
		} else {
			assertNull(errorMessagePrefix, analyzer.getSummary());
		}

		for (int i = 0; i < sql.length; i++) {
			assertTrue(errorMessagePrefix, analyzer.getEntries().containsKey(sql[i]));
			Statistics statistics = analyzer.getEntries().get(sql[i]);
			assertEquals(errorMessagePrefix, count[i], statistics.getCnt());
			assertEquals(errorMessagePrefix, rows[i], statistics.getRowsSum());
		}
	}

	public static LoggingDataSourceProxy newDataSource() throws SQLException, ConfigurationException {
		return newDataSource(createConfig());
	}

	public static void flush(LoggingDataSourceProxy dataSource) {
		((LoggingAnalyzer) dataSource.getAnalyzer()).flush();
	}

	public static LoggingDataSourceProxy newDataSource(Config config) throws SQLException, ConfigurationException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		LoggingDataSourceProxy result = new LoggingDataSourceProxy(context, config);
		result.setAnalyzer(new TestableLoggingDataSource(context, config));
		return result;
	}

	public static Config createConfig() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setDriverClassName(DummyDataSource.class.getCanonicalName());
		config.setLogTimeoutNanos(Long.MAX_VALUE);
		config.setLogStacktrace(false);
		return config;
	}

	public static void readAndClose(ResultSet results) throws SQLException {
		while (results.next()) {
			// Do nothing, just iterate to the end.
		}
		results.close();
	}

}

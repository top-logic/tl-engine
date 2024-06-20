/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig;

/**
 * {@link DataSource} wrapper that logs all statements that are executed.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LoggingDataSourceProxy extends DefaultDataSourceProxy {

	/**
	 * Configuration options for {@link LoggingDataSourceProxy}.
	 */
	public interface Config extends DataSourceConfig, LoggingAnalyzerConfig {

		/**
		 * @see #getEnableOnStartup()
		 */
		String ENABLE_ON_STARTUP = "enableOnStartup";

		/**
		 * Whether to enable logging to the application log on application startup.
		 */
		@Name(ENABLE_ON_STARTUP)
		boolean getEnableOnStartup();

		/**
		 * @see #getEnableOnStartup()
		 */
		void setEnableOnStartup(boolean value);

	}

	/**
	 * Configuration of {@link LoggingAnalyzer}
	 */
	public interface LoggingAnalyzerConfig extends ConfigurationItem {
		/**
		 * @see #getIgnorePattern()
		 */
		String IGNORE_PATTERN_PROPERTY = "ignorePattern";

		/**
		 * @see #getLogAggregate()
		 */
		String LOG_AGGREGATE_PROPERTY = "logAggregate";

		/**
		 * @see #getLogStacktrace()
		 */
		String LOG_STACKTRACE_PROPERTY = "logStacktrace";

		/**
		 * @see #getLogTimeoutNanos()
		 */
		String LOG_TIMEOUT_NANOS_PROPERTY = "logTimeoutNanos";

		/**
		 * @see #getWarnThresholdCallCount()
		 */
		String WARN_THRESHOLD_CALL_COUNT_PROPERTY = "warnThresholdCallCount";

		/**
		 * @see #getWarnThresholdMillis()
		 */
		String WARN_THRESHOLD_MILLIS_PROPERTY = "warnThresholdMillis";
		
		/**
		 * @see #getWarnThresholdCallCountSum()
		 */
		String WARN_THRESHOLD_CALL_COUNT_SUM_PROPERTY = "warnThresholdCallCountSum";

		/**
		 * @see #getWarnThresholdMillisSum()
		 */
		String WARN_THRESHOLD_MILLIS_SUM_PROPERTY = "warnThresholdMillisSum";

		/**
		 * Configuration option: Regular expression that matches {@link StackTraceElement}s whose
		 * {@link DataSource} activity should not be logged.
		 */
		@Name(Config.IGNORE_PATTERN_PROPERTY)
		String getIgnorePattern();

		/**
		 * @see #getIgnorePattern()
		 */
		void setIgnorePattern(String canonicalName);

		/**
		 * Configuration option: Whether to aggregate multiple similar requests to a single log
		 * entry.
		 */
		@Name(Config.LOG_AGGREGATE_PROPERTY)
		@BooleanDefault(true)
		boolean getLogAggregate();

		/**
		 * Configuration option: Whether the stack trace of the {@link DataSource} request should be
		 * logged to.
		 */
		@Name(Config.LOG_STACKTRACE_PROPERTY)
		@BooleanDefault(true)
		boolean getLogStacktrace();

		/**
		 * @see #getLogStacktrace()
		 */
		void setLogStacktrace(boolean b);

		/**
		 * Configuration option: Nano seconds between two requests before aggregated log entries are
		 * flushed.
		 */
		@Name(Config.LOG_TIMEOUT_NANOS_PROPERTY)
		@LongDefault(500000000)
		long getLogTimeoutNanos();

		/**
		 * @see #getLogTimeoutNanos()
		 */
		void setLogTimeoutNanos(long maxValue);

		/**
		 * Configuration option: Number of calls of one SQL statement between two flushes, before a
		 * warning is logged and not just an info.
		 * <p>
		 * Value Ranges:
		 * </p>
		 * <ul>
		 * <li>Positive values: Warn if there are more than that many calls.</li>
		 * <li>0: Always warn.</li>
		 * <li>Negative values: Never warn because of the call count.</li>
		 * </ul>
		 * 
		 * @see Config#WARN_THRESHOLD_CALL_COUNT_SUM_PROPERTY
		 * @see Config#WARN_THRESHOLD_MILLIS_PROPERTY
		 * @see Config#WARN_THRESHOLD_MILLIS_SUM_PROPERTY
		 */
		@Name(Config.WARN_THRESHOLD_CALL_COUNT_PROPERTY)
		@LongDefault(50)
		@Format(NegativeIsMaxValue.class)
		long getWarnThresholdCallCount();

		/**
		 * Configuration option: Milliseconds needed for the executions of one SQL statement between
		 * two flushes, before a warning is logged and not just an info.
		 * <p>
		 * Value Ranges:
		 * </p>
		 * <ul>
		 * <li>Positive values: Warn if there are more than that many calls.</li>
		 * <li>0: Always warn.</li>
		 * <li>Negative values: Never warn because of execution time.</li>
		 * </ul>
		 * 
		 * @see #getWarnThresholdCallCount()
		 * @see #getWarnThresholdMillis()
		 * @see #getWarnThresholdCallCountSum()
		 * @see #getWarnThresholdMillisSum()
		 */
		@Name(Config.WARN_THRESHOLD_MILLIS_PROPERTY)
		@LongDefault(500)
		@Format(NegativeIsMaxValue.class)
		long getWarnThresholdMillis();

		/**
		 * Configuration option: Number of calls of all SQL statements between two flushes, before a
		 * warning is logged and not just an info.
		 * <p>
		 * Value Ranges:
		 * </p>
		 * <ul>
		 * <li>Positive values: Warn if there are more than that many calls.</li>
		 * <li>0: Always warn.</li>
		 * <li>Negative values: Never warn because of the call count.</li>
		 * </ul>
		 * 
		 * @see #getWarnThresholdCallCount()
		 * @see #getWarnThresholdMillis()
		 * @see #getWarnThresholdCallCountSum()
		 * @see #getWarnThresholdMillisSum()
		 */
		@Name(Config.WARN_THRESHOLD_CALL_COUNT_SUM_PROPERTY)
		@LongDefault(50)
		@Format(NegativeIsMaxValue.class)
		long getWarnThresholdCallCountSum();

		/**
		 * Configuration option: Milliseconds needed for the executions of all SQL statements
		 * between two flushes, before a warning is logged and not just an info.
		 * <p>
		 * Value Ranges:
		 * </p>
		 * <ul>
		 * <li>Positive values: Warn if there are more than that many calls.</li>
		 * <li>0: Always warn.</li>
		 * <li>Negative values: Never warn because of execution time.</li>
		 * </ul>
		 * 
		 * @see #getWarnThresholdCallCount()
		 * @see #getWarnThresholdMillis()
		 * @see #getWarnThresholdCallCountSum()
		 * @see #getWarnThresholdMillisSum()
		 */
		@Name(Config.WARN_THRESHOLD_MILLIS_SUM_PROPERTY)
		@LongDefault(500)
		@Format(NegativeIsMaxValue.class)
		long getWarnThresholdMillisSum();

		/**
		 * {@link ConfigurationValueProvider} that treats values lower than zero as
		 * {@link Long#MAX_VALUE}.
		 */
		class NegativeIsMaxValue extends AbstractConfigurationValueProvider<Long> {

			/**
			 * Singleton {@link NegativeIsMaxValue} instance.
			 */
			public static final NegativeIsMaxValue INSTANCE = new Config.NegativeIsMaxValue();

			private NegativeIsMaxValue() {
				super(Long.class);
			}

			@Override
			protected Long getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				long value = Long.parseLong(propertyValue.toString());
				if (value < 0) {
					return Long.MAX_VALUE;
				}
				return Long.valueOf(value);
			}

			@Override
			protected String getSpecificationNonNull(Long configValue) {
				if (configValue.longValue() == Long.MAX_VALUE) {
					return "-1";
				}
				return configValue.toString();
			}

		}
	}

	/**
	 * Plug-in that receives the observed statements.
	 */
	public interface StatementAnalyzer {

		/**
		 * Notifies about a single statement
		 * 
		 * @param sql
		 *        The SQL statement source.
		 * @param elapsed
		 *        the time in nano seconds, the operation took.
		 * @param rows
		 *        The number of rows retrieved or sent.
		 * @param calls
		 *        The number of calls that actually hit the database (zero for operations within a
		 *        batch).
		 */
		void log(String sql, long elapsed, long rows, int calls);

	}

	private volatile StatementAnalyzer _analyzer;
	
	/**
	 * Creates a {@link LoggingDataSourceProxy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LoggingDataSourceProxy(InstantiationContext context, Config config)
			throws SQLException, ConfigurationException {
		super(null, DefaultDataSourceFactory.genericDriverOptions(config));

		if (config.getEnableOnStartup()) {
			_analyzer = new LoggingAnalyzer(context, config);
		}
	}

	/**
	 * Whether statement analysis is active.
	 */
	public final boolean isEnabled() {
		return _analyzer != null;
	}

	/**
	 * The currently installed {@link StatementAnalyzer}, or <code>null</code> if analysis is
	 * disabled.
	 */
	public StatementAnalyzer getAnalyzer() {
		return _analyzer;
	}

	/**
	 * Installs a {@link StatementAnalyzer} into this datasource.
	 * 
	 * @param analyzer
	 *        The analyzer of <code>null</code> for disabling analysis.
	 */
	public void setAnalyzer(StatementAnalyzer analyzer) {
		_analyzer = analyzer;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (isEnabled()) {
			return new LoggingConnection(super.getConnection());
		} else {
			return super.getConnection();
		}
	}

	class LoggingConnection extends ConnectionProxy {

		private final Connection _connection;

		public LoggingConnection(Connection connection) {
			_connection = connection;
		}

		@Override
		protected Connection impl() {
			return _connection;
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1), a1);
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1, int a2) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1, a2), a1);
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1, int a2, int a3) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1, a2, a3), a1);
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1, int a2, int a3, int a4) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1, a2, a3, a4), a1);
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1, int[] a2) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1, a2), a1);
		}
		
		@Override
		public PreparedStatement prepareStatement(String a1, String[] a2) throws SQLException {
			return new LoggingPreparedStatement(super.prepareStatement(a1, a2), a1);
		}
		
		@Override
		public Statement createStatement() throws SQLException {
			return new LoggingStatement(super.createStatement());
		}
		
		@Override
		public Statement createStatement(int a1, int a2) throws SQLException {
			return new LoggingStatement(super.createStatement(a1, a2));
		}

		@Override
		public Statement createStatement(int a1, int a2, int a3) throws SQLException {
			return new LoggingStatement(super.createStatement(a1, a2, a3));
		}

		class LoggingPreparedStatement extends PreparedStatementProxy {

			private final PreparedStatement _statement;

			private final String _sql;

			public LoggingPreparedStatement(PreparedStatement prepareStatement, String sql) {
				_statement = prepareStatement;
				_sql = sql;
			}

			@Override
			protected PreparedStatement impl() {
				return _statement;
			}

			@Override
			public boolean execute() throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute();
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(_sql, elapsed);
				} else {
					log(_sql, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public void addBatch() throws SQLException {
				long elapsed = -System.nanoTime();
				super.addBatch();
				elapsed += System.nanoTime();
				log(_sql, elapsed, 0, 0);
			}

			@Override
			public void addBatch(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				super.addBatch(a1);
				elapsed += System.nanoTime();
				log(a1, elapsed, 0, 0);
			}

			@Override
			public int[] executeBatch() throws SQLException {
				long elapsed = -System.nanoTime();
				int[] result = super.executeBatch();
				elapsed += System.nanoTime();
				/* The result of executeBatch can contain negative numbers that are used to
				 * represent other information like "SUCCESS_NO_INFO" and "EXECUTE_FAILED".
				 * Therefore, sum only the positive entries. */
				log(_sql, elapsed, sumPositives(result));
				return result;
			}

			private int sumPositives(int[] numbers) {
				int sum = 0;
				for (int i = 0; i < numbers.length; i++) {
					int summand = numbers[i];
					if (summand > 0) {
						sum += summand;
					}
				}
				return sum;
			}

			@Override
			public ResultSet executeQuery() throws SQLException {
				long elapsed = -System.nanoTime();
				ResultSet result = super.executeQuery();
				elapsed += System.nanoTime();

				return new LoggingResultSet(_sql, elapsed, result);
			}

			@Override
			public int executeUpdate() throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate();
				elapsed += System.nanoTime();
				log(_sql, elapsed, result);
				return result;
			}

			@Override
			public boolean execute(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public boolean execute(String a1, int a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public boolean execute(String a1, int[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public boolean execute(String a1, String[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public ResultSet executeQuery(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				ResultSet result = super.executeQuery(a1);
				elapsed += System.nanoTime();

				return new LoggingResultSet(_sql, elapsed, result);
			}

			@Override
			public int executeUpdate(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1);
				elapsed += System.nanoTime();
				log(_sql, elapsed, result);
				return result;
			}

			@Override
			public int executeUpdate(String a1, int a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(_sql, elapsed, result);
				return result;
			}

			@Override
			public int executeUpdate(String a1, int[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(_sql, elapsed, result);
				return result;
			}

			@Override
			public int executeUpdate(String a1, String[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(_sql, elapsed, result);
				return result;
			}

			@Override
			public Connection getConnection() throws SQLException {
				return LoggingConnection.this;
			}

			@Override
			public ResultSet getResultSet() throws SQLException {
				return new LoggingResultSet(_sql, 0, super.getResultSet());
			}

		}

		class LoggingStatement extends StatementProxy {

			private final Statement _statement;

			public LoggingStatement(Statement statement) {
				_statement = statement;
			}

			@Override
			protected Statement impl() {
				return _statement;
			}
			
			@Override
			public boolean execute(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}
			
			@Override
			public boolean execute(String a1, int a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}
			
			@Override
			public boolean execute(String a1, int[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}
			
			@Override
			public boolean execute(String a1, String[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				boolean result = super.execute(a1, a2);
				elapsed += System.nanoTime();
				if (result || getUpdateCount() < 0) {
					log(a1, elapsed);
				} else {
					log(a1, elapsed, getUpdateCount());
				}
				return result;
			}

			@Override
			public ResultSet executeQuery(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				ResultSet result = super.executeQuery(a1);
				elapsed += System.nanoTime();

				return new LoggingResultSet(a1, elapsed, result);
			}
			
			@Override
			public int executeUpdate(String a1) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1);
				elapsed += System.nanoTime();
				log(a1, elapsed, result);
				return result;
			}
			
			@Override
			public int executeUpdate(String a1, int a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(a1, elapsed, result);
				return result;
			}
			
			@Override
			public int executeUpdate(String a1, int[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(a1, elapsed, result);
				return result;
			}
			
			@Override
			public int executeUpdate(String a1, String[] a2) throws SQLException {
				long elapsed = -System.nanoTime();
				int result = super.executeUpdate(a1, a2);
				elapsed += System.nanoTime();
				log(a1, elapsed, result);
				return result;
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return LoggingConnection.this;
			}

		}
	}
	
	private class LoggingResultSet extends ResultSetProxy {

		private final String _sql;
		private final ResultSet _impl;
		private long _elapsed;
		private long _rows;

		public LoggingResultSet(String sql, long elapsed, ResultSet result) {
			_sql = sql;
			_elapsed = elapsed;
			_impl = result;
		}

		@Override
		protected ResultSet impl() {
			return _impl;
		}
		
		@Override
		public boolean next() throws SQLException {
			long elapsed = -System.nanoTime();
			boolean result = super.next();
			elapsed += System.nanoTime();
			if (result) {
				_rows++;
			}
			_elapsed += elapsed;
			return result;
		}
		
		@Override
		public void close() throws SQLException {
			super.close();
			
			log(_sql, _elapsed, _rows);
		}
		
	}
	
	/** Statistics about one group of SQL statements being logged. */
	public static class Statistics {
		private int _cnt = 0;

		private long _elapsedSum;

		private long _elapsedMax;
		
		private long _rowsSum;
		
		private long _rowsMax;
		
		private Exception _stacktrace;
		
		Statistics(Exception stacktrace) {
			super();
			
			_stacktrace = stacktrace;
		}

		/**
		 * The number of total calls that actually hit the database (zero for operations within a
		 * batch).
		 */
		public int getCnt() {
			return _cnt;
		}

		void inc(long elapsed, long rows, int calls) {
			_cnt += calls;
			_elapsedSum += elapsed;
			if (elapsed > _elapsedMax) {
				_elapsedMax = elapsed;
			}
			_rowsSum += rows;
			if (rows > _rowsMax) {
				_rowsMax = rows;
			}
		}

		/**
		 * The stack trace of the location where the first one of the aggregated statements was
		 * issued from.
		 */
		public Exception getStacktrace() {
			return _stacktrace;
		}

		/**
		 * The maximum time in nanoseconds one of the aggregated statements took.
		 */
		public long getElapsedNanoMax() {
			return _elapsedMax;
		}

		/**
		 * The average time in nanoseconds all of the aggregated statements took.
		 */
		public long getElapsedNanoAvg() {
			if (_cnt == 0) {
				return -1;
			}
			return _elapsedSum / _cnt;
		}

		/**
		 * The total time in nanoseconds all of the aggregated statements took.
		 */
		public long getElapsedNanoSum() {
			return _elapsedSum;
		}

		/**
		 * The maximum rows returned from all of the aggregated statements.
		 */
		public long getRowsMax() {
			return _rowsMax;
		}

		/**
		 * The average number of rows returned from all of the aggregated statements.
		 */
		public long getRowsAvg() {
			if (_cnt == 0) {
				return -1;
			}
			return _rowsSum / _cnt;
		}

		/**
		 * The total number of rows returned from all of the aggregated statements.
		 */
		public long getRowsSum() {
			return _rowsSum;
		}

		@Override
		public String toString() {
			return "Statistics(count = " + _cnt + "; elapsedSum = " + _elapsedSum + "; elapsedMax = " + _elapsedMax
				+ "; rowsSum = " + _rowsSum + "; rowsMax = " + _rowsMax + "; stacktrace = " + _stacktrace + ")";
		}

		/**
		 * Creates a copy of this {@link Statistics}.
		 */
		public Statistics copy() {
			Statistics result = new Statistics(_stacktrace);
			result._cnt = _cnt;
			result._elapsedSum = _elapsedSum;
			result._elapsedMax = _elapsedMax;
			result._rowsSum = _rowsSum;
			result._rowsMax = _rowsMax;
			return result;
		}

	}

	private static final class StatisticsComparator implements Comparator<Entry<String, Statistics>> {

		/** The singleton {@link LoggingDataSourceProxy.StatisticsComparator} instance. */
		public static final StatisticsComparator SINGLETON = new StatisticsComparator();

		@Override
		public int compare(Entry<String, Statistics> o1, Entry<String, Statistics> o2) {
			return compare(o1.getValue().getElapsedNanoSum(), o2.getValue().getElapsedNanoSum());
		}

		private int compare(long a, long b) {
			return a < b ? -1 : (a == b ? 0 : 1);
		}

	}

	final void log(String a1, long elapsed) {
		log(a1, elapsed, 0);
	}
	
	final void log(String a1, long elapsed, long rows) {
		log(a1, elapsed, rows, 1);
	}

	/**
	 * Logs the given database operation.
	 * 
	 * @param a1
	 *        The SQL statement source.
	 * @param elapsed
	 *        the time in nano seconds, the operation took.
	 * @param rows
	 *        The number of rows retrieved or sent.
	 * @param calls
	 *        The number of calls that actually hit the database (zero for operations within a
	 *        batch).
	 */
	void log(String a1, long elapsed, long rows, int calls) {
		if (isEnabled()) {
			_analyzer.log(a1, elapsed, rows, calls);
		}
	}

	/**
	 * {@link StatementAnalyzer} that aggregates multiple similar statements into a single
	 * {@link Statistics} entry.
	 */
	public static class AggregatingAnalyzer implements StatementAnalyzer {

		/**
		 * {@link Pattern} matching an SQL "in" statement and its set literal.
		 * <p>
		 * Captures the "in" statement in the first group to preserve its case.
		 * </p>
		 */
		private static final Pattern SET_LITERAL_PATTERN = Pattern.compile("(?i)\\b(in)\\s+\\([^\\)]+\\)");

		/**
		 * Pattern matching a number followed or following one of the char " =><;()[]"
		 */
		private static final Pattern NUMBER_PATTERN =
			Pattern.compile("(^|[\\s=><;\\(\\)\\[\\]])\\d+($|[\\s=><;\\(\\)\\[\\]])");

		private final Pattern _literalStringPattern;

		private final Map<String, Statistics> buffer = new HashMap<>();

		private final boolean _allocateStacktrace;

		private final char _stringQuoteChar;

		/**
		 * Creates a {@link AggregatingAnalyzer}.
		 */
		public AggregatingAnalyzer(boolean allocateStacktrace, char stringQuoteChar) {
			_allocateStacktrace = allocateStacktrace;
			_stringQuoteChar = stringQuoteChar;
			_literalStringPattern =
				Pattern.compile(_stringQuoteChar + "[^" + _stringQuoteChar + "]+" + _stringQuoteChar);
		}

		/**
		 * Creates a {@link AggregatingAnalyzer}.
		 */
		public AggregatingAnalyzer(boolean allocateStacktrace) {
			this(allocateStacktrace, '\'');
		}

		@Override
		public void log(String sql, long elapsed, long rows, int calls) {
			String key = unifySQL(sql);
			synchronized (buffer) {
				Statistics statistics = buffer.get(key);
				if (statistics == null) {
					statistics = new Statistics(allocateStacktrace());
					buffer.put(key, statistics);
				}
				statistics.inc(elapsed, rows, calls);
			}
		}

		private String unifySQL(String sql) {

			String unified = sql;
			// Take the "in" from the first group to preserve its case.
			unified = SET_LITERAL_PATTERN.matcher(unified).replaceAll("$1 (...)");
			unified = NUMBER_PATTERN.matcher(unified).replaceAll("$1<number>$2");
			unified = _literalStringPattern.matcher(unified).replaceAll(_stringQuoteChar + "..." + _stringQuoteChar);
			return unified;
		}

		private Exception allocateStacktrace() {
			if (shouldAllocateStacktrace()) {
				return new Exception("Stack trace.");
			} else {
				return null;
			}
		}

		/**
		 * Whether stack traces should be collected.
		 */
		protected boolean shouldAllocateStacktrace() {
			return _allocateStacktrace;
		}

		/**
		 * Removes the collected {@link Statistics} entries from the internal buffer for further
		 * processing.
		 */
		public List<Entry<String, Statistics>> take() {
			synchronized (buffer) {
				if (!buffer.isEmpty()) {
					ArrayList<Entry<String, Statistics>> result =
						new ArrayList<>(buffer.entrySet());
					buffer.clear();
					return result;
				} else {
					return Collections.emptyList();
				}
			}
		}

		static class DefaultEntry<K, V> implements Map.Entry<K, V> {

			private final K _kex;

			private V _value;

			public DefaultEntry(K kex, V value) {
				_kex = kex;
				_value = value;
			}

			@Override
			public K getKey() {
				return _kex;
			}

			@Override
			public V getValue() {
				return _value;
			}

			@Override
			public V setValue(V value) {
				V oldValue = _value;
				_value = value;
				return oldValue;
			}

		}

		/**
		 * Takes a snapshot of the current monitoring data without reseting all statistics.
		 */
		public List<Entry<String, Statistics>> snapshot() {
			synchronized (buffer) {
				if (!buffer.isEmpty()) {
					List<Entry<String, Statistics>> result = new ArrayList<>();
					for (Entry<String, Statistics> entry : buffer.entrySet()) {
						result.add(new DefaultEntry<>(entry.getKey(), entry.getValue().copy()));
					}
					return result;
				} else {
					return Collections.emptyList();
				}
			}
		}
	}

	/**
	 * {@link AggregatingAnalyzer} that writes information to the application log.
	 */
	public static class LoggingAnalyzer extends AggregatingAnalyzer {

		private final long _warnThresholdCallCount;

		private final long _warnThresholdMillis;

		private final long _warnThresholdCallCountSum;

		private final long _warnThresholdMillisSum;

		private final Pattern ignorePattern;

		private boolean logStacktrace;

		private boolean logAggregate;

		private long lastCall;

		private final long _logTimeout;

		/**
		 * Creates a {@link LoggingAnalyzer} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public LoggingAnalyzer(InstantiationContext context, LoggingAnalyzerConfig config) {
			super(config.getLogStacktrace());

			ignorePattern = initIgnorePattern(config);
			logStacktrace = config.getLogStacktrace();
			logAggregate = config.getLogAggregate();
			_logTimeout = config.getLogTimeoutNanos();
			_warnThresholdCallCount = config.getWarnThresholdCallCount();
			_warnThresholdMillis = config.getWarnThresholdMillis();
			_warnThresholdCallCountSum = config.getWarnThresholdCallCountSum();
			_warnThresholdMillisSum = config.getWarnThresholdMillisSum();
		}

		private Pattern initIgnorePattern(LoggingAnalyzerConfig config) {
			String ignorePatternSrc = config.getIgnorePattern();
			if (StringServices.isEmpty(ignorePatternSrc)) {
				return null;
			} else {
				return Pattern.compile(ignorePatternSrc);
			}
		}

		@Override
		public void log(String sql, long elapsed, long rows, int calls) {
			super.log(sql, elapsed, rows, calls);
			if (logAggregate) {
				long now = System.nanoTime();
				if (now - lastCall > _logTimeout) {
					flush();
				}
				lastCall = now;
			} else {
				flush();
			}
		}

		@Override
		protected boolean shouldAllocateStacktrace() {
			return super.shouldAllocateStacktrace() || ignorePattern != null;
		}

		/**
		 * Flushes all aggregated log entries to the logger.
		 */
		public final void flush() {
			List<Entry<String, Statistics>> entries = take();

			Collections.sort(entries, StatisticsComparator.SINGLETON);

			logStatistics(entries);
		}

		private void logStatistics(List<Entry<String, Statistics>> entries) {
			long statementCountSum = 0;
			long callCountSum = 0;
			long rowCountSum = 0;
			long elapsedMillisSum = 0;
			for (Entry<String, Statistics> entry : entries) {
				Statistics statistics = entry.getValue();
				if (isIgnored(statistics.getStacktrace())) {
					continue;
				}
				logStatisticsEntry(statistics, entry.getKey());

				statementCountSum++;
				callCountSum += statistics.getCnt();
				rowCountSum += statistics.getRowsSum();
				elapsedMillisSum += (statistics.getElapsedNanoSum() / 1000000L);
			}

			// Note: It's possible that all details have been ignored or only a single statement has
			// been logged. In such cases, a summary line is not useful.
			if (statementCountSum > 1) {
				logStatisticsSummary(statementCountSum, callCountSum, rowCountSum, elapsedMillisSum);
			}
		}

		private boolean isIgnored(Exception caller) {
			if (caller == null) {
				return false;
			}
			if (ignorePattern != null) {
				StackTraceElement[] stackTrace = caller.getStackTrace();
				for (int n = stackTrace.length - 1; n >= 0; n--) {
					StackTraceElement element = stackTrace[n];
					Matcher matcher = ignorePattern.matcher(element.getClassName());
					if (matcher.find()) {
						return true;
					}
				}
			}
			return false;
		}

		/** Is called to log the statistics summary. */
		protected void logStatisticsSummary(
				long statementCountSum, long callCountSum, long rowCountSum, long elapsedMillisSum) {
			String summary = "Database usage: " + DebugHelper.toDuration(elapsedMillisSum) + " for " + callCountSum
				+ " database calls, " + statementCountSum + " unique statements, "
				+ rowCountSum + " total rows.";
			if (callCountSum > _warnThresholdCallCountSum || elapsedMillisSum > _warnThresholdMillisSum) {
				Logger.warn(summary, LoggingDataSourceProxy.class);
			} else {
				Logger.info(summary, LoggingDataSourceProxy.class);
			}
		}

		/** Is called to log a statistics entry. */
		protected void logStatisticsEntry(Statistics statistics, String sql) {
			long elapsedMillis = statistics.getElapsedNanoSum() / 1000000L;
			String message = statistics.getCnt() + " times " + "(" + DebugHelper.toDuration(elapsedMillis) + ", "
				+ statistics.getRowsSum() + " rows) " + sql;
			Throwable loggedStacktrace = logStacktrace ? statistics.getStacktrace() : null;
			if (statistics.getCnt() > _warnThresholdCallCount || elapsedMillis > _warnThresholdMillis) {
				Logger.warn(message, loggedStacktrace, LoggingDataSourceProxy.class);
			} else {
				Logger.info(message, loggedStacktrace, LoggingDataSourceProxy.class);
			}
		}
	}

}

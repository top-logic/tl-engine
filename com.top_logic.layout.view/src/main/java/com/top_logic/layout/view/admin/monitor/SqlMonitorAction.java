/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.AggregatingAnalyzer;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;
import com.top_logic.basic.sql.LoggingDataSourceProxy.StatementAnalyzer;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} controlling SQL statement monitoring on the default connection pool and
 * returning the current statistics (one entry per normalized statement) for display.
 *
 * <p>
 * Monitoring works by installing a {@link AggregatingAnalyzer} on the pool's
 * {@link LoggingDataSourceProxy}; the analyzer lives on the (shared) data source between commands,
 * so each {@link Mode} simply re-fetches it. The returned statistics list is meant to be written to
 * a channel feeding a {@link SqlStatisticsTable}.
 * </p>
 */
public class SqlMonitorAction implements ViewAction {

	/**
	 * What a {@link SqlMonitorAction} does to the monitoring state.
	 */
	public enum Mode {
		/** Install a fresh analyzer and clear the pool; returns an empty list. */
		START,

		/** Return a snapshot of the running analyzer's statistics, leaving it installed. */
		REFRESH,

		/** Return the running analyzer's final statistics and uninstall it. */
		STOP;
	}

	/**
	 * Configuration for {@link SqlMonitorAction}.
	 */
	@TagName("sql-monitor")
	public interface Config extends PolymorphicConfiguration<SqlMonitorAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		@Override
		@ClassDefault(SqlMonitorAction.class)
		Class<? extends SqlMonitorAction> getImplementationClass();

		/**
		 * What the action does to the monitoring state.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();
	}

	private final Mode _mode;

	/**
	 * Creates a new {@link SqlMonitorAction} from configuration.
	 */
	@CalledByReflection
	public SqlMonitorAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DataSource dataSource = pool.getDataSource();
		if (!(dataSource instanceof LoggingDataSourceProxy proxy)) {
			InfoService.showInfo(I18NConstants.SQL_MONITORING_UNAVAILABLE);
			return List.of();
		}

		switch (_mode) {
			case START:
				proxy.setAnalyzer(new AggregatingAnalyzer(true, stringQuoteChar(pool)));
				pool.clear();
				return List.of();
			case REFRESH:
				return analyzer(proxy) != null ? analyzer(proxy).snapshot() : List.<Entry<String, Statistics>> of();
			case STOP:
				AggregatingAnalyzer analyzer = analyzer(proxy);
				List<Entry<String, Statistics>> result =
					analyzer != null ? analyzer.take() : List.<Entry<String, Statistics>> of();
				proxy.setAnalyzer(null);
				return result;
		}
		return List.of();
	}

	/**
	 * The installed {@link AggregatingAnalyzer}, or {@code null} if monitoring is not running.
	 */
	private static AggregatingAnalyzer analyzer(LoggingDataSourceProxy proxy) {
		StatementAnalyzer analyzer = proxy.getAnalyzer();
		return analyzer instanceof AggregatingAnalyzer aggregating ? aggregating : null;
	}

	/**
	 * The SQL string quote character of the pool's dialect, defaulting to {@code '} on error.
	 */
	private static char stringQuoteChar(ConnectionPool pool) {
		try {
			return pool.getSQLDialect().stringQuoteChar();
		} catch (SQLException ex) {
			return '\'';
		}
	}
}

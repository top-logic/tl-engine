/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.sql.DataSource;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.AggregatingAnalyzer;
import com.top_logic.basic.sql.LoggingDataSourceProxy.StatementAnalyzer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.TLContext;

/**
 * Base class for {@link CommandHandler}s accessing the monitoring result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MonitoringCommand extends AbstractCommandHandler {

	private static final Property<AggregatingAnalyzer> ANALYZER = TypedAnnotatable.property(AggregatingAnalyzer.class, "analyzer");

	private static final Property<HttpSessionBindingListener> LISTENER =
		TypedAnnotatable.property(HttpSessionBindingListener.class, "listener");

	/**
	 * The monitored datasource.
	 */
	protected static DataSource datasource() {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DataSource dataSource = pool.getDataSource();
		return dataSource;
	}

	/**
	 * Creates a {@link MonitoringCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MonitoringCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Installs a {@link StatementAnalyzer} to the {@link #datasource()}.
	 */
	public final void installAnalyzer(StatementAnalyzer analyzer) {
		final LoggingDataSourceProxy datasource = (LoggingDataSourceProxy) datasource();
		if (analyzer == null) {
			datasource.setAnalyzer(null);

			// Remove safety listener.
			TLSessionContext sessionContext = TLContext.getContext().getSessionContext();
			HttpSessionBindingListener listener = sessionContext.reset(LISTENER);
			if (listener != null) {
				sessionContext.removeHttpSessionBindingListener(listener);
			}
		} else {
			// Install removal listener to prevent leaving the analyzer installed after the
			// monitoring session has terminated.
			TLSessionContext sessionContext = TLContext.getContext().getSessionContext();
			HttpSessionBindingListener listener = new HttpSessionBindingListener() {
				@Override
				public void valueBound(HttpSessionBindingEvent event) {
					// Ignore.
				}

				@Override
				public void valueUnbound(HttpSessionBindingEvent event) {
					datasource.setAnalyzer(null);
				}
			};
			sessionContext.set(LISTENER, listener);
			sessionContext.addHttpSessionBindingListener(listener);

			datasource.setAnalyzer(analyzer);
		}
	}

	/**
	 * The installed {@link StatementAnalyzer} from {@link #datasource()}.
	 */
	public static final StatementAnalyzer getAnalyzer() {
		return ((LoggingDataSourceProxy) datasource()).getAnalyzer();
	}

	/**
	 * Whether a {@link StatementAnalyzer} was installed into the given component.
	 */
	public static boolean hasResult(LayoutComponent component) {
		return component.isSet(MonitoringCommand.ANALYZER);
	}

	/**
	 * The {@link StatementAnalyzer} installed to the given component.
	 */
	public static AggregatingAnalyzer getResult(LayoutComponent component) {
		return component.get(ANALYZER);
	}

	/**
	 * Installs a {@link StatementAnalyzer} to the given component.
	 */
	public static void setResult(LayoutComponent aComponent, AggregatingAnalyzer analyzer) {
		aComponent.set(ANALYZER, analyzer);
	}

	/**
	 * Removes an installed {@link StatementAnalyzer} from the given component.
	 */
	public static AggregatingAnalyzer removeResult(LayoutComponent component) {
		return component.reset(ANALYZER);
	}

}

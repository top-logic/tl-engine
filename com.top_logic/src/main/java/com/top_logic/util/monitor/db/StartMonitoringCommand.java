/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} starting the monitoring process.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StartMonitoringCommand extends MonitoringCommand {

	/**
	 * Creates a {@link StartMonitoringCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StartMonitoringCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		DataSource dataSource = datasource();
		if (dataSource instanceof LoggingDataSourceProxy) {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			char stringQuoteChar;
			try {
				stringQuoteChar = pool.getSQLDialect().stringQuoteChar();
			} catch (SQLException ex) {
				stringQuoteChar = '\'';
			}
			LoggingDataSourceProxy.AggregatingAnalyzer analyzer =
				new LoggingDataSourceProxy.AggregatingAnalyzer(true, stringQuoteChar);
			installAnalyzer(analyzer);
			pool.clear();

			MonitoringCommand.setResult(aComponent, analyzer);

			// Clear current display.
			aComponent.setModel(null);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}

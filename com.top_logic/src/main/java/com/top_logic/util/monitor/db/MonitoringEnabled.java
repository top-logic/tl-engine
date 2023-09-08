/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.util.Map;

import javax.sql.DataSource;

import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} enabling a monitoring command, if monitoring is enabled by
 * configuration.
 */
public class MonitoringEnabled implements ExecutabilityRule {

	/**
	 * Singleton {@link MonitoringEnabled} instance.
	 */
	public static final MonitoringEnabled INSTANCE = new MonitoringEnabled();

	private MonitoringEnabled() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		DataSource datasource = StartMonitoringCommand.datasource();
		if (!(datasource instanceof LoggingDataSourceProxy)) {
			return ExecutableState.createDisabledState(I18NConstants.NOT_PREPARED_FOR_MONITORING);
		}
		if (MonitoringCommand.getAnalyzer() != null) {
			return ExecutableState.createDisabledState(I18NConstants.ANOTHER_MONITORING_SESSION_RUNNING);
		}
		return ExecutableState.EXECUTABLE;
	}

}
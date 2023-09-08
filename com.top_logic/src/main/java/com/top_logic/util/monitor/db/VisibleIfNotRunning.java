/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} enabling a monitoring command, if monitoring is enabled by
 * configuration.
 */
public class VisibleIfNotRunning implements ExecutabilityRule {

	/**
	 * Singleton {@link VisibleIfNotRunning} instance.
	 */
	public static final VisibleIfNotRunning INSTANCE = new VisibleIfNotRunning();

	private VisibleIfNotRunning() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (MonitoringCommand.hasResult(aComponent)) {
			// Already analyzing
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		return ExecutableState.EXECUTABLE;
	}

}
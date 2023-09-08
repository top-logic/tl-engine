/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} enabling a {@link CommandHandler}, if data collection has finished with
 * a non-empty result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasDataCollected implements ExecutabilityRule {

	/**
	 * Singleton {@link HasDataCollected} instance.
	 */
	public static final HasDataCollected INSTANCE = new HasDataCollected();

	private HasDataCollected() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (MonitoringCommand.hasResult(aComponent)) {
			return ExecutableState.createDisabledState(I18NConstants.MONITORING_RUNNING);
		}
		if (model == null) {
			return ExecutableState.createDisabledState(I18NConstants.NO_DATA_COLLECTED);
		}
		return ExecutableState.EXECUTABLE;
	}

}
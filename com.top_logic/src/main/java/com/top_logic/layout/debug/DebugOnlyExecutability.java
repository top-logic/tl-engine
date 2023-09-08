/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.debug;

import java.util.Map;

import com.top_logic.layout.basic.DebuggingConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} enabling a command only if the application runs in debug mode.
 */
public class DebugOnlyExecutability implements ExecutabilityRule {

	/**
	 * Singleton {@link DebugOnlyExecutability} instance.
	 */
	public static final DebugOnlyExecutability INSTANCE = new DebugOnlyExecutability();

	private DebugOnlyExecutability() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return DebuggingConfig.configuredInstance().getShowDebugButtons() ? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}

}

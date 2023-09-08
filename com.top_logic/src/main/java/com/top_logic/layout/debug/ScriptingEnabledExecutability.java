/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.debug;

import java.util.Map;

import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} enabling a command only if script recording is enabled in an
 * application.
 */
public class ScriptingEnabledExecutability implements ExecutabilityRule {

	/**
	 * Singleton {@link ScriptingEnabledExecutability} instance.
	 */
	public static final ScriptingEnabledExecutability INSTANCE = new ScriptingEnabledExecutability();

	private ScriptingEnabledExecutability() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return ScriptingRecorder.isEnabled() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

}

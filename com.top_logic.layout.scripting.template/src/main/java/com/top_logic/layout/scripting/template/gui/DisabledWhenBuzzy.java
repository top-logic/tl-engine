/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} disabling a button, if either a script is running, or recording is
 * active.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DisabledWhenBuzzy implements ExecutabilityRule {

	/**
	 * Singleton {@link DisabledWhenBuzzy} instance.
	 */
	public static final DisabledWhenBuzzy INSTANCE = new DisabledWhenBuzzy();

	private DisabledWhenBuzzy() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (ApplyApplicationAction.isScriptRunning(aComponent)) {
			return ExecutableState.createDisabledState(I18NConstants.ERROR_REPLAY_RUNNING);
		}
		if (((ScriptRecorderTree) aComponent).isRecording()) {
			return ExecutableState.createDisabledState(I18NConstants.ERROR_CURRENTLY_RECORDING);
		}
		return ExecutableState.EXECUTABLE;
	}
}
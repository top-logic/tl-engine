/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.layout.scripting.recorder.gui.ScriptingGuiConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} for the server script selector.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ServerScriptSelectorExecutability implements ExecutabilityRule {

	/**
	 * Singleton {@link ServerScriptSelectorExecutability} instance.
	 */
	public static final ServerScriptSelectorExecutability INSTANCE = new ServerScriptSelectorExecutability();

	private ServerScriptSelectorExecutability() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (ScriptingGuiConfig.get().shouldShowServerScriptSelector()) {
			return ExecutableState.EXECUTABLE;
		}
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}

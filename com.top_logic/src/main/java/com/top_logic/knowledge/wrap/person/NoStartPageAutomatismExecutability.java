/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that {@link ExecutableState#NOT_EXEC_HIDDEN hides} the
 * {@link CommandHandler}, if the {@link PersonalConfiguration#getStartPageAutomatism()} is active.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NoStartPageAutomatismExecutability implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		if (getPersonalConfig().getStartPageAutomatism()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}

	private PersonalConfiguration getPersonalConfig() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

}

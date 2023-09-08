/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that displays a command, if its component is visible.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ComponentIsVisibleRule implements ExecutabilityRule {

	/**
	 * Singleton {@link ComponentIsVisibleRule} instance.
	 */
	public static final ComponentIsVisibleRule INSTANCE = new ComponentIsVisibleRule();

	private ComponentIsVisibleRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (!aComponent.isVisible()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		return ExecutableState.EXECUTABLE;
	}

}

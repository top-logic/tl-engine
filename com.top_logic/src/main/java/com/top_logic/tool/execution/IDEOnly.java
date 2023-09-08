/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.Environment;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} for a command that must be shown only when running the app in the
 * development environment.
 * 
 * @see Environment#isDeployed()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDEOnly implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return Environment.isDeployed() ? ExecutableState.NOT_EXEC_HIDDEN : ExecutableState.EXECUTABLE;
	}

}

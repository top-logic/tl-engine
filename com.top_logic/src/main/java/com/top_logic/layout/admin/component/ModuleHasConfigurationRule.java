/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ExecutabilityRule} which ensures that only services with a configuration are executable.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ModuleHasConfigurationRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		if (model instanceof BasicRuntimeModule<?>) {
			BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;

			boolean hasConfiguration;
			try {
				hasConfiguration = TLServiceUtils.hasServiceConfiguration(module);
			} catch (TopLogicException ex) {
				// The error is reported when displaying the configuration.
				hasConfiguration = false;
			}
			if (hasConfiguration) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}

		return ExecutableState.NO_EXEC_NOT_SUPPORTED;
	}

}

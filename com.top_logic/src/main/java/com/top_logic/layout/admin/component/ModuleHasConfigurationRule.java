/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

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

			if (hasServiceConfig(module)) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}

		return ExecutableState.NO_EXEC_NOT_SUPPORTED;
	}

	private boolean hasServiceConfig(BasicRuntimeModule<?> module) {
		try {
			return ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation()) != null;
		} catch (ConfigurationException exception) {
			return false;
		}
	}

}

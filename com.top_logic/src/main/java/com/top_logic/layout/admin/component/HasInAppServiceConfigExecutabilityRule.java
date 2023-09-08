/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.File;
import java.util.Map;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Checks if a custom in app configuration exists in {@link ModuleLayoutConstants#AUTOCONF_PATH} for
 * a given service.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class HasInAppServiceConfigExecutabilityRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof BasicRuntimeModule) {
			File inappServiceConfig =
				TLSaveServiceConfigHandler.inAppServiceConfiguration((BasicRuntimeModule<?>) model);

			if (inappServiceConfig.exists()) {
				return ExecutableState.EXECUTABLE;
			}
		}

		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}

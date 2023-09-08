/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Checks whether a {@link BasicRuntimeModule} service is in app editable.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceInAppExecutabilityRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof BasicRuntimeModule) {
			InApp annotation = getInAppAnnotation(model);

			if (annotation != null) {
				if (!annotation.value()) {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
			}
		}

		return ExecutableState.EXECUTABLE;
	}

	private InApp getInAppAnnotation(Object model) {
		Class<? extends ManagedClass> moduleImplementation = getModuleImplementation((BasicRuntimeModule<?>) model);

		return moduleImplementation.getAnnotation(InApp.class);
	}

	private Class<? extends ManagedClass> getModuleImplementation(BasicRuntimeModule<?> runtimeModule) {
		return runtimeModule.getImplementation();
	}

}

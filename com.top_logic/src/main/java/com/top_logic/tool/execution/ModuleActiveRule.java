/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that returns the {@link ExecutableState} depending on the
 * {@link BasicRuntimeModule#isActive() active} state of a given {@link BasicRuntimeModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleActiveRule<T extends BasicRuntimeModule<?>> implements ExecutabilityRule {

	/**
	 * The {@link BasicRuntimeModule} whose {@link BasicRuntimeModule#isActive() active} state is
	 * checked.
	 */
	protected final T _module;

	/**
	 * Creates a new {@link ModuleActiveRule}.
	 */
	public ModuleActiveRule(T module) {
		_module = module;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (_module.isActive()) {
			return moduleActiveState(_module);
		} else {
			return moduleInactiveState(_module);
		}
	}

	/**
	 * Returns the {@link ExecutableState} when the given {@link BasicRuntimeModule} is active.
	 * 
	 * @param module
	 *        The active {@link BasicRuntimeModule}.
	 * @return Actual result of {@link #isExecutable(LayoutComponent, Object, Map)}.
	 * 
	 * @see #moduleInactiveState(BasicRuntimeModule)
	 */
	protected ExecutableState moduleActiveState(T module) {
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Returns the {@link ExecutableState} when the given {@link BasicRuntimeModule} is not active.
	 * 
	 * @param module
	 *        The inactive {@link BasicRuntimeModule}.
	 * @return Actual result of {@link #isExecutable(LayoutComponent, Object, Map)}.
	 * 
	 * @see #moduleActiveState(BasicRuntimeModule)
	 */
	protected ExecutableState moduleInactiveState(T module) {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}


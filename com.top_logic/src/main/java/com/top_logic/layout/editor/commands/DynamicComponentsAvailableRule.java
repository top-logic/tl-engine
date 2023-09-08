/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.DynamicComponentService.Module;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.ModuleActiveRule;

/**
 * {@link ExecutabilityRule} that hides a command, if {@link DynamicComponentService} has no
 * components to insert.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicComponentsAvailableRule extends ModuleActiveRule<DynamicComponentService.Module> {

	/** Singleton {@link DynamicComponentsAvailableRule} instance. */
	public static final DynamicComponentsAvailableRule INSTANCE = new DynamicComponentsAvailableRule();

	private DynamicComponentsAvailableRule() {
		super(DynamicComponentService.Module.INSTANCE);
	}

	@Override
	protected ExecutableState moduleActiveState(Module module) {
		if (_module.getImplementationInstance().getComponentDefinitions().isEmpty()) {
			// treat as if the module were inactive.
			return moduleInactiveState(module);
		} else {
			return super.moduleActiveState(module);
		}
	}

}
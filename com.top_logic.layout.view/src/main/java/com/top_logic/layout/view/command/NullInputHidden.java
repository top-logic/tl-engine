/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that hides the command when the input is {@code null}.
 *
 * <p>
 * Returns {@link ExecutableState#NOT_EXEC_HIDDEN} for {@code null} input and
 * {@link ExecutableState#EXECUTABLE} otherwise.
 * </p>
 */
public class NullInputHidden implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link NullInputHidden}.
	 */
	@TagName("NullInputHidden")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(NullInputHidden.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/** Singleton instance. */
	public static final NullInputHidden INSTANCE = new NullInputHidden();

	/**
	 * Creates a new {@link NullInputHidden}.
	 */
	private NullInputHidden() {
		// Singleton.
	}

	/**
	 * Creates a new {@link NullInputHidden} from configuration.
	 */
	@CalledByReflection
	public NullInputHidden(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (input == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}
}

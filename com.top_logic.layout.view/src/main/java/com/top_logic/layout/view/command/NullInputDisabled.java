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
 * {@link ViewExecutabilityRule} that disables the command when the input is {@code null}.
 *
 * <p>
 * Returns <code>NO_EXEC_NO_MODEL</code> for {@code null} input and <code>EXECUTABLE</code>
 * otherwise.
 *
 * @see ExecutableState#NO_EXEC_NO_MODEL
 * @see ExecutableState#EXECUTABLE
 * </p>
 */
public class NullInputDisabled implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link NullInputDisabled}.
	 */
	@TagName("null-input-disabled")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(NullInputDisabled.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/** Singleton instance. */
	public static final NullInputDisabled INSTANCE = new NullInputDisabled();

	/**
	 * Creates a new {@link NullInputDisabled}.
	 */
	private NullInputDisabled() {
		// Singleton.
	}

	/**
	 * Creates a new {@link NullInputDisabled} from configuration.
	 */
	@CalledByReflection
	public NullInputDisabled(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (input == null) {
			return ExecutableState.NO_EXEC_NO_MODEL;
		}
		return ExecutableState.EXECUTABLE;
	}
}

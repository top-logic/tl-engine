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
 * {@link ViewExecutabilityRule} that disables the command while an input is {@code null}.
 *
 * <p>
 * Returns <code>NO_EXEC_NO_MODEL</code> when the main argument - or one of the arguments named in
 * {@link NullArgumentRule.Config#getInputs() inputs} - is {@code null}, and <code>EXECUTABLE</code>
 * otherwise.
 * </p>
 *
 * @see ExecutableState#NO_EXEC_NO_MODEL
 * @see ExecutableState#EXECUTABLE
 */
public class NullInputDisabled extends NullArgumentRule {

	/**
	 * Configuration for {@link NullInputDisabled}.
	 */
	@TagName("null-input-disabled")
	public interface Config extends NullArgumentRule.Config {

		@Override
		@ClassDefault(NullInputDisabled.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/** Singleton instance, checking the main argument. */
	public static final NullInputDisabled INSTANCE = new NullInputDisabled();

	/**
	 * Creates a new {@link NullInputDisabled}.
	 */
	private NullInputDisabled() {
		super(ExecutableState.NO_EXEC_NO_MODEL);
	}

	/**
	 * Creates a new {@link NullInputDisabled} from configuration.
	 */
	@CalledByReflection
	public NullInputDisabled(InstantiationContext context, Config config) {
		super(context, config, ExecutableState.NO_EXEC_NO_MODEL);
	}
}

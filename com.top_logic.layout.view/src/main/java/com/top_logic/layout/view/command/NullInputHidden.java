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
 * {@link ViewExecutabilityRule} that hides the command while an input is {@code null}.
 *
 * <p>
 * Returns <code>NOT_EXEC_HIDDEN</code> when the main argument - or one of the arguments named in
 * {@link NullArgumentRule.Config#getInputs() inputs} - is {@code null}, and <code>EXECUTABLE</code>
 * otherwise.
 * </p>
 *
 * @see ExecutableState#NOT_EXEC_HIDDEN
 * @see ExecutableState#EXECUTABLE
 */
public class NullInputHidden extends NullArgumentRule {

	/**
	 * Configuration for {@link NullInputHidden}.
	 */
	@TagName("null-input-hidden")
	public interface Config extends NullArgumentRule.Config {

		@Override
		@ClassDefault(NullInputHidden.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/** Singleton instance, checking the main argument. */
	public static final NullInputHidden INSTANCE = new NullInputHidden();

	/**
	 * Creates a new {@link NullInputHidden}.
	 */
	private NullInputHidden() {
		super(ExecutableState.NOT_EXEC_HIDDEN);
	}

	/**
	 * Creates a new {@link NullInputHidden} from configuration.
	 */
	@CalledByReflection
	public NullInputHidden(InstantiationContext context, Config config) {
		super(context, config, ExecutableState.NOT_EXEC_HIDDEN);
	}
}

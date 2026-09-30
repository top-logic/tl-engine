/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * {@link ViewExecutabilityRule} that shows a command only for the anonymous user (e.g. a "Login"
 * action), hiding it once authenticated.
 */
public class AnonymousOnly implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link AnonymousOnly}.
	 */
	@TagName("anonymous-only")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(AnonymousOnly.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Creates a new {@link AnonymousOnly} from configuration.
	 */
	@CalledByReflection
	public AnonymousOnly(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return TLContext.isAnonymous() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}
}

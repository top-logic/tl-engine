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
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;
import com.top_logic.util.TLContext;

/**
 * {@link ViewExecutabilityRule} that shows a command only for an authenticated (non-anonymous) user
 * (e.g. a "Logout" action), hiding it for the anonymous user.
 *
 * @implNote The hidden state {@link #LOGIN_REQUIRED} gives the missing login as its reason, so that
 *           an attempt to run the command anyway tells the user to log in.
 */
public class AuthenticatedOnly implements ViewExecutabilityRule {

	/**
	 * The state of a command for the anonymous user: hidden, because it requires a login.
	 */
	public static final ExecutableState LOGIN_REQUIRED =
		new ExecutableState(CommandVisibility.HIDDEN, I18NConstants.ERROR_LOGIN_REQUIRED);

	/**
	 * Configuration for {@link AuthenticatedOnly}.
	 */
	@TagName("authenticated-only")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(AuthenticatedOnly.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Creates a new {@link AuthenticatedOnly} from configuration.
	 */
	@CalledByReflection
	public AuthenticatedOnly(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return TLContext.isAnonymous() ? LOGIN_REQUIRED : ExecutableState.EXECUTABLE;
	}
}

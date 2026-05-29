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
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that switches the session back to the anonymous user.
 *
 * <p>
 * The actual session swap is deferred to the reload handled by {@code ViewServlet} via
 * {@link PendingSessionAction}.
 * </p>
 */
public class LogoutCommand implements ViewCommand {

	/**
	 * Configuration for {@link LogoutCommand}.
	 */
	@TagName("logout-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(LogoutCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link LogoutCommand} from configuration.
	 */
	@CalledByReflection
	public LogoutCommand(InstantiationContext context, Config config) {
		// No additional configuration.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		PendingSessionAction.requestLogout(displayContext.asRequest().getSession());
		context.getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		return HandlerResult.DEFAULT_RESULT;
	}
}

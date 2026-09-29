/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that switches the active UI theme.
 *
 * <p>
 * Persists the selected theme as the user's preference and instantly applies it on the client by
 * setting the {@code data-theme} attribute on the document element - no page reload.
 * </p>
 */
public class SetThemeCommand implements ViewCommand {

	/**
	 * Configuration for {@link SetThemeCommand}.
	 */
	@TagName("set-theme")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getTheme()}. */
		String THEME = "theme";

		@Override
		@ClassDefault(SetThemeCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * Id of the theme to activate.
		 */
		@Name(THEME)
		@Mandatory
		String getTheme();

	}

	private final String _theme;

	/**
	 * Creates a {@link SetThemeCommand} from configuration.
	 */
	@CalledByReflection
	public SetThemeCommand(InstantiationContext context, Config config) {
		_theme = config.getTheme();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		UIThemeService.getInstance().setActiveThemeId(_theme);

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create()
				.setCode("document.documentElement.setAttribute('data-theme', '" + _theme + "');"));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}

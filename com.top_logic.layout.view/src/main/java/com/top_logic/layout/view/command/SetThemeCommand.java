/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that puts a UI theme into effect.
 *
 * <p>
 * Stores the theme as the user's preference and applies it on the client instantly, without a page
 * reload. A command naming no theme drops the preference, which leaves the user's pages following
 * the appearance preference of the operating system.
 * </p>
 */
@InApp
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
		 *
		 * <p>
		 * Without a theme, the command drops the theme the user has selected, so that the user's
		 * pages follow the appearance preference of the operating system.
		 * </p>
		 */
		@Name(THEME)
		@Nullable
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
		return applyTheme(context, _theme);
	}

	/**
	 * Stores the given theme as the current user's preference and applies it on the client.
	 *
	 * @param context
	 *        The context whose update queue carries the change to the browser.
	 * @param themeId
	 *        Id of the theme to activate, or {@code null} to drop the user's preference and follow
	 *        the appearance preference of the operating system.
	 * @return The result of the activation.
	 */
	public static HandlerResult applyTheme(ReactContext context, String themeId) {
		UIThemeService.getInstance().setSelectedThemeId(themeId);

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode(applyCode(themeId)));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private static String applyCode(String themeId) {
		StringBuilder code = new StringBuilder();
		code.append("window.");
		code.append(UIThemeService.CLIENT_API);
		code.append(".");
		if (themeId == null) {
			code.append(UIThemeService.FOLLOW_SYSTEM_FUNCTION);
			code.append("();");
		} else {
			code.append(UIThemeService.SELECT_FUNCTION);
			code.append("(");
			TagUtil.writeJsString(code, themeId);
			code.append(");");
		}
		return code.toString();
	}

}

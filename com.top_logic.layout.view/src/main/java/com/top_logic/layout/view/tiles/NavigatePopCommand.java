/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that pops the topmost frame from the enclosing
 * {@link TileStackScope tile stack}.
 *
 * <p>
 * Resolves the target stack via {@link ViewContext#getTileStackScope()} - typically used by a
 * "Back" button inside a pushed frame. For breadcrumb-driven navigation, prefer letting the
 * {@link TileBreadcrumbElement &lt;tile-breadcrumb&gt;} write directly to the path channel.
 * </p>
 */
public class NavigatePopCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePopCommand}.
	 */
	@TagName("navigate-pop")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(NavigatePopCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link NavigatePopCommand}.
	 */
	@CalledByReflection
	public NavigatePopCommand(InstantiationContext context, Config config) {
		// No instance configuration.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<navigate-pop> requires a ViewContext, got " + context.getClass().getName());
		}
		TileStackScope scope = viewContext.getTileStackScope();
		if (scope == null) {
			throw new IllegalStateException(
				"<navigate-pop> executed outside of any enclosing <tile-stack>.");
		}
		scope.pop();
		return HandlerResult.DEFAULT_RESULT;
	}
}

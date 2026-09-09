/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.annotation.InApp;
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
 * Resolves the target stack from the {@link ViewContext} - typically used by a "Back" button inside
 * a pushed frame. For breadcrumb-driven navigation, prefer letting the
 * {@link TileBreadcrumbElement &lt;tile-breadcrumb&gt;} write directly to the path channel. To pop
 * as one step of a longer command, use {@link NavigatePopAction the action of the same tag} inside
 * a {@link com.top_logic.layout.view.command.GenericViewCommand &lt;generic-command&gt;}.
 * </p>
 *
 * @implNote Delegates to {@link NavigatePopAction}.
 */
@InApp
public class NavigatePopCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePopCommand}.
	 */
	@TagName(NavigatePopAction.Config.TAG_NAME)
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(NavigatePopCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	private final NavigatePopAction _action;

	/**
	 * Creates a new {@link NavigatePopCommand}.
	 */
	@CalledByReflection
	public NavigatePopCommand(InstantiationContext context, Config config) {
		_action = new NavigatePopAction();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}

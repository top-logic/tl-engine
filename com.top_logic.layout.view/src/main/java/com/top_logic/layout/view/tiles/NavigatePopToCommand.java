/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that truncates the {@link TileStackScope tile stack} path to a fixed depth.
 *
 * <p>
 * After execution exactly {@link Config#getDepth()} frames remain on the stack. {@code depth=0}
 * empties the stack (the {@link TileStackElement.Config#getInitial() initial} view is shown
 * again). Values larger than the current depth are no-ops. To truncate as one step of a longer
 * command, use {@link NavigatePopToAction the action of the same tag} inside a
 * {@link com.top_logic.layout.view.command.GenericViewCommand &lt;generic-command&gt;}.
 * </p>
 *
 * @implNote Delegates to {@link NavigatePopToAction}.
 */
@InApp
public class NavigatePopToCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePopToCommand}.
	 */
	@TagName(NavigatePopToAction.Config.TAG_NAME)
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(NavigatePopToCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * Number of frames to keep on the stack after the command runs.
		 */
		@Name(NavigatePopToAction.Config.DEPTH)
		@Mandatory
		int getDepth();
	}

	private final NavigatePopToAction _action;

	/**
	 * Creates a new {@link NavigatePopToCommand}.
	 */
	@CalledByReflection
	public NavigatePopToCommand(InstantiationContext context, Config config) {
		_action = new NavigatePopToAction(config.getDepth());
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		_action.execute(context, input);
		return HandlerResult.DEFAULT_RESULT;
	}
}

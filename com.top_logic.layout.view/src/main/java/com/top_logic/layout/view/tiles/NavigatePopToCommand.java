/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that truncates the {@link TileStackScope tile stack} path to a fixed depth.
 *
 * <p>
 * After execution exactly {@link Config#getDepth()} frames remain on the stack. {@code depth=0}
 * empties the stack (the {@link TileStackElement.Config#getInitial() initial} view is shown
 * again). Values larger than the current depth are no-ops.
 * </p>
 */
public class NavigatePopToCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePopToCommand}.
	 */
	@TagName("navigate-pop-to")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(NavigatePopToCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getDepth()}. */
		String DEPTH = "depth";

		/**
		 * Number of frames to keep on the stack after the command runs.
		 */
		@Name(DEPTH)
		@Mandatory
		int getDepth();
	}

	private final int _depth;

	/**
	 * Creates a new {@link NavigatePopToCommand}.
	 */
	@CalledByReflection
	public NavigatePopToCommand(InstantiationContext context, Config config) {
		_depth = config.getDepth();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<navigate-pop-to> requires a ViewContext, got " + context.getClass().getName());
		}
		TileStackScope scope = viewContext.getTileStackScope();
		if (scope == null) {
			throw new IllegalStateException(
				"<navigate-pop-to> executed outside of any enclosing <tile-stack>.");
		}
		scope.popTo(_depth);
		return HandlerResult.DEFAULT_RESULT;
	}
}

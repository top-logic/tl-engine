/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} that truncates the enclosing {@link TileStackScope tile stack} to a fixed
 * depth.
 *
 * <p>
 * After execution exactly {@link Config#getDepth()} frames remain on the stack. {@code depth=0}
 * empties the stack (the {@link TileStackElement.Config#getInitial() initial} view is shown
 * again). Values larger than the current depth are no-ops.
 * </p>
 *
 * <p>
 * Passes the input through as output, so an action following it in the chain still sees the object
 * the chain works on.
 * </p>
 */
@InApp
public class NavigatePopToAction implements ViewAction {

	/**
	 * Configuration for {@link NavigatePopToAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<NavigatePopToAction> {

		/** Configuration tag of a {@link NavigatePopToAction}. */
		String TAG_NAME = "navigate-pop-to";

		/** Configuration name for {@link #getDepth()}. */
		String DEPTH = "depth";

		@Override
		@ClassDefault(NavigatePopToAction.class)
		Class<? extends NavigatePopToAction> getImplementationClass();

		/**
		 * Number of frames to keep on the stack.
		 */
		@Name(DEPTH)
		@Mandatory
		int getDepth();
	}

	private final int _depth;

	/**
	 * Creates a new {@link NavigatePopToAction} from configuration.
	 */
	@CalledByReflection
	public NavigatePopToAction(InstantiationContext context, Config config) {
		this(config.getDepth());
	}

	/**
	 * Creates a new {@link NavigatePopToAction}.
	 *
	 * @param depth
	 *        Number of frames to keep on the stack.
	 */
	public NavigatePopToAction(int depth) {
		_depth = depth;
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		TileStackScope.lookup(context, Config.TAG_NAME).popTo(_depth);
		return input;
	}
}

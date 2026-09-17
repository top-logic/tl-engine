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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} that pops the topmost frame from the enclosing {@link TileStackScope tile
 * stack}.
 *
 * <p>
 * Passes the input through as output, so an action following it in the chain still sees the object
 * the chain works on - which is what makes "delete the object, then leave the frame that displayed
 * it" one command.
 * </p>
 */
@InApp
public class NavigatePopAction implements ViewAction {

	/**
	 * Configuration for {@link NavigatePopAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<NavigatePopAction> {

		/** Configuration tag of a {@link NavigatePopAction}. */
		String TAG_NAME = "navigate-pop";

		@Override
		@ClassDefault(NavigatePopAction.class)
		Class<? extends NavigatePopAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link NavigatePopAction} from configuration.
	 */
	@CalledByReflection
	public NavigatePopAction(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Creates a new {@link NavigatePopAction}.
	 */
	public NavigatePopAction() {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		TileStackScope.lookup(context, Config.TAG_NAME).pop();
		return input;
	}
}

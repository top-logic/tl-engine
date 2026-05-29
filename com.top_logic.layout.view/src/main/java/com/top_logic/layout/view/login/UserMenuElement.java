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
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} that renders a {@link UserMenuControl} showing the current user with login/logout
 * actions.
 *
 * <p>
 * Configured as {@code <user-menu/>}, typically inside an {@code <app-bar>}.
 * </p>
 */
public class UserMenuElement implements UIElement {

	/**
	 * Configuration for {@link UserMenuElement}.
	 */
	@TagName("user-menu")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(UserMenuElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link UserMenuElement} from configuration.
	 */
	@CalledByReflection
	public UserMenuElement(InstantiationContext context, Config config) {
		// No configuration options.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new UserMenuControl(context);
	}

}

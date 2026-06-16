/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.login.LoginCommandsConfig;

/**
 * {@link UIElement} that renders the extra login-dialog commands contributed via
 * {@link LoginCommandsConfig} (e.g. a self-service "Forgot password?" button or SSO providers).
 *
 * <p>
 * Unlike {@code <stack>}, the children are not configured inline but taken from the application
 * configuration, so optional feature modules can contribute buttons without the base view module
 * depending on them. Renders nothing when no command is contributed.
 * </p>
 */
public class LoginCommandsElement implements UIElement {

	/**
	 * Configuration for {@link LoginCommandsElement}.
	 */
	@TagName("login-commands")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(LoginCommandsElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	private final List<UIElement> _commands;

	/**
	 * Creates a new {@link LoginCommandsElement} from configuration.
	 */
	@CalledByReflection
	public LoginCommandsElement(InstantiationContext context, Config config) {
		_commands = ApplicationConfig.getInstance().getConfig(LoginCommandsConfig.class).getCommands().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> children = new ArrayList<>(_commands.size());
		for (int i = 0; i < _commands.size(); i++) {
			ViewContext childContext = context.withChildSlotPath(Integer.toString(i));
			children.add((ReactControl) _commands.get(i).createControl(childContext));
		}
		return new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false, children);
	}

}

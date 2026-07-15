/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.UIElement;

/**
 * Application-configured registry of extra commands contributed to the React login dialog.
 *
 * <p>
 * The login view renders these through the {@code <login-commands>} element
 * ({@link com.top_logic.layout.view.element.LoginCommandsElement}). Optional feature modules (e.g.
 * self-service password reset, SSO providers) contribute their buttons here via an application
 * configuration fragment, without the base view module depending on them.
 * </p>
 *
 * @see com.top_logic.layout.view.element.LoginCommandsElement
 */
public interface LoginCommandsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getCommands()}. */
	String COMMANDS = "commands";

	/**
	 * The contributed login-dialog elements (typically {@code <button>}s opening a dialog).
	 */
	@Name(COMMANDS)
	@DefaultContainer
	List<PolymorphicConfiguration<? extends UIElement>> getCommands();
}

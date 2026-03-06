/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.mig.html.layout;

import com.top_logic.layout.DisplayContext;

/**
 * Plugin to execute some action when a user logged in.
 * 
 * @implNote The plugin is executed when the {@link MainLayout} is initialized.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LoginHook {

	/**
	 * Handles the login of the user.
	 * 
	 * <p>
	 * When the hook is executed, the {@link MainLayout} is already initialized.
	 * </p>
	 * 
	 * @param callback
	 *        The callback to call when the login hook was processed. The callback must be executed
	 *        when this login hook was completely processed to ensure that the remaining
	 *        {@link LoginHook} are executed.
	 */
	void handleLogin(DisplayContext context, Runnable callback);

}


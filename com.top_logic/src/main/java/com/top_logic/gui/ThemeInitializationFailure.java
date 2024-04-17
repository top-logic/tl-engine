/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * Failure thrown when a theme cannot be initialized.
 */
public class ThemeInitializationFailure extends I18NException {

	/**
	 * Creates a {@link ThemeInitializationFailure}.
	 */
	public ThemeInitializationFailure(ResKey message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link ThemeInitializationFailure}.
	 */
	public ThemeInitializationFailure(ResKey message) {
		super(message);
	}

}

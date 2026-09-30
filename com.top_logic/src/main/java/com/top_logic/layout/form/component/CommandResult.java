/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Directly forwards the command result as input to a UI action.
 */
@InApp
public class CommandResult implements ValueTransformation {

	/**
	 * Singleton {@link CommandResult} instance.
	 */
	public static final CommandResult INSTANCE = new CommandResult();

	private CommandResult() {
		// Singleton constructor.
	}

	@Override
	public Object transform(LayoutComponent component, Object model) {
		return model;
	}

}

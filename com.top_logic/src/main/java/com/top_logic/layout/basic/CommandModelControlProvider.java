/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} creating {@link ButtonControl} for {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandModelControlProvider implements ControlProvider {

	/** Singleton {@link CommandModelControlProvider} instance. */
	public static final CommandModelControlProvider INSTANCE = new CommandModelControlProvider();

	/**
	 * Creates a new {@link CommandModelControlProvider}.
	 */
	protected CommandModelControlProvider() {
		// singleton instance
	}

	@Override
	public Control createControl(Object model, String style) {
		if (!(model instanceof CommandModel)) {
			return null;
		}
		return new ButtonControl((CommandModel) model);
	}

}

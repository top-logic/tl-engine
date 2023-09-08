/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;

/**
 * The state of the command is executable if and only if the field has a value.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NonEmptyFieldCommandExecutabilityListener extends CommandExecutabiltyFieldListener {

	/**
	 * Create a {@link NonEmptyFieldCommandExecutabilityListener} for the given command.
	 */
	public NonEmptyFieldCommandExecutabilityListener(CommandModel command, ResKey notExecutableReason) {
		super(command, notExecutableReason);
	}

	@Override
	protected boolean commandIsExecutable(FormField field, Object oldValue, Object newValue) {
		return newValue != null;
	}

}

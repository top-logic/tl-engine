/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;

/**
 * Change the executable state of the command dependent on the content of the field.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class CommandExecutabiltyFieldListener implements ValueListener {

	private final CommandModel _command;

	private final ResKey _notExecutableReason;

	/**
	 * Create a {@link CommandExecutabiltyFieldListener} for the given command.
	 */
	public CommandExecutabiltyFieldListener(CommandModel command, ResKey notExecutableReason) {
		_command = command;
		_notExecutableReason = notExecutableReason;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (commandIsExecutable(field, oldValue, newValue)) {
			CommandModelUtilities.setExecutable(_command);
		} else {
			CommandModelUtilities.setNonExecutable(_command, _notExecutableReason);
		}
	}

	/**
	 * True if the command is executable with the given value, otherwise false.
	 */
	protected abstract boolean commandIsExecutable(FormField field, Object oldValue, Object newValue);

}

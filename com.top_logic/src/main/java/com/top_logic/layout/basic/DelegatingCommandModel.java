/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} delegating its operative aspect to a simple {@link Command}.
 * 
 * <p>
 * The executability is static (only controlled by the setters {@link #setVisible(boolean)},
 * {@link #setExecutable()}, and {@link #setNotExecutable(ResKey)}.
 * </p>
 * 
 * @see DynamicDelegatingCommandModel
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DelegatingCommandModel extends AbstractCommandModel implements WrappedCommandModel {

	private final Command _command;

	/**
	 * Creates a new {@link DelegatingCommandModel}.
	 * 
	 * @param command
	 *        The {@link Command} to execute by this {@link CommandModel}.
	 * 
	 * @see CommandModelFactory#commandModel(Command)
	 */
	protected DelegatingCommandModel(Command command) {
		_command = command;
	}

	@Override
	public Command unwrap() {
		return _command;
	}

	@Override
	protected void appendToStringValues(StringBuilder toStringBuilder) {
		super.appendToStringValues(toStringBuilder);
		toStringBuilder.append("_executable:").append(_command).append(',');
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		return _command.executeCommand(context);
	}

}

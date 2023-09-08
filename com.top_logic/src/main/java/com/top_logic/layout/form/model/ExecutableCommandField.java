/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandBuilder;
import com.top_logic.layout.basic.WrappedCommandModel;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandField} based on an executable {@link Command}.
 * 
 * @see #getExecutable()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ExecutableCommandField extends CommandField implements WrappedCommandModel {

	private Command _command;

	/**
	 * @see FormFactory#newCommandField(String, Command)
	 */
	ExecutableCommandField(String name, Command command, ExecutabilityModel executability, CheckScope checkScope) {
		super(name, executability(executability, command), checkScope);
		_command = command;
	}

	/**
	 * @see FormFactory#newCommandField(String, CommandBuilder)
	 */
	ExecutableCommandField(String name, CommandBuilder commandBuilder, ExecutabilityModel executability,
			CheckScope checkScope) {
		super(name, executability(executability, null), checkScope);
		_command = commandBuilder.build(this);
		if (executability == null) {
			initExecutabilityDelegate(fallbackExecutability(_command));
		}
	}

	private static ExecutabilityModel executability(ExecutabilityModel executability, Command fallback) {
		if (executability != null) {
			return executability;
		}
		return fallbackExecutability(fallback);
	}

	private static ExecutabilityModel fallbackExecutability(Command fallback) {
		if (fallback instanceof ExecutabilityModel) {
			return (ExecutabilityModel) fallback;
		} else {
			return ConstantExecutabilityModel.ALWAYS_EXECUTABLE;
		}
	}

	@Override
	public Command unwrap() {
		return _command;
	}

	/**
	 * Executes the command of this {@link ExecutableCommandField} using the arguments and the
	 * {@link LayoutComponent} given in the constructor, when {@link #isExecutable()} and
	 * {@link #isVisible()}.
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		if (isExecutable() && isVisible()) {
			return _command.executeCommand(aContext);
		}
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(COMMAND_NOT_EXECUTABLE_ERROR_KEY);
		Logger.error("Command field '" + getQualifiedName() + "' not executable.", ExecutableCommandField.class);
		return result;
	}

	/**
	 * This method returns the executable which is used to execute this {@link ExecutableCommandField}.
	 * 
	 * @return never <code>null</code>.
	 */
	public Command getExecutable() {
		return this._command;
	}

	/**
	 * This method sets the {@link Command} to dispatch to, if this {@link ExecutableCommandField} will be
	 * executed
	 * 
	 * @param executable
	 *        must not be <code>null</code>.
	 */
	public void setExecutable(Command executable) {
		if (executable == null) {
			throw new IllegalArgumentException("'executable' must not be 'null'.");
		}
		this._command = executable;
	}
}

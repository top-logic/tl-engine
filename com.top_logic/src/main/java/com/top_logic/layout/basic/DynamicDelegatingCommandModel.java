/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.ConstantExecutabilityModel;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link DynamicCommandModel} delegating its operative aspect to a simple {@link Command}.
 * 
 * @see DelegatingCommandModel CommandModel for Executable that have constant executability.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicDelegatingCommandModel extends DynamicCommandModel implements WrappedCommandModel {

	/**
	 * Delegate for execution.
	 */
	private final Command _command;

	/**
	 * Creates a {@link DynamicDelegatingCommandModel}.
	 * 
	 * @param command
	 *        The {@link Command} executing the command.
	 * @param executability
	 *        The model handling executability.
	 * 
	 * @see CommandModelFactory#commandModel(Command, ExecutabilityModel)
	 */
	protected DynamicDelegatingCommandModel(Command command, ExecutabilityModel executability) {
		super(executability);
		_command = command;
	}

	/**
	 * Constructor for subclasses that uses {@link ComponentCommand}. Such an
	 * {@link ComponentCommand} is both, {@link Command} and {@link ExecutabilityModel} but
	 * should only created once.
	 */
	protected DynamicDelegatingCommandModel(CommandBuilder builder) {
		super(ConstantExecutabilityModel.ALWAYS_EXECUTABLE);
		_command = builder.build(this);
		if (_command instanceof ExecutabilityModel) {
			initExecutability((ExecutabilityModel) _command);
		}
	}

	@Override
	public Command unwrap() {
		return _command;
	}

	/**
	 * Executes the command of this {@link CommandModel} using the given {@link Command}.
	 * 
	 * @see Command#executeCommand(DisplayContext)
	 */
	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext aContext) {
		if (isExecutable() && isVisible()) {
			return _command.executeCommand(aContext);
		}
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(COMMAND_NOT_EXECUTABLE_ERROR_KEY);
		Logger.error("Not executable: " + getLabel(), DynamicDelegatingCommandModel.class);
		return result;
	}

	@Override
	protected void appendToStringValues(StringBuilder toStringBuilder) {
		super.appendToStringValues(toStringBuilder);
		toStringBuilder.append("executable:").append(_command).append(',');
	}

}


/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link CommandModel} executing a {@link ControlCommand} on a given {@link Control}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ControlCommandModel extends AbstractCommandModel {

	static final String COMMAND_MODEL_KEY = ComponentCommand.COMMAND_MODEL_KEY;

	private final ControlCommand command;
	private final Map<String, Object> arguments;
	private final Control control;

	/**
	 * Creates a {@link ControlCommandModel}. All arguments must not be
	 * <code>null</code>.
	 */
	public ControlCommandModel(ControlCommand aCommand, Control aControl, Map<String, Object> someArguments,
			String aLabel) {
		if (aLabel == null) {
			throw new IllegalArgumentException("'aLabel' must not be 'null'.");
		}
		if (someArguments == null) {
			throw new IllegalArgumentException("'someArguments' must not be 'null'.");
		}
		if (aControl == null) {
			throw new IllegalArgumentException("'aControl' must not be 'null'.");
		}
		if (aCommand == null) {
			throw new IllegalArgumentException("'aCommand' must not be 'null'.");
		}
		this.command = aCommand;
		this.control = aControl;
		this.arguments = someArguments;
		setLabel(aLabel);
	}

	/**
	 * Creates a {@link ControlCommandModel}. All arguments must not be
	 * <code>null</code>. The label of this {@link CommandModel} will be the
	 * {@link AJAXCommandHandler#getDefaultI18NKey() I18N key of the command}.
	 */
	public ControlCommandModel(ControlCommand aCommand, Control aControl, Map<String, Object> someArguments) {
		this(aCommand, aControl, someArguments, getCommandI18N(aCommand, Resources.getInstance()));
	}

	/**
	 * Creates a {@link ControlCommandModel} for a {@link ControlCommand}
	 * which does not need any arguments for the execution. All arguments must not be
	 * <code>null</code>. The label of this {@link CommandModel} will be the
	 * {@link AJAXCommandHandler#getDefaultI18NKey() I18N key of the command}.
	 */
	public ControlCommandModel(ControlCommand aCommand, Control aControl) {
		this(aCommand, aControl, Collections.<String, Object> emptyMap());
	}

	/**
	 * Executing will be dispatched to the execute method of the
	 * {@link ControlCommand} of this {@link ControlCommandModel} in case
	 * {@link #isVisible()} and {@link #isExecutable()}. The arguments and the
	 * control are those which are given in the constructor.
	 * 
	 * @see Command#executeCommand(DisplayContext)
	 */
	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		if (isExecutable() && isVisible()) {
			/* Must build a new map since some ControlCommands change the arguments (original
			 * example of such command has been deleted). */
			Map<String, Object> theArguments = new HashMap<>(arguments);
			theArguments.put(COMMAND_MODEL_KEY, this);
			return command.execute(context, control, theArguments);
		}
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(COMMAND_NOT_EXECUTABLE_ERROR_KEY);
		Logger.error("Not executable: " + getLabel(), ControlCommandModel.class);
		return result;
	}

	@Override
	protected void appendToStringValues(StringBuilder toStringBuilder) {
		super.appendToStringValues(toStringBuilder);
		toStringBuilder.append("controlCommand:").append(command).append(',');
		if (arguments != null && !arguments.isEmpty()) {
			toStringBuilder.append("arguments:").append(arguments).append(',');
		}
	}
}

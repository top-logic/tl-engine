/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import java.text.DateFormat;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommand;

/**
 * Description of a failing business command execution.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandContextDescription implements ContextDescription {
	private LayoutComponent _component;

	private BoundCommand _command;

	private final Throwable _exception;

	/**
	 * Creates a {@link CommandContextDescription}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param command
	 *        See {@link #getCommand()}.
	 * @param exception
	 *        See {@link #getException()}.
	 */
	public CommandContextDescription(LayoutComponent component, BoundCommand command, Throwable exception) {
		_component = component;
		_command = command;
		_exception = exception;
	}

	/**
	 * The component on which the {@link #getCommand()} was executed.
	 */
	public LayoutComponent getComponent() {
		return _component;
	}

	/**
	 * The executed command.
	 * 
	 * @see #getComponent()
	 */
	public BoundCommand getCommand() {
		return _command;
	}

	/**
	 * The problem that happened.
	 * 
	 * @see #getCommand()
	 */
	public Throwable getException() {
		return _exception;
	}

	@Override
	public ResKey getErrorTitle() {
		return I18NConstants.COMMAND_FAILED_TITLE__COMMAND.fill(getCommandLabel());
	}

	@Override
	public ResKey getErrorMessage() {
		DateFormat timestampFormat = DateUtil.getIso8601DateFormat();
		final String timestamp = timestampFormat.format(new Date());

		if (isInternalError()) {
			return I18NConstants.INTERNAL_ERROR__TIMESTAMP.fill(timestamp);
		} else {
			return I18NConstants.COMMAND_FAILED__COMMAND_TIMESTAMP.fill(getCommandLabel(), timestamp);
		}
	}

	@Override
	public void logError() {
		String commandId = getCommandId();
		String logMessage = "Command '" + commandId + "' failed.";
		final Throwable problem = getException();
		if (isInternalError()) {
			Logger.error(logMessage, problem, ErrorHandlingHelper.class);
		} else {
			// For compatibility, even log non-fatal problems.
			if (problem != null) {
				Logger.info(logMessage, problem, ErrorHandlingHelper.class);
			}
		}
	}

	ResKey getCommandLabel() {
		LayoutComponent component = getComponent();
		BoundCommand failedCommand = getCommand();

		ResKey commandKey = component.getResPrefix().key(failedCommand.getID());

		return ResKey.fallback(commandKey, failedCommand.getResourceKey(component));
	}

	String getCommandId() {
		BoundCommand failedCommand = getCommand();
		return failedCommand.getID();
	}

	boolean isInternalError() {
		Throwable problem = getException();
		if (problem == null) {
			return false;
		}

		return ErrorHandlingHelper.isInternalError(problem);
	}

}

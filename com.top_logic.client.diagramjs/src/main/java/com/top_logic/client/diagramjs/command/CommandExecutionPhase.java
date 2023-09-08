/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.command;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Clarifies the execution phase of a command.
 * 
 * <ul>
 * <li>{@link #CAN_EXECUTE}</li>
 * <li>{@link #PRE_EXECUTE}</li>
 * <li>{@link #PRE_EXECUTED}</li>
 * <li>{@link #EXECUTE}</li>
 * <li>{@link #EXECUTED}</li>
 * <li>{@link #POST_EXECUTE}</li>
 * <li>{@link #POST_EXECUTED}</li>
 * <li>{@link #REVERT}</li>
 * <li>{@link #REVERTED}</li>
 * </ul>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public enum CommandExecutionPhase {
	
	/**
	 * Phase before {@link CommandHandler#canExecute(JavaScriptObject)} runs.
	 */
	CAN_EXECUTE("canExecute"),
	
	/**
	 * Phase before {@link CommandHandler#preExecute(JavaScriptObject)} runs.
	 */
	PRE_EXECUTE("preExecute"), 
	
	/**
	 * Phase after {@link CommandHandler#preExecute(JavaScriptObject)} ran.
	 */
	PRE_EXECUTED("preExecuted"),
	
	/**
	 * Phase before {@link CommandHandler#execute(JavaScriptObject)} runs.
	 */
	EXECUTE("execute"), 
	
	/**
	 * Phase after {@link CommandHandler#execute(JavaScriptObject)} ran.
	 */
	EXECUTED("executed"), 
	
	/**
	 * Phase before {@link CommandHandler#postExecute(JavaScriptObject)} runs.
	 */
	POST_EXECUTE("postExecute"), 
	
	/**
	 * Phase after {@link CommandHandler#postExecute(JavaScriptObject)} ran.
	 */
	POST_EXECUTED("postExecuted"), 
	
	/**
	 * Phase before {@link CommandHandler#revert(JavaScriptObject)} runs.
	 */
	REVERT("revert"), 
	
	/**
	 * Phase after {@link CommandHandler#revert(JavaScriptObject)} ran.
	 */
	REVERTED("reverted");
	
	String _executionPhaseName;
	
	CommandExecutionPhase(String executionPhaseName) {
		_executionPhaseName = executionPhaseName;
	}
	
	/**
	 * The diagramJS method name for the given execution phase.
	 */
	public String getExecutionPhaseName() {
		return _executionPhaseName;
	}
	
}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.command;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.ModuleNames;
import com.top_logic.client.diagramjs.event.EventBus;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.event.GeneralEventNames;

/**
 * A service that offers execution of commands.
 * 
 * <p>
 * In the process of a command the command stack fires a number of events that allow other
 * components to participate in the command execution.
 * </p>
 * 
 * <p>
 * The events are emitted in the following order:
 * </p>
 * 
 * <ul>
 * <li>{@link CommandExecutionPhase#CAN_EXECUTE}</li>
 * <li>{@link CommandExecutionPhase#PRE_EXECUTE}</li>
 * <li>{@link CommandExecutionPhase#PRE_EXECUTED}</li>
 * <li>{@link CommandExecutionPhase#EXECUTE}</li>
 * <li>{@link CommandExecutionPhase#EXECUTED}</li>
 * <li>{@link CommandExecutionPhase#POST_EXECUTE}</li>
 * <li>{@link CommandExecutionPhase#POST_EXECUTED}</li>
 * <li>{@link CommandExecutionPhase#REVERT}</li>
 * <li>{@link CommandExecutionPhase#REVERTED}</li>
 * </ul>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CommandStack extends JavaScriptObject {

	/**
	 * Creates the {@link CommandStack}.
	 */
	protected CommandStack() {
		super();
	}

	/**
	 * Register the given {@link CommandHandler} for the given {@code command} name.
	 */
	public final native void register(String command, CommandHandler handler) /*-{
		this.register(command, handler);
	}-*/;

	/**
	 * Executes for the given {@code command} the corresponding {@link CommandHandler} on the given
	 * {@code context}.
	 */
	public final native void execute(String command, JavaScriptObject context) /*-{
		this.execute(command, context);
	}-*/;

	/**
	 * Adds an intercepter for the command execution in the given {@link CommandExecutionPhase}.
	 */
	public final void addCommandInterceptor(String command, CommandExecutionPhase clarifier, EventHandler handler) {
		getEventBus().addEventHandler(getEventName(command, clarifier), handler);
	}

	/**
	 * @see #addCommandInterceptor(String, CommandExecutionPhase, EventHandler)
	 */
	public final void addCommandInterceptor(List<String> commands, CommandExecutionPhase clarifier,
			EventHandler handler) {
		EventBus eventBus = getEventBus();

		for (String command : commands) {
			eventBus.addEventHandler(getEventName(command, clarifier), handler);
		}
	}

	private String getEventName(String command, CommandExecutionPhase clarifier) {
		return getGeneralEventName(command) + GeneralEventNames.EVENT_NAME_SEPARATOR + clarifier.getExecutionPhaseName();
	}

	private String getGeneralEventName(String command) {
		return ModuleNames.COMMAND_STACK_MODULE + GeneralEventNames.EVENT_NAME_SEPARATOR + command;
	}

	private final native EventBus getEventBus() /*-{
		return this._eventBus;
	}-*/;
}

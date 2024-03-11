/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link Command} that a {@link CommandHandler} on a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentCommand extends AbstractDynamicCommand implements CheckScope, CommandBuilder {

	/**
	 * Creates a new {@link ComponentCommand}
	 * 
	 * @param command
	 *        the command which will be executed. must not be <code>null</code>
	 * @param component
	 *        the component which will act as {@link LayoutComponent component}- argument of the
	 *        command. must not be <code>null</code>
	 */
	public static CommandBuilder newInstance(CommandHandler command, LayoutComponent component) {
		return newInstance(command, component, CommandHandler.NO_ARGS);
	}

	/**
	 * Creates a new {@link ComponentCommand}
	 * 
	 * @param command
	 *        the command which will be executed. must not be <code>null</code>
	 * @param component
	 *        the component which will act as {@link LayoutComponent component}- argument of the
	 *        command. must not be <code>null</code>
	 * @param arguments
	 *        the arguments for the command. must not be <code>null</code>
	 */
	public static CommandBuilder newInstance(CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		return new ComponentCommand(command, component, arguments);
	}

	/**
	 * Key which is used to store the {@link CommandModel} in the arguments of the
	 * {@link CommandHandler} before it is executed.
	 * 
	 * @see ComponentCommand#getCommandModel(Map)
	 */
	public static final String COMMAND_MODEL_KEY = "___commandModel___";

	private final Map<String, Object> arguments, unmodifiableArguments;
	private final LayoutComponent component;
	private final CommandHandler command;

	private Object _model;

	private ComponentCommand(CommandHandler command, LayoutComponent component, Map<String, Object> arguments) {
		if (arguments == null) {
			throw new IllegalArgumentException("'someArguments' must not be 'null'.");
		}
		if (component == null) {
			throw new IllegalArgumentException("'component' must not be 'null'.");
		}
		if (command == null) {
			throw new IllegalArgumentException("'command' must not be 'null'.");
		}
		this.command = command;
		this.component = component;
		this.arguments = arguments;
		this.unmodifiableArguments = Collections.unmodifiableMap(this.arguments);
	}

	/**
	 * Executes the command of this {@link ComponentCommand} using the arguments and the
	 * {@link LayoutComponent} given in the constructor. The given model is not considered.
	 * 
	 * @see Command#executeCommand(DisplayContext)
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		/* Does not use the arguments stored in this ButtonModel since some CommandHandler change
		 * the arguments (original example has been deleted). */
		HashMap<String, Object> theArguments = new HashMap<>(arguments);
		theArguments.put(COMMAND_MODEL_KEY, _model);

		HandlerResult result = CommandDispatcher.getInstance().dispatchCommand(command, context, component, theArguments);

		if (result.isSuspended()) {
			// Note: Initialize the continuation early to prevent the actual button click to be
			// resumed. Resuming the concrete button click might not work, because the button might
			// no longer exist, when the resume happens (e.g. if the button was displayed in a popup
			// dialog such as a context menu or burger menu). Instead the underlying component
			// command is resumed. This is possible independently of the concrete button that
			// originally invoked the command.
			result.initContinuation(command, component, theArguments);
		}

		return result;
	}

	@Override
	public Command build(CommandModel model) {
		_model = model;
		return this;
	}

	/**
	 * The {@link LayoutComponent} to execute the {@link #getCommand()} on.
	 */
	public final LayoutComponent getComponent() {
		return this.component;
	}

	/**
	 * The {@link CommandHandler} to execute on the {@link #getComponent()}.
	 */
	public final CommandHandler getCommand() {
		return this.command;
	}

	/**
	 * The arguments to pass to the {@link #getCommand()} when executing.
	 */
	public final Map<String, Object> getArguments() {
		return this.unmodifiableArguments;
	}

	@Override
	public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
		return command.checkScopeProvider().getCheckScope(component).getAffectedFormHandlers();
	}

	@Override
	protected ExecutableState calculateExecutability() {
		return CommandDispatcher.resolveExecutableState(command, getComponent(), getArguments());
	}
	
	@Override
	public String toString() {
		StringBuilder toStringBuilder = new StringBuilder();
		toStringBuilder.append(getClass().getName());
		toStringBuilder.append("[command:");
		toStringBuilder.append(command);
		toStringBuilder.append(",component:");
		toStringBuilder.append(component);
		if (arguments != null && !arguments.isEmpty()) {
			toStringBuilder.append(",arguments:");
			toStringBuilder.append(arguments);
		}
		toStringBuilder.append(']');
		return toStringBuilder.toString();
	}

	/**
	 * Returns the {@link CommandModel} which was added to the arguments when calling
	 * {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * <p>
	 * This method must only be called during
	 * {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}. Otherwise the
	 * result is undefined.
	 * </p>
	 * 
	 * @param someArguments
	 *        Arguments of the {@link CommandHandler} in
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return The {@link CommandModel} executing the {@link #getCommand() command}.
	 */
	public static CommandModel getCommandModel(Map<String, Object> someArguments) {
		return (CommandModel) someArguments.get(COMMAND_MODEL_KEY);
	}
	
}

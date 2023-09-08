/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static com.top_logic.layout.basic.DefaultButtonUIModel.*;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * Factory methods for creating {@link CommandModel}s.
 * 
 * @see ComponentCommandModel
 * @see DelegatingCommandModel
 * @see DynamicDelegatingCommandModel
 * @see DynamicCommandModelAdapter
 * @see AbstractCommandModel
 * @see DynamicCommandModel
 * @see CommandField
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandModelFactory {

	/**
	 * Creates a {@link CommandModel} for a {@link CommandHandler} that does not need any arguments.
	 * 
	 * @see #commandModel(CommandHandler, LayoutComponent, Map)
	 */
	public static ComponentCommandModel commandModel(CommandHandler command, LayoutComponent component) {
		return commandModel(command, Resources.getInstance(), component);
	}

	/**
	 * Creates a {@link CommandModel} for a {@link CommandHandler} that does not need any arguments.
	 * 
	 * @see #commandModel(CommandHandler, Resources, LayoutComponent, Map)
	 */
	public static ComponentCommandModel commandModel(CommandHandler command, Resources res, LayoutComponent component) {
		return commandModel(command, res, component, CommandHandler.NO_ARGS);
	}

	/**
	 * Creates a {@link CommandModel} invoking the given {@link CommandHandler} on the given
	 * {@link LayoutComponent} with the given arguments.
	 * 
	 * @see #commandModel(CommandHandler, Resources, LayoutComponent, Map)
	 */
	public static ComponentCommandModel commandModel(CommandHandler command, LayoutComponent component,
			Map<String, Object> someArguments) {
		return commandModel(command, Resources.getInstance(), component, someArguments);
	}

	/**
	 * Creates a {@link CommandModel} invoking the given {@link CommandHandler} on the given
	 * {@link LayoutComponent} with the given arguments.
	 * 
	 * <p>
	 * The command label will be {@link AJAXCommandHandler#getDefaultI18NKey()}, interpreted by the
	 * given {@link Resources} or {@link StringServices#EMPTY_STRING} if the command does not have
	 * an default key.
	 * </p>
	 * 
	 * @param command
	 *        The {@link CommandHandler} to invoke.
	 * @param res
	 *        The {@link Resources} instance to create the label with.
	 * @param component
	 *        The {@link LayoutComponent} to invoke the given handler on.
	 * @param someArguments
	 *        The arguments to pass to the given {@link CommandHandler}, see
	 *        {@link CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, LayoutComponent, Object, Map)}
	 * 
	 * @see #commandModel(CommandHandler, LayoutComponent, Map, String)
	 */
	public static ComponentCommandModel commandModel(CommandHandler command, Resources res, LayoutComponent component,
			Map<String, Object> someArguments) {
		return commandModel(command, component, someArguments, getCommandI18N(component, command, res));
	}

	/**
	 * Creates a {@link ComponentCommandModel} for a command which does not need any arguments. The
	 * label of this {@link ComponentCommandModel} is the given label.
	 * 
	 * @see #commandModel(CommandHandler, LayoutComponent, Map, String)
	 */
	public static CommandModel commandModel(CommandHandler command, LayoutComponent component, String label) {
		return commandModel(command, component, CommandHandler.NO_ARGS, label);
	}

	/**
	 * Creates a {@link CommandModel} invoking the given {@link CommandHandler} on the given
	 * {@link LayoutComponent} with the given arguments.
	 * 
	 * @param command
	 *        The {@link CommandHandler} to invoke when clicking the button.
	 * @param component
	 *        The component on which the given handler is invoked.
	 * @param someArguments
	 *        The arguments to pass to the given handler, see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param label
	 *        The label of the button.
	 */
	public static ComponentCommandModel commandModel(CommandHandler command, LayoutComponent component,
			Map<String, Object> someArguments, String label) {
		return new ComponentCommandModel(command, component, someArguments, label);
	}

	/**
	 * Creates a {@link CommandModel} delegating to the given {@link Command}.
	 * 
	 * <p>
	 * Note: Instead of calling this method with a newly created anonymous {@link Command} instance,
	 * consider sub-classing {@link AbstractCommandModel} directly. This is especially useful to get
	 * access to the UI aspects of the resulting button from within
	 * {@link Command#executeCommand(DisplayContext)}.
	 * </p>
	 * 
	 * @param command
	 *        The {@link Command} to execute by this {@link CommandModel}.
	 */
	public static CommandModel commandModel(Command command) {
		return new DelegatingCommandModel(command);
	}

	/**
	 * Creates a {@link CommandModel} delegating its execution and executability to the given
	 * {@link DynamicCommand}
	 * 
	 * <p>
	 * Note: Instead of calling this method with a newly created anonymous {@link Command} instance,
	 * consider sub-classing {@link DynamicCommandModel} directly. This is especially useful to get
	 * access to the UI aspects of the resulting button from within
	 * {@link Command#executeCommand(DisplayContext)}.
	 * </p>
	 */
	public static CommandModel commandModel(DynamicCommand dynamicExecutable) {
		return commandModel(dynamicExecutable, dynamicExecutable);
	}

	/**
	 * Creates a {@link CommandModel} delegating its exectution to the given {@link Command} and its
	 * executability to the given {@link ExecutabilityModel}.
	 * 
	 * <p>
	 * Note: Instead of calling this method with a newly created anonymous {@link Command} and
	 * {@link ExecutabilityModel} instance, consider sub-classing {@link DynamicCommandModel}
	 * directly. This is especially useful to get access to the UI aspects of the resulting button
	 * from within {@link Command#executeCommand(DisplayContext)}.
	 * </p>
	 * 
	 * @param executable
	 *        The {@link Command} to be invoked.
	 * @param executability
	 *        The {@link ExecutabilityModel} handling the executability.
	 */
	public static CommandModel commandModel(Command executable, ExecutabilityModel executability) {
		return new DynamicDelegatingCommandModel(executable, executability);
	}

	/**
	 * Creates a {@link CommandModel} delegate using for its executability the given
	 * {@link ExecutabilityModel}.
	 * 
	 * @param delegate
	 *        The {@link CommandModel} to delegate all but executability.
	 * @param executability
	 *        The {@link ExecutabilityModel} to delegate executability to.
	 * 
	 */
	public static CommandModel commandModel(CommandModel delegate, ExecutabilityModel executability) {
		return new DynamicCommandModelAdapter(delegate, executability);
	}

}

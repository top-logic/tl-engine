/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The class {@link CommandCommandHandler} is a {@link CommandHandler} which executes some
 * {@link Command} and contains all necessary informations for it.
 * 
 * @since 5.7.4
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class CommandCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration of the {@link CommandCommandHandler}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends AbstractCommandHandler.Config {

		/** Name of {@link #getCommand()} property. */
		String COMMAND_PROPERTY = "command";

		/**
		 * The {@link Command} to wrap
		 */
		@Name(COMMAND_PROPERTY)
		@Mandatory
		@InstanceFormat
		Command getCommand();

		/**
		 * Setter for {@link #getCommand()}.
		 * 
		 * @param c
		 *        new value of {@link #getCommand()}.
		 */
		void setCommand(Command c);
	}

	/** Holds the command that shall be executed. */
	protected final Command _command;

	/**
	 * Creates a new {@link CommandCommandHandler}.
	 */
	public CommandCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
		_command = config.getCommand();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		return _command.executeCommand(context);
	}

	/**
	 * Creates a {@link CommandCommandHandler} with given {@link Command} and id "command".
	 * 
	 * @see CommandCommandHandler#newHandler(Command, String)
	 */
	public static CommandCommandHandler newHandler(Command command) {
		return newHandler(command, "command");
	}

	/**
	 * Creates a {@link CommandCommandHandler} with given {@link Command}, id and system group.
	 * 
	 * @see CommandCommandHandler#newHandler(Command, String, BoundCommandGroup)
	 */
	public static CommandCommandHandler newHandler(Command command, String commandID) {
		return newHandler(command, commandID, SimpleBoundCommandGroup.SYSTEM);
	}

	/**
	 * Creates a {@link CommandCommandHandler} with given {@link Command}, id and group.
	 */
	public static CommandCommandHandler newHandler(Command command, String commandID, BoundCommandGroup group) {
		Config config = newConfig(command, commandID, group);
		return (CommandCommandHandler) TypedConfigUtil.createInstance(config);
	}

	/**
	 * Creates new configuration for an {@link CommandCommandHandler}.
	 * @param command
	 *        Value of {@link Config#getCommand()}.
	 * @param commandID
	 *        Value of {@link Config#getId()}.
	 * @param group
	 *        Value of {@link Config#getGroup()}.
	 */
	private static Config newConfig(Command command, String commandID, BoundCommandGroup group) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setImplementationClass(CommandCommandHandler.class);

		TypedConfigUtil.setProperty(item, CommandHandler.Config.ID_PROPERTY, commandID);
		AbstractCommandHandler.updateGroup(item, group);
		TypedConfigUtil.setProperty(item, Config.COMMAND_PROPERTY, command);
		return item;
	}

}


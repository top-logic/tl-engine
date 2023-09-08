/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * The class {@link DefaultCommandModelConfiguration} is the default implementation of
 * {@link CommandModelConfiguration}. It is necessary to specify the command by given the
 * commandName (see {@link Config#getCommand()}). It is possible to give the component which will be
 * used to execute the command (see {@link Config#getTargetComponent()}). If no component name is
 * given the command will be executed at the component this {@link CommandModelConfiguration} is
 * configured.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultCommandModelConfiguration<C extends DefaultCommandModelConfiguration.Config<?>>
		extends AbstractCommandModelConfiguration<C> {

	/**
	 * Configuration options for {@link DefaultCommandModelConfiguration}.
	 */
	public interface Config<I extends DefaultCommandModelConfiguration<?>>
			extends AbstractCommandModelConfiguration.Config<I> {

		/** @see #getCommand() */
		String COMMAND_NAME = "command";

		/** @see #getTargetComponent() */
		String COMPONENT_NAME = "targetComponent";

		/**
		 * The name of the command this {@link CommandModelConfiguration} configures. The
		 * corresponding command must be registered at the {@link CommandHandlerFactory}
		 */
		@Name(COMMAND_NAME)
		@Mandatory
		String getCommand();

		/**
		 * The name of the {@link LayoutComponent} which shall be used to execute the configured
		 * command.
		 */
		@Name(COMPONENT_NAME)
		ComponentName getTargetComponent();
	}

	/**
	 * Creates a {@link DefaultCommandModelConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Creates the {@link CommandModel}.
	 * 
	 * <p>
	 * The given {@link LayoutComponent} must not be <code>null</code>. If there is no name for a
	 * component which can be used to execute the configured command, then the given
	 * {@link LayoutComponent} is also the target component.
	 * </p>
	 * 
	 * @return The {@link CommandModel} to display, or <code>null</code> in case of an unresolvable
	 *         command.
	 */
	@Override
	protected CommandModel internalCreateCommandModel(LayoutComponent aLayoutComponent) {
		C config = getConfig();
		String commandName = config.getCommand();
		LayoutComponent target = resolveTargetComponent(aLayoutComponent, config.getTargetComponent());

		CommandHandler handler = target.getCommandById(commandName);
		if (handler == null) {
			handler = CommandHandlerFactory.getInstance().getHandler(commandName);
			if (handler == null) {
				Logger.error("Command '" + commandName + "' does neither exist in component '"
					+ target.getName() + "', nor in command handler factory.",
					DefaultCommandModelConfiguration.class);
				return null;
			}
			target.registerCommand(handler);
		}

		if (getCommandArguments() != null) {
			return CommandModelFactory.commandModel(handler, target, getCommandArguments());
		} else {
			return CommandModelFactory.commandModel(handler, target);
		}
	}

	static LayoutComponent resolveTargetComponent(LayoutComponent baseComponent, ComponentName componentName) {
		if (componentName == null) {
			return baseComponent;
		}

		LayoutComponent target;
		MainLayout mainLayout = baseComponent.getMainLayout();
		// MainLayout does not find itself by name
		if (componentName.equals(mainLayout.getName())) {
			target = mainLayout;
		} else {
			target = mainLayout.getComponentByName(componentName);
		}
		if (target == null) {
			Logger.warn("TargetComponent '" + componentName + "' not found, using default target '"
				+ baseComponent + "' instead.", DefaultCommandModelConfiguration.class);
			target = baseComponent;
		}
		return target;
	}

}

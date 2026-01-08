/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Additional custom commands to add to the context menu.
 */
@Label("Custom commands")
@InApp
public class CustomContextMenuCommands extends AbstractConfiguredInstance<CustomContextMenuCommands.Config<?>>
		implements ContextMenuCommandsProvider {

	/**
	 * Configuration options for {@link CustomContextMenuCommands}.
	 */
	public interface Config<I extends CustomContextMenuCommands> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getCommands()
		 */
		String COMMANDS = "commands";

		/**
		 * @see #isIncludeApplicationConfiguration()
		 */
		String INCLUDE_APPLICATION_CONFIGURATION = "include-application-configuration";

		/**
		 * Whether to add globally configured commands from the application configuration.
		 * 
		 * <p>
		 * If set, the configured commands are added to the commands configured in the application
		 * configuration for the type of the object on which the context menu is opened.
		 * </p>
		 * 
		 * @see LabelProviderService#getContextCommands(Object)
		 */
		@BooleanDefault(true)
		@Name(INCLUDE_APPLICATION_CONFIGURATION)
		boolean isIncludeApplicationConfiguration();

		/**
		 * Custom commands to add to the context menus.
		 */
		@Name(COMMANDS)
		@DefaultContainer
		@Options(fun = InAppImplementations.class)
		@AcceptableClassifiers("context-menu")
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();
	}

	private final List<CommandHandler> _handlers;

	/**
	 * Creates a {@link CustomContextMenuCommands} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomContextMenuCommands(InstantiationContext context, Config<?> config) {
		super(context, config);

		List<ConfigBase<? extends CommandHandler>> commandConfigs = config.getCommands();
		List<CommandHandler> handlers = new ArrayList<>(commandConfigs.size());
		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		for (CommandHandler.ConfigBase<? extends CommandHandler> handlerConfig : commandConfigs) {
			CommandHandler handler = factory.getCommand(context, handlerConfig);
			handlers.add(handler);
		}
		_handlers = handlers;
	}

	@Override
	public boolean hasContextMenuCommands(Object obj) {
		return true;
	}

	@Override
	public List<CommandHandler> getContextCommands(Object obj) {
		if (!getConfig().isIncludeApplicationConfiguration()) {
			return _handlers;
		}

		List<CommandHandler> generalCommands = LabelProviderService.getInstance().getContextCommands(obj);
		return CollectionUtil.concat(generalCommands, _handlers);
	}

}

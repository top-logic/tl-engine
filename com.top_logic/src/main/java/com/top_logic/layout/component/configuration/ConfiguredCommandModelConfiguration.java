/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CommandModelConfiguration} with a configured {@link CommandModel} which does not depend on
 * the concrete {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredCommandModelConfiguration<C extends ConfiguredCommandModelConfiguration.Config<?>>
		extends CommandModelCustomization<C> implements CommandModelConfiguration {
	
	/**
	 * Typed configuration interface definition for {@link ConfiguredCommandModelConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends ConfiguredCommandModelConfiguration<?>>
			extends CommandModelCustomization.Config<I> {

		/** Name of the configuration parameter to configure {@link #getCommandModel()}. */
		String COMMAND_MODEL_NAME = "commandModel";

		/**
		 * Configuration of the actual {@link CommandModel}.
		 */
		@Name(COMMAND_MODEL_NAME)
		@Mandatory
		PolymorphicConfiguration<CommandModel> getCommandModel();

		/**
		 * Setter for {@link #getCommandModel()}.
		 */
		void setCommandModel(PolymorphicConfiguration<CommandModel> model);
	}

	private CommandModel _commandModel;

	/**
	 * Create a {@link ConfiguredCommandModelConfiguration}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ConfiguredCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
		_commandModel = context.getInstance(config.getCommandModel());
		customizeCommandModel(_commandModel);
	}

	@Override
	public CommandModel createCommandModel(LayoutComponent aLayoutComponent) {
		return _commandModel;
	}

}


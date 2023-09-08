/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configurable {@link DelegatingCommandModel} with inner command configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredDelegatingCommandModel extends DelegatingCommandModel
		implements ConfiguredInstance<ConfiguredDelegatingCommandModel.Config> {

	/**
	 * Typed configuration interface definition for {@link ConfiguredDelegatingCommandModel}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ConfiguredDelegatingCommandModel> {

		/** Configuration name of the value of {@link #getCommand()}. */
		String COMMAND_NAME = "command";

		/**
		 * Configuration of the command to dispatch
		 * {@link CommandModel#executeCommand(com.top_logic.layout.DisplayContext) the execution}
		 * to.
		 * 
		 * @see DelegatingCommandModel#unwrap()
		 */
		@Mandatory
		@Name(COMMAND_NAME)
		PolymorphicConfiguration<Command> getCommand();

		/**
		 * Setter for the value of {@link #getCommand()}.
		 */
		void setCommand(PolymorphicConfiguration<Command> command);
	}

	private final Config _config;

	/**
	 * Create a {@link ConfiguredDelegatingCommandModel}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredDelegatingCommandModel(InstantiationContext context, Config config) {
		super(context.getInstance(config.getCommand()));
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}


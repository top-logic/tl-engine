/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link AbstractCommandModelConfiguration} is an abstract superclass which handles some
 * properties of the configured {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCommandModelConfiguration<C extends AbstractCommandModelConfiguration.Config<?>>
		extends CommandModelCustomization<C> implements CommandModelConfiguration {

    /**
     * Configuration interface for {@link AbstractCommandModelConfiguration}.
     * 
     * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
     */
	public interface Config<I extends AbstractCommandModelConfiguration<?>>
			extends CommandModelCustomization.Config<I> {

		/** @see #getCommandArguments() */
		String COMMAND_ARGUMENTS_NAME = "commandArguments";
		
		/**
		 * Static arguments for the command.
		 */
		@Name(COMMAND_ARGUMENTS_NAME)
		@MapBinding(valueFormat = StringValueProvider.class, tag = "argument")
		Map<String, Object> getArguments();
	}
	
	/**
	 * Creates a {@link AbstractCommandModelConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
	}
	
	@Override
	public final CommandModel createCommandModel(LayoutComponent layoutComponent) {
		CommandModel model = internalCreateCommandModel(layoutComponent);
		if (model != null) {
			customizeCommandModel(model);
		}
		return model;
	}
	
	/**
	 * This method actually creates the {@link CommandModel}
	 * 
	 * @param layoutComponent
	 *            the component which is used to register the command
	 * @see #createCommandModel(LayoutComponent)
	 */
	protected abstract CommandModel internalCreateCommandModel(LayoutComponent layoutComponent);
	
	/**
	 * @see Config#getArguments()
	 */
	protected final Map<String, Object> getCommandArguments() {
		return getConfig().getArguments();
	}
	
}

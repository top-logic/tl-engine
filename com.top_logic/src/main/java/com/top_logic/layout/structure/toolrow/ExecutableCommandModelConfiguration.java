/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.toolrow;

import java.lang.reflect.Constructor;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.configuration.AbstractCommandModelConfiguration;
import com.top_logic.layout.component.configuration.CommandModelConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ExecutableCommandModelConfiguration} provides a
 * {@link CommandModelConfiguration} based on an {@link Command}. It is necessary to specify the
 * executable by given the {@link CommandModel} class (see {@link Config#getCommandModel()}).
 * 
 * @author <a href="mailto:theo.sattler@top-logic.com">Theo Sattler</a>
 */
public class ExecutableCommandModelConfiguration<C extends ExecutableCommandModelConfiguration.Config<?>>
		extends AbstractCommandModelConfiguration<C> {

	/**
	 * Configuration options for {@link ExecutableCommandModelConfiguration}.
	 */
	public interface Config<I extends ExecutableCommandModelConfiguration<?>>
			extends AbstractCommandModelConfiguration.Config<I> {

		/** @see #getCommandModel() */
		String COMMAND_MODEL_CLASS = "commandModel";

		/**
		 * The class of the executable to be executed by the command.
		 */
		@Name(COMMAND_MODEL_CLASS)
		@Mandatory
		String getCommandModel();
	}

	/**
	 * Creates a {@link ExecutableCommandModelConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExecutableCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The given {@link LayoutComponent} must not be <code>null</code>. If there is no name for a
	 * component which can be used to execute the configured command, then the given
	 * {@link LayoutComponent} is also the target component.
	 */
	@Override
	protected CommandModel internalCreateCommandModel(LayoutComponent aLayoutComponent) {
		CommandModel theCM;
		String commandModelClass = getConfig().getCommandModel();
		try {
			Class<? extends CommandModel> theClass = (Class<? extends CommandModel>) Class.forName(commandModelClass);
			try {
				Constructor<? extends CommandModel> theConstructor = theClass.getConstructor(LayoutComponent.class);
				theCM = theConstructor.newInstance(aLayoutComponent);
			} catch (NoSuchMethodException e) {
				// try default constructor
				theCM = theClass.newInstance();
			}
		} catch (Exception e) {
			throw new ConfigurationError("Unable to instanciate command model for "+commandModelClass, e);
		}
		return theCM;
	}
	
}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ConfiguredCommandModelConfiguration} which adds a {@link CheckScope} to the
 * configured {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DirtyCheckingCommandModelConfiguration<C extends DirtyCheckingCommandModelConfiguration.Config<?>>
		extends ConfiguredCommandModelConfiguration<C> {

	/**
	 * Typed configuration interface definition for {@link DirtyCheckingCommandModelConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends DirtyCheckingCommandModelConfiguration<?>>
			extends ConfiguredCommandModelConfiguration.Config<I> {

		/** Configuration name for the value of {@link #getComponentName()}. */
		String COMPONENT_NAME_NAME = "componentName";

		/** Configuration name for the value of {@link #getCheckScopeProvider()}. */
		String CHECK_SCOPE_PROVIDER_NAME = "checkScopeProvider";

		/**
		 * Name of the component that is used to create the {@link CheckScope} from the
		 * {@link #getCheckScopeProvider() configured provider}.
		 * 
		 * @return May be <code>null</code>, which means that the component used to create the
		 *         {@link CommandModelConfiguration#createCommandModel(LayoutComponent) command
		 *         model} is used.
		 */
		@Name(COMPONENT_NAME_NAME)
		ComponentName getComponentName();

		/**
		 * Configuration of the {@link CheckScopeProvider} to create the {@link CheckScope} to use.
		 */
		@Name(CHECK_SCOPE_PROVIDER_NAME)
		@Mandatory
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

	}

	private final CheckScopeProvider _checkScopeProvider;

	/**
	 * Creates a new {@link DirtyCheckingCommandModelConfiguration}.
	 */
	public DirtyCheckingCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
		_checkScopeProvider = context.getInstance(config.getCheckScopeProvider());
	}

	@Override
	public CommandModel createCommandModel(LayoutComponent aLayoutComponent) {
		CommandModel commandModel = super.createCommandModel(aLayoutComponent);
		ComponentName componentName = getConfig().getComponentName();
		LayoutComponent checkComponent;
		if (componentName == null) {
			checkComponent = aLayoutComponent;
		} else {
			checkComponent = aLayoutComponent.getMainLayout().getComponentByName(componentName);
			if (checkComponent == null) {
				Logger.error("No component '" + componentName + "' found as definied in " + getConfig().location()
					+ ". Ignore check scope.",
					DirtyCheckingCommandModelConfiguration.class);
				return commandModel;
			}
		}
		CheckScope checkScope = _checkScopeProvider.getCheckScope(checkComponent);
		if (commandModel instanceof AbstractCommandModel) {
			((AbstractCommandModel) commandModel).setCheckClosure(checkScope);
		} else {
			commandModel = new CommandModelAdapter(commandModel) {
				@Override
				public CheckScope getCheckScope() {
					return checkScope;
				}
			};
		}
		return commandModel;
	}

}


/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.components;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.commands.AddComponentCommand;
import com.top_logic.layout.editor.commands.DeleteComponentCommand;
import com.top_logic.layout.editor.commands.EditLayoutCommand;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * Component containing a command to configure i.e. replace itself with an instantiated
 * {@link DynamicComponentDefinition}.
 * 
 * @see DynamicComponentDefinition
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ComponentPlaceholder extends BoundComponent {

	/**
	 * Configuration registering a component configuration command.
	 */
	public interface Config extends BoundComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ClassDefault(ComponentPlaceholder.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@FormattedDefault("placeholder") // Required to prevent synthetic names, since those get no
											// scope.
		ComponentName getName();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);

			registry.registerButton(AddComponentCommand.DEFAULT_COMMAND_ID);
			registry.registerButton(DeleteComponentCommand.DEFAULT_COMMAND_ID);
			registry.registerButton(EditLayoutCommand.DEFAULT_COMMAND_ID);
		}
	}
	
	/**
	 * Creates a {@link ComponentPlaceholder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ComponentPlaceholder(InstantiationContext context, ComponentPlaceholder.Config config)
			throws ConfigurationException {
		super(context, config);
	}

}

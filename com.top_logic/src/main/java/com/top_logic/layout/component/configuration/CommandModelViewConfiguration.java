/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.IButtonRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link CommandModelViewConfiguration} is an adapter to configure a command model to a
 * {@link LayoutComponent}. The resulting {@link View} is a view to render the configured
 * {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandModelViewConfiguration<C extends CommandModelViewConfiguration.Config<?>>
		extends AbstractConfiguredInstance<C> implements ViewConfiguration {

	private final CommandModelConfiguration conf;

	private final IButtonRenderer renderer;

    /**
     * Configuration interface for {@link CommandModelConfiguration}.
     * 
     * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
     */
	public interface Config<I extends CommandModelViewConfiguration<?>> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getCommandConfiguration()
		 */
		String COMMAND_CONFIGURATION = "commandConfiguration";

		/**
		 * @see #getRenderer()
		 */
		String RENDERER = "renderer";

		/**
		 * The {@link IButtonRenderer} to create the visual representation.
		 */
		@Name(RENDERER)
		@InstanceFormat
		IButtonRenderer getRenderer();
		
		/**
		 * Configuration of the command to execute.
		 */
		@Name(COMMAND_CONFIGURATION)
		PolymorphicConfiguration<CommandModelConfiguration> getCommandConfiguration();
	}
	
	/**
	 * Creates a {@link CommandModelViewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandModelViewConfiguration(InstantiationContext context, C config) {
		super(context, config);
		
		this.conf = context.getInstance(config.getCommandConfiguration());
		this.renderer = config.getRenderer();
	}
	
	@Override
	public HTMLFragment createView(LayoutComponent component) {
		if (conf == null) {
			throw new NullPointerException("A CommandModelViewConfiguration needs a commandConfiguration");
		}
		CommandModel commandModel = conf.createCommandModel(component);
		if (commandModel == null) {
			return Fragments.empty();
		}
		return createView(component, commandModel);
	}

	/**
	 * Creates the view from the configured {@link CommandModel}.
	 * 
	 * @param component
	 *        The context component.
	 * @param commandModel
	 *        The created {@link CommandModel}.
	 */
	protected HTMLFragment createView(LayoutComponent component, CommandModel commandModel) {
		if (renderer != null) {
			return new ButtonControl(commandModel, renderer);
		} else {
			return new ButtonControl(commandModel);
		}
	}
}

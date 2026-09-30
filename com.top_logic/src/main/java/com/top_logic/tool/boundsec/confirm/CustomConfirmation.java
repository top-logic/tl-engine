/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.confirm;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler.TargetConfig;

/**
 * {@link CommandConfirmation} that can be customized with a message to show.
 */
@InApp
public class CustomConfirmation extends DefaultConfirmation
		implements ConfiguredInstance<CustomConfirmation.Config<?>> {

	private Config<?> _config;

	/**
	 * Configuration options for {@link CustomConfirmation}.
	 */
	public interface Config<I extends CustomConfirmation> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getConfirmMessage()
		 */
		String CONFIRM_MESSAGE = "confirmMessage";

		/**
		 * The message to display to the user requesting for confirmation that the command should
		 * really be executed.
		 * 
		 * <p>
		 * The message can refer to the {@link TargetConfig#getTarget() target model} of the command
		 * using the <code>{0}</code> placeholder.
		 * </p>
		 * 
		 * <p>
		 * If no message is specified, a default confirmation message is shown.
		 * </p>
		 */
		@Name(CONFIRM_MESSAGE)
		ResKey1 getConfirmMessage();
	}

	/**
	 * Creates a {@link CustomConfirmation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomConfirmation(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	public ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model,
			Map<String, Object> arguments) {
		ResKey1 customConfirm = getConfig().getConfirmMessage();
		if (customConfirm != null) {
			return customConfirm.fill(model);
		}

		return getDefaultConfirmation(component, commandLabel, model, arguments);
	}

}

/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.LabelRenderer;

/**
 * {@link LabelProvider} whose internal {@link LabelProvider} is configured.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredLabelRenderer extends LabelRenderer implements ConfiguredInstance<ConfiguredLabelRenderer.Config> {

	/**
	 * Typed configuration interface definition for {@link ConfiguredLabelRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ConfiguredLabelRenderer> {

		/**
		 * The {@link LabelProvider} that is used to create the rendered label.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends LabelProvider> getLabels();
	}

	private final Config _config;

	/**
	 * Create a {@link ConfiguredLabelRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredLabelRenderer(InstantiationContext context, Config config) {
		super(context.getInstance(config.getLabels()));
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}


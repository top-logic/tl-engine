/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Configurable {@link RewritingEventVisitor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredRewritingEventVisitor<C extends ConfiguredRewritingEventVisitor.Config<?>>
		extends RewritingEventVisitor implements ConfiguredInstance<C> {

	/**
	 * Typed configuration interface definition for {@link ConfiguredRewritingEventVisitor}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends ConfiguredRewritingEventVisitor<?>> extends PolymorphicConfiguration<I> {
		// configuration interface definition
	}

	private final C _config;

	/**
	 * Create a {@link ConfiguredRewritingEventVisitor}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredRewritingEventVisitor(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

}


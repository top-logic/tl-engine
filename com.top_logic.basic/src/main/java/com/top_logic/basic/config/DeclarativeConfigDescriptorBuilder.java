/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.NonNullable;

/**
 * {@link AbstractConfiguredInstance} holding a configured {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeclarativeConfigDescriptorBuilder<C extends DeclarativeConfigDescriptorBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> {

	/**
	 * Configuration for a {@link DeclarativeConfigDescriptorBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends DeclarativeConfigDescriptorBuilder<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration of the {@link ConfigurationDescriptor} to instantiate.
		 */
		@Mandatory
		@NonNullable
		ConfigurationDescriptorConfig getDescriptor();

	}

	private final ConfigurationDescriptor _descriptor;

	/**
	 * Creates a new {@link DeclarativeConfigDescriptorBuilder}.
	 */
	public DeclarativeConfigDescriptorBuilder(InstantiationContext context, C config) {
		super(context, config);
		BufferingProtocol descriptorCreationLog = new BufferingProtocol();
		_descriptor = TypedConfiguration.getConfigurationDescriptor(descriptorCreationLog, config.getDescriptor());
		
		if (descriptorCreationLog.hasErrors()) {
			context.error("Unable to create configuration descriptor.");
			descriptorCreationLog.getErrors().forEach(context::error);
		}
	}

	/**
	 * The configured {@link ConfigurationDescriptor}.
	 */
	public ConfigurationDescriptor descriptor() {
		return _descriptor;
	}

}


/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorProxy;

/**
 * {@link AttributeValueLocator} that dispatches to a configured locator.
 * 
 * @see AttributeValueLocatorFactory
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FactoryDispatch extends AttributeValueLocatorProxy implements ConfiguredInstance<FactoryDispatch.Config> {

	private final Config _config;

	private volatile AbstractAttributeValueLocator _impl;

	/**
	 * Configuration options for {@link FactoryDispatch}.
	 */
	@TagName("config-reference")
	public interface Config extends PolymorphicConfiguration<FactoryDispatch> {

		/**
		 * The name of the locator in the factory.
		 * 
		 * @see AttributeValueLocatorFactory#createConfiguredLocator(String, String)
		 */
		@Mandatory
		String getName();

		/**
		 * @see #getName()
		 */
		void setName(String name);

		/**
		 * The locator's sub-configuration.
		 * 
		 * @see AttributeValueLocatorFactory#createConfiguredLocator(String, String)
		 */
		@Nullable
		String getSpec();

		/**
		 * @see #getSpec()
		 */
		void setSpec(String spec);

	}

	/**
	 * Creates a {@link FactoryDispatch} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FactoryDispatch(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	protected AbstractAttributeValueLocator impl() {
		AbstractAttributeValueLocator result = _impl;
		if (result == null) {
			AttributeValueLocatorFactory factory =
				AttributeValueLocatorFactory.Module.INSTANCE.getImplementationInstance();
			result =
				(AbstractAttributeValueLocator) factory.createConfiguredLocator(_config.getName(), _config.getSpec());
			_impl = result;
		}
		return result;
	}

	/**
	 * Creates a {@link FactoryDispatch} configuration.
	 * 
	 * @param name
	 *        See {@link Config#getName()}.
	 * @param spec
	 *        See {@link Config#getSpec()}.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(String name,
			String spec) {
		Config config = TypedConfiguration.newConfigItem(FactoryDispatch.Config.class);
		config.setName(name);
		config.setSpec(spec);
		return config;
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.format;

import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link FormatProvider} adapting a {@link ConfigurationValueProvider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingletonFormatProvider<C extends SingletonFormatProvider.Config<?>>
		extends AbstractConfiguredInstance<C> implements FormatProvider {

	/**
	 * Configuration options for {@link SingletonFormatProvider}.
	 */
	@TagName("singleton-format")
	public interface Config<I extends SingletonFormatProvider<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link ConfigurationValueProvider} to adapt.
		 */
		@InstanceFormat
		Class<? extends Format> getImpl();

	}

	/**
	 * Creates a {@link SingletonFormatProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SingletonFormatProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Format createFormat() throws ConfigurationException {
		return ConfigUtil.getInstance(getConfig().getImpl());
	}

}
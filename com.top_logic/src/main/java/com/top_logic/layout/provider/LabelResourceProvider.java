/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * Dynamic extension of a {@link LabelProvider} to a {@link ResourceProvider} to
 * use a {@link LabelProvider} in a context where a {@link ResourceProvider} is
 * required.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelResourceProvider extends AbstractResourceProvider
		implements ConfiguredInstance<LabelResourceProvider.Config<?>> {
	private final LabelProvider _labelProvider;

	private Config<?> _config;

	/**
	 * Configuration options for {@link LabelResourceProvider}.
	 */
	public interface Config<I extends LabelResourceProvider> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link LabelProvider} to wrap.
		 */
		@InstanceFormat
		LabelProvider getLabelProvider();

		/**
		 * @see #getLabelProvider()
		 */
		void setLabelProvider(LabelProvider value);

	}

	/**
	 * Creates a {@link LabelResourceProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LabelResourceProvider(InstantiationContext context, Config<?> config) {
		_config = config;
		_labelProvider = config.getLabelProvider();
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	public String getLabel(Object object) {
		return _labelProvider.getLabel(object);
	}
	
	/**
	 * Dynamically convert the given {@link LabelProvider} into a
	 * {@link ResourceProvider}.
	 * 
	 * @return the given {@link LabelProvider}, if it also implements the
	 *         {@link ResourceProvider} interface, or a new
	 *         {@link LabelResourceProvider} instance wrapping the given
	 *         {@link LabelProvider}.
	 */
	public static ResourceProvider toResourceProvider(LabelProvider labelProvider) {
		if (labelProvider instanceof ResourceProvider) {
			return (ResourceProvider) labelProvider;
		}

		Config<?> config = TypedConfiguration.newConfigItem(Config.class);
		config.setLabelProvider(labelProvider);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}
}

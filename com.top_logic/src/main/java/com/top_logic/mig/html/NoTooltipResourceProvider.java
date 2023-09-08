/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.ConfiguredProxyResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * The NoTooltipResourceProvider suppresses the tooltips of the underlying ResourceProvider.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class NoTooltipResourceProvider
		extends ConfiguredProxyResourceProvider<ConfiguredProxyResourceProvider.Config<NoTooltipResourceProvider>> {

    /** Default instance of this class using the MetaResourceProvider as underlying resource provider. */
	public static final NoTooltipResourceProvider INSTANCE =
		createNoTooltipResourceProvider(MetaResourceProvider.INSTANCE);

	/**
	 * Creates a new NoTooltipResourceProvider over the given ResourceProvider.
	 *
	 * @param aResourceProvider
	 *        the resource provider to suppress tooltips
	 */
    public static NoTooltipResourceProvider createNoTooltipResourceProvider(ResourceProvider aResourceProvider) {
		@SuppressWarnings("unchecked")
		ConfiguredProxyResourceProvider.Config<NoTooltipResourceProvider> config =
			TypedConfiguration.newConfigItem(ConfiguredProxyResourceProvider.Config.class);
		config.setImplementationClass(NoTooltipResourceProvider.class);
		config.setInner(aResourceProvider);
		return TypedConfigUtil.createInstance(config);
    }

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link NoTooltipResourceProvider}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public NoTooltipResourceProvider(InstantiationContext context, Config<NoTooltipResourceProvider> config) {
		super(context, config);
	}

    @Override
	public String getTooltip(Object aObject) {
		// Explicitly suppress looking up a tool-tip.
        return null;
    }

}

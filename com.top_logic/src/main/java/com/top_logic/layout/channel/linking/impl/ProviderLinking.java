/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.DerivedComponentChannel;
import com.top_logic.layout.channel.linking.Provider;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} specified by {@link Provider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ProviderLinking extends AbstractCachedChannelLinking<Provider> {

	/**
	 * Creates a {@link ProviderLinking}.
	 */
	public ProviderLinking(InstantiationContext context, Provider config) {
		super(context, config);
	}

	@Override
	public ComponentChannel createChannel(Log log, LayoutComponent contextComponent) {
		ModelProvider provider = InstantiationContext.toContext(log).getInstance(getConfig().getImpl());
		if (provider == null) {
			return null;
		}
		return new DerivedComponentChannel(contextComponent, provider);
	}

	@Override
	public Object eval(LayoutComponent component) {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		return context.getInstance(getConfig().getImpl()).getBusinessModel(component);
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("provider");
		result.append("(");
		result.append(getConfig().getImpl().getImplementationClass().getName());
		result.append(")");
	}

	@Override
	public boolean hasCompactRepresentation() {
		PolymorphicConfiguration<? extends ModelProvider> config = getConfig().getImpl();
		if (config.getConfigurationInterface() != PolymorphicConfiguration.class) {
			// The provider requires some kind of configuration.
			return false;
		}
		return true;
	}
}

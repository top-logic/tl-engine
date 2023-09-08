/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.ProviderLinking;
import com.top_logic.layout.component.model.ModelProvider;

/**
 * {@link ModelSpec} computing the model by invoking a {@link ModelProvider}.
 * 
 * @see ModelProvider#getBusinessModel(com.top_logic.mig.html.layout.LayoutComponent)
 * @see ProviderLinking
 */
@TagName("provider")
public interface Provider extends ModelSpec {

	@Override
	@ClassDefault(ProviderLinking.class)
	Class<? extends ChannelLinking> getImplementationClass();

	/**
	 * The {@link ModelProvider} implementation to evaluate for retrieving the model.
	 */
	@Mandatory
	@DefaultContainer
	PolymorphicConfiguration<? extends ModelProvider> getImpl();

	/**
	 * @see #getImpl()
	 */
	void setImpl(PolymorphicConfiguration<? extends ModelProvider> value);
}
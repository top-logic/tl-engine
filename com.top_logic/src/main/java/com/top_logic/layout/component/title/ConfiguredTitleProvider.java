/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.title;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * {@link AbstractConfiguredInstance} {@link TitleProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredTitleProvider<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C> implements TitleProvider {

	/**
	 * Creates a {@link ConfiguredTitleProvider}.
	 */
	public ConfiguredTitleProvider(InstantiationContext context, C config) {
		super(context, config);
	}

}


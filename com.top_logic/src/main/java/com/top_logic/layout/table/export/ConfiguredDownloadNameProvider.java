/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * {@link AbstractConfiguredInstance} {@link DownloadNameProvider}.
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 */
public abstract class ConfiguredDownloadNameProvider<C extends PolymorphicConfiguration<?>>
	extends AbstractConfiguredInstance<C> implements DownloadNameProvider {

	/**
	 * Creates a {@link ConfiguredDownloadNameProvider}.
	 */
	public ConfiguredDownloadNameProvider(InstantiationContext context, C config) {
		super(context, config);
	}

}
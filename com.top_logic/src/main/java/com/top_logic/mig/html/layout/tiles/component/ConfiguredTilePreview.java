/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Configured implementation of {@link TilePreview}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredTilePreview<C extends ConfiguredTilePreview.Config<?>>
		extends AbstractConfiguredInstance<C> implements TilePreview {

	/**
	 * Configuration of a {@link ConfiguredTilePreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ConfiguredTilePreview<?>> extends PolymorphicConfiguration<I> {

		// no properties here

	}

	/**
	 * Creates a new {@link ConfiguredTilePreview}.
	 */
	public ConfiguredTilePreview(InstantiationContext context, C config) {
		super(context, config);
	}

}


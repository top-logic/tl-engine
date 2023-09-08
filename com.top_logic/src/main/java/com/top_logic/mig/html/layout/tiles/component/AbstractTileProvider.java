/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * Abstract super class for {@link TileProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTileProvider<C extends AbstractTileProvider.Config<?>>
		extends AbstractConfiguredInstance<C> implements TileProvider {

	/**
	 * Configuration for the {@link AbstractTileProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface Config<I extends AbstractTileProvider<?>> extends TileProviderConfig<I> {
		// Configuration interface for AbstractTileProvider.
	}

	/**
	 * Creates a new {@link AbstractTileProvider}.
	 */
	public AbstractTileProvider(InstantiationContext context, C config) {
		super(context, config);
	}

}


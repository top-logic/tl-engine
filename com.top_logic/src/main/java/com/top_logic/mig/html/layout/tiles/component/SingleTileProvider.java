/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link TileProvider} returning a singleton list containing the given {@link ComponentTile} .
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingleTileProvider extends AbstractTileProvider<SingleTileProvider.Config> {

	/** A {@link SingleTileProvider} instance. */
	public static final SingleTileProvider INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
		.getInstance(TypedConfiguration.newConfigItem(SingleTileProvider.Config.class));

	/**
	 * Configuration of the {@link SingleTileProvider}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractTileProvider.Config<SingleTileProvider> {

		@Override
		@BooleanDefault(false)
		boolean isComponentDisplayedInline();

	}

	/**
	 * Creates a new {@link SingleTileProvider}.
	 */
	public SingleTileProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public List<? extends ComponentTile> getDisplayTiles(ContainerComponentTile tile) {
		return Collections.singletonList(tile);
	}

}


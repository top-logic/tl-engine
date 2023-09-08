/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;

/**
 * Supplier for a list of {@link ComponentTile} within a tile environment.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentTileSupplier extends Supplier<List<ComponentTile>> {

	/**
	 * A list of {@link ComponentTile}s that are displayed by {@link #getRootComponent()}.
	 */
	@Override
	List<ComponentTile> get();

	/**
	 * Returns the root {@link LayoutComponent} of the tile hierarchy to which this
	 * {@link ComponentTileSupplier} belongs.
	 * 
	 * @apiNote Currently this is either a {@link RootTileComponent} or a
	 *          {@link TileContainerComponent}.
	 */
	LayoutComponent getRootComponent();

}


/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;

/**
 * Allows the command only when the component is a {@link TileContainerComponent} and currently
 * displays a {@link CompositeTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class HasCompositeTileLayout extends HasSpecificTileExecutability {

	/** Singleton {@link HasCompositeTileLayout} instance. */
	public static final HasCompositeTileLayout INSTANCE = new HasCompositeTileLayout();

	private HasCompositeTileLayout() {
		// singleton instance
	}

	@Override
	protected Class<? extends TileLayout> tileLayoutClass() {
		return CompositeTile.class;
	}

}

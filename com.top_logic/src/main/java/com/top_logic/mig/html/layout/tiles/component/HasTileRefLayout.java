/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;

/**
 * Allows the command only when the component is a {@link TileContainerComponent} and currently
 * displays a {@link TileRef}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class HasTileRefLayout extends HasSpecificTileExecutability {

	/** Singleton {@link HasTileRefLayout} instance. */
	public static final HasTileRefLayout INSTANCE = new HasTileRefLayout();

	private HasTileRefLayout() {
		// singleton instance
	}

	@Override
	protected Class<? extends TileLayout> tileLayoutClass() {
		return TileRef.class;
	}

}

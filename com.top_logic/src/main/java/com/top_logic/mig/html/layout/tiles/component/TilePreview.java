/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.tiles.TileLayout;

/**
 * Factory to create a {@link HTMLFragment} as preview for a given {@link ComponentTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TilePreview {

	/**
	 * {@link TilePreview} constantly returning an {@link Fragments#empty() empty fragment}.
	 */
	TilePreview EMPTY = (tile) -> Fragments.empty();

	/**
	 * Creates a preview for the given {@link TileLayout} displayed in the given
	 * {@link TileContainerComponent}.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create preview for.
	 * 
	 * @return The preview {@link HTMLFragment} for the given {@link ComponentTile}. Must not be
	 *         <code>null</code> but may be empty.
	 */
	HTMLFragment createPreview(ComponentTile tile);

}


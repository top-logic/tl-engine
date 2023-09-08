/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.List;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;

/**
 * Factory interface creating {@link ComponentTile} for a {@link LayoutComponent}.
 * 
 * @see TileInfo
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface TileFactory {

	/**
	 * Creates {@link ComponentTile}s to display the given component.
	 * 
	 * <p>
	 * The returned list may contain more elements, e.g. to display the given component with
	 * different objects as model.
	 * </p>
	 * 
	 * @param component
	 *        The component to display.
	 */
	List<ComponentTile> createTiles(LayoutComponent component);
}


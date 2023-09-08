/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Factory for {@link ComponentTile}s to be displayed in the overview for a given
 * {@link ContainerComponentTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface TileProvider {

	/**
	 * Configuration for the {@link TileProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	interface TileProviderConfig<T extends TileProvider> extends PolymorphicConfiguration<T> {

		/**
		 * Whether the component at which the {@link TileProvider} is configured is displayed
		 * inline.
		 * 
		 * <p>
		 * Normally the {@link TileProvider} gets a {@link ContainerComponentTile} which represents
		 * a {@link LayoutComponent}. The component is not visible unless the given
		 * {@link ContainerComponentTile} is clicked. When {@link #isComponentDisplayedInline()} is
		 * <code>true</code>, the component is treated as visible as soon as the the
		 * {@link TileProvider#getDisplayTiles(ContainerComponentTile) tiles} are displayed.
		 * </p>
		 * 
		 * @implSpec This property is actually an intrinsic part of the implementation class of the
		 *           configured {@link TileProvider} and should not be configured, but overridden.
		 */
		@Abstract
		boolean isComponentDisplayedInline();

	}

	/**
	 * Creates the {@link ComponentTile} to display for the given {@link ComponentTile}.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create displayed {@link ComponentTile}s for.
	 * 
	 * @return A list of {@link ComponentTile} to display for the given {@link ComponentTile}.
	 */
	List<? extends ComponentTile> getDisplayTiles(ContainerComponentTile tile);

}


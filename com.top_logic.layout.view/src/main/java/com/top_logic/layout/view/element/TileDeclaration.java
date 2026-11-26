/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * An entry of a {@link DashboardElement dashboard}: a declaration contributing tiles to it.
 *
 * <p>
 * A {@link TileElement &lt;tile&gt;} contributes one fixed tile, a {@link TileListElement
 * &lt;tiles&gt;} one tile per object of a computed list. The tiles of all entries share one order.
 * </p>
 *
 * <p>
 * An entry is displayed by the dashboard only; it creates no control of its own.
 * </p>
 */
public interface TileDeclaration extends UIElement {

	/**
	 * Configuration of a {@link TileDeclaration}.
	 */
	interface Config extends UIElement.Config {

		@Override
		Class<? extends TileDeclaration> getImplementationClass();

		/**
		 * The stable id of this entry within its dashboard.
		 *
		 * <p>
		 * It is the persistence key of the tiles of the entry when the user rearranges the
		 * dashboard: the id of a fixed tile, the prefix of the ids of computed tiles.
		 * </p>
		 */
		@Override
		@Name(ID)
		@Mandatory
		String getId();
	}

	/**
	 * Creates the tiles this entry contributes to a dashboard displayed in the given context.
	 *
	 * @param context
	 *        The context the dashboard is displayed in.
	 * @param host
	 *        Told when the contributed tiles or what activating them offers change.
	 * @return The tiles of this entry for one display of the dashboard.
	 */
	DashboardTiles createTiles(ViewContext context, DashboardTiles.Host host);

	@Override
	default IReactControl createControl(ViewContext context) {
		throw new UnsupportedOperationException("A tile declaration must be an entry of a <dashboard>.");
	}

}

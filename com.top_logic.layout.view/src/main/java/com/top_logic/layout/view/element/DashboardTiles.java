/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.layout.ItemActivation;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.TileContent;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.model.listen.ModelScope;

/**
 * The tiles a {@link TileDeclaration} contributes to one display of a {@link DashboardElement
 * dashboard}.
 *
 * <p>
 * The tiles may change while the dashboard is displayed; the {@link Host} is told so, and reads
 * the {@link #tiles() current tiles} again.
 * </p>
 */
public interface DashboardTiles {

	/**
	 * What a dashboard offers the tiles of its entries.
	 */
	interface Host {

		/**
		 * The {@link DashboardTiles#tiles() tiles} have changed.
		 */
		void tilesChanged();

		/**
		 * What activating the tiles offers has changed.
		 */
		void activationChanged();
	}

	/**
	 * One tile, as contributed by an entry of the dashboard.
	 *
	 * @param id
	 *        The stable id of the tile, unique within the dashboard.
	 * @param width
	 *        The relative width of the tile.
	 * @param rowSpan
	 *        The number of grid rows the tile occupies.
	 * @param label
	 *        The name the tile is announced under while it is moved, {@code null} for none.
	 * @param title
	 *        The name a hidden tile is offered to be added under.
	 * @param optional
	 *        Whether the tile is hidden for a user who has neither hidden nor added it.
	 * @param hiding
	 *        What withholds the tile from the dashboard, {@code null} for nothing.
	 * @param content
	 *        Creates what the tile displays, called when the tile is shown for the first time.
	 * @param activation
	 *        What activating the tile runs with the given item, {@code null} where the content
	 *        brings its own action or the tile is not activated.
	 * @param item
	 *        The object the activation runs with.
	 */
	record Entry(String id, TileWidth width, int rowSpan, String label, ResKey title, boolean optional,
			UnitHiding hiding, Supplier<TileContent> content, ItemActivation activation, Object item) {
		// Pure data.
	}

	/**
	 * The current tiles, in the order of their declaration.
	 */
	List<Entry> tiles();

	/**
	 * Begins following the model, while the dashboard is displayed.
	 *
	 * @param scope
	 *        The scope to observe the model in.
	 */
	default void attach(ModelScope scope) {
		// Fixed by default.
	}

	/**
	 * Stops following the model, while the dashboard is not displayed.
	 */
	default void detach() {
		// Fixed by default.
	}

	/**
	 * Releases what the tiles hold, when the dashboard is disposed.
	 */
	default void dispose() {
		// Nothing held by default.
	}

}

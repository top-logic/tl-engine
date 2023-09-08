/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * GUI model for a tile in a tile hierarchy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentTile {

	/** {@link Provider} returning <code>null</code>. */
	Provider<Menu> NO_MENU = () -> null;

	/**
	 * Forces the application to display this {@link ComponentTile}.
	 */
	void displayTile();

	/**
	 * The actual business object represented by this {@link ComponentTile}.
	 */
	Object getBusinessObject();

	/**
	 * The component which is displayed when this {@link ComponentTile} becomes visible.
	 * 
	 * @return May be <code>null</code>, when this {@link ComponentTile} has no
	 *         {@link LayoutComponent} as content, or it cannot be determined.
	 */
	LayoutComponent getTileComponent();

	/**
	 * Returns the {@link TilePreview} to create a {@link HTMLFragment} as preview for
	 * {@link #getBusinessObject()}.
	 */
	TilePreview getPreview();

	/**
	 * Whether the tile is allowed to be displayed.
	 */
	boolean isAllowed();

	/**
	 * Label for {@link #getBusinessObject()}.
	 */
	ResKey getTileLabel();

	/**
	 * Determines the commands that are displayed for this {@link ComponentTile}.
	 * 
	 * @return A {@link Provider} delivering the commands to display for
	 *         {@link #getBusinessObject()}.
	 */
	default Provider<Menu> getBurgerMenu() {
		return NO_MENU;
	}

}

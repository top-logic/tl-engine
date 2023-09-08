/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * {@link DefaultDescendingLayoutVisitor} visiting the components in the displayed path.
 * 
 * <p>
 * This listener abstracts between the actual {@link LayoutContainer#getChildList() child structure}
 * and the logical structure of components displayed in a tile.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayedTilesVisitor extends DefaultDescendingLayoutVisitor {


	/**
	 * Visits the displayed path in the given root tile.
	 * 
	 * @param rootTile
	 *        The {@link RootTileComponent} to visit.
	 */
	public void visitRootTile(RootTileComponent rootTile) {
		for (LayoutComponent tileComponent : rootTile.displayedPath()) {
			tileComponent.acceptVisitorRecursively(this);
		}
	}

	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		boolean descent = super.visitLayoutComponent(aComponent);
		if (aComponent instanceof GroupTileComponent) {
			// Children of a GroupTileComponent are displayed in the next part of the displayed
			// path.
			return false;
		}
		if (aComponent instanceof ContextTileComponent) {
			((ContextTileComponent) aComponent).getSelector().acceptVisitorRecursively(this);
			// Visit the selector as this component is actually visible on the GUI.
			return false;
		}
		return descent;
	}

}


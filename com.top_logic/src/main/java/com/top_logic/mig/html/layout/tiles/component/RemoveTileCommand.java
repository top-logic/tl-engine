/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to remove a given {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveTileCommand extends TileMenuCommand<TileLayout> {

	/**
	 * Creates a new {@link RemoveTileCommand}.
	 */
	public RemoveTileCommand(TileContainerComponent container, TileLayout tileToRemove) {
		super(container, tileToRemove);
		setImage(com.top_logic.layout.form.treetable.component.Icons.DELETE_MENU);
		setNotExecutableImage(com.top_logic.layout.form.treetable.component.Icons.DELETE_MENU_DISABLED);
		setLabel(I18NConstants.DELETE_TILE_LAYOUT);
	}

	@Override
	protected HandlerResult internalExecute(DisplayContext context) {
		ArrayList<TileLayout> newTiles = new ArrayList<>(((CompositeTile) _container.displayedLayout()).getTiles());
		newTiles.remove(_tile);
		_container.updateDisplayedLayout(newTiles);
		return HandlerResult.DEFAULT_RESULT;
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Command to hide a given {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HideTileCommand extends TileMenuCommand<TileLayout> {

	/**
	 * Creates a new {@link HideTileCommand}.
	 */
	public HideTileCommand(TileContainerComponent container, TileLayout tileToHide) {
		super(container, tileToHide);
		setImage(Icons.HIDE_TILE_COMMAND);
		setLabel(Resources.getInstance().getString(I18NConstants.HIDE_TILE_LAYOUT));
	}

	@Override
	protected HandlerResult internalExecute(DisplayContext context) {
		ArrayList<TileLayout> newTiles = new ArrayList<>(((CompositeTile) _container.displayedLayout()).getTiles());
		if (_tile instanceof PersonalizedTile) {
			((PersonalizedTile) _tile).setHidden(true);
		} else {
			newTiles.remove(_tile);
		}
		_container.updateDisplayedLayout(newTiles);
		return HandlerResult.DEFAULT_RESULT;
	}

}

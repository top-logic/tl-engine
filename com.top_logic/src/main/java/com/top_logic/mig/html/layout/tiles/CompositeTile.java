/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;

/**
 * {@link TileLayout} which is just a composition of other {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface CompositeTile extends TileLayout {

	/**
	 * The contained {@link TileLayout} in this {@link CompositeTile}.
	 */
	@DefaultContainer
	List<TileLayout> getTiles();

	/**
	 * Sets {@link #getTiles()}.
	 */
	void setTiles(List<TileLayout> tiles);

}


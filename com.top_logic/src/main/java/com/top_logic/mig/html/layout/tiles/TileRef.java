/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;

/**
 * Reference to a {@link TileDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("tile")
public interface TileRef extends TileLayout, NamedConfigMandatory {

	/**
	 * The concrete content of a {@link TileRef}.
	 *
	 * <p>
	 * In the content only context free {@link TileLayout} are allowed or references to
	 * {@link TileDefinition} within {@link TileDefinition#getTiles()} of the referenced
	 * {@link TileDefinition}.
	 * </p>
	 */
	@DefaultContainer
	@ItemDefault(ContextTileGroup.class)
	TileLayout getContentTile();

}


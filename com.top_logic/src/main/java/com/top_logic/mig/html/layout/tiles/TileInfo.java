/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.PreviewedTile;

/**
 * Display information for displaying a {@link LayoutComponent} as tile.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileInfo extends NamedConfigMandatory, LabeledConfiguration, PreviewedTile {

	/**
	 * Factory to create {@link ComponentTile}s for the annotated component to be displayed as tile.
	 */
	@InstanceFormat
	@InstanceDefault(SingleTileFactory.class)
	TileFactory getTileFactory();

}


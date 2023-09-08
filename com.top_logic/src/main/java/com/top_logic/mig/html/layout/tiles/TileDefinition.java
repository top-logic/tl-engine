/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.PreviewedTile;
import com.top_logic.mig.html.layout.tiles.component.SingleTileProvider;
import com.top_logic.mig.html.layout.tiles.component.TileDefinitionContainer;
import com.top_logic.mig.html.layout.tiles.component.TileProvider;

/**
 * Definition of a component tile.
 * 
 * <p>
 * A {@link TileDefinition} contains a {@link LayoutComponent} which is displayed in the tile, and
 * {@link TileDefinition}s for {@link LayoutComponent} that have {@link #getComponent()} as context
 * component.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileDefinition
		extends NamedConfigMandatory, TileDefinitionContainer, LabeledConfiguration, PreviewedTile {

	/** Configuration name of the value of {@link #getTileProvider()}. */
	String TILE_PROVIDER_NAME = "tileProvider";

	/** Configuration name of the value of {@link #getComponent()}. */
	String COMPONENT_NAME = "component";

	/** Configuration name of the value of {@link #getTileCommands()}. */
	String TILE_COMMANDS_NAME = "tileCommands";

	/** 
	 * The configuration of the {@link LayoutComponent} which is displayed in the tile.
	 */
	@DefaultContainer
	@Name(COMPONENT_NAME)
	LayoutComponent.Config getComponent();

	/**
	 * The configuration of a {@link TileProvider} to create {@link ComponentTile}s to display for
	 * the {@link TileRef} that references this {@link TileDefinition}.
	 */
	@ItemDefault(SingleTileProvider.Config.class)
	@Name(TILE_PROVIDER_NAME)
	TileProvider.TileProviderConfig<?> getTileProvider();

	/**
	 * Commands to display for the defined tile.
	 * 
	 * @return May be <code>null</code>.
	 */
	@Name(TILE_COMMANDS_NAME)
	PolymorphicConfiguration<Provider<Menu>> getTileCommands();

}


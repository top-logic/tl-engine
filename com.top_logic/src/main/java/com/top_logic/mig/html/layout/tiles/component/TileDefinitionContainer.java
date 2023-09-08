/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.tiles.TileBuilder;
import com.top_logic.mig.html.layout.tiles.TileDefinition;

/**
 * Container configuration for {@link TileDefinition} and {@link TileBuilder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileDefinitionContainer extends ConfigurationItem {

	/** Configuration name of the value of {@link #getTiles()}. */
	String TILES_ATTRIBUTE_NAME = "tiles";

	/** Configuration name of the value of {@link #getBuilders()}. */
	String TILES_BUILDERS_ATTRIBUTE_NAME = "builders";

	/**
	 * Definition of the known tiles, ordered by name.
	 * 
	 * <p>
	 * The names of all {@link TileDefinition} (including the {@link TileDefinition}s with a
	 * definition) must be pairwise different.
	 * </p>
	 */
	@Key(TileDefinition.NAME_ATTRIBUTE)
	@Name(TILES_ATTRIBUTE_NAME)
	Map<String, TileDefinition> getTiles();

	/**
	 * All configured {@link TileBuilder}s.
	 */
	@Name(TILES_BUILDERS_ATTRIBUTE_NAME)
	List<TileBuilder> getBuilders();

	/**
	 * Comma separated list of tile names which must not serve as supplier for
	 * {@link TileDefinition} and {@link TileBuilder} when it is a context of this
	 * {@link TileDefinitionContainer}.
	 * 
	 * <p>
	 * When this {@link TileDefinitionContainer} is currently displayed, it provides
	 * {@link #getTiles()} and {@link #getBuilders()} for selection. Also the {@link TileDefinition}
	 * and {@link TileBuilder} of the contexts are used. When this set contains the name of such a
	 * context, neither the {@link TileDefinition} nor the {@link TileBuilder} of that context are
	 * delivered as select option.
	 * </p>
	 */
	@Format(CommaSeparatedStringSet.class)
	Set<String> getIgnoreContexts();

}


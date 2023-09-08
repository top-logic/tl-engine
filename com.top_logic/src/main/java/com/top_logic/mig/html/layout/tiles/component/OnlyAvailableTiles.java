/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.tiles.TileDefinition;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent.DefaultLayoutConfig;

/**
 * Checks that no {@link TileRef} references to a {@link TileDefinition} which does not exist.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OnlyAvailableTiles extends GenericValueDependency<DefaultLayoutConfig, Map<String, TileDefinition>> {

	/**
	 * Creates a new {@link OnlyAvailableTiles}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OnlyAvailableTiles() {
		super(DefaultLayoutConfig.class, (Class) Map.class);
	}

	@Override
	public boolean isChecked(int index) {
		return index == 0;
	}

	@Override
	protected void checkValue(PropertyModel<DefaultLayoutConfig> defaultLayout,
			PropertyModel<Map<String, TileDefinition>> configuredTiles) {
		/* Problems are logged when there are more with one tile with the same name. This is checked
		 * in a different constraint. */
		BufferingProtocol log = new BufferingProtocol();
		Set<String> knownTiles = TileUtils.addDefinitionsRecursive(log, configuredTiles.getValue(), new HashMap<>()).keySet();
		TileLayout value = defaultLayout.getValue().getRootTile();
		ResKey problem = TileUtils.hasUnkownTile(value, knownTiles);

		if (problem != ResKey.NONE) {
			defaultLayout.setProblemDescription(problem);
		}

	}

}

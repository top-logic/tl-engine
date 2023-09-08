/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.mig.html.layout.tiles.TileDefinition;
import com.top_logic.mig.html.layout.tiles.TileUtils;

/**
 * {@link ValueConstraint} checking that there are no two {@link TileDefinition} with the same
 * {@link TileDefinition#getName()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoDuplicateTileNames extends ValueConstraint<Map<String, TileDefinition>> {

	/**
	 * Creates a new {@link NoDuplicateTileNames}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NoDuplicateTileNames() {
		super((Class) Map.class);
	}

	@Override
	protected void checkValue(PropertyModel<Map<String, TileDefinition>> configs) {
		Map<String, TileDefinition> tiles = configs.getValue();
		BufferingProtocol log = new BufferingProtocol();
		TileUtils.addDefinitionsRecursive(log, tiles, new HashMap<>());
		if (!log.hasErrors()) {
			return;
		}
		configs.setProblemDescription(I18NConstants.DUPLICATE_TILE_NAMES__ERRORS.fill(log.getErrors()));
	}

}


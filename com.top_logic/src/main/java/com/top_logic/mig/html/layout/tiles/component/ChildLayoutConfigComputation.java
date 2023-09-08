/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.func.Function1;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.tiles.TileDefinition;

/**
 * Computation of the child configurations of a {@link TileContainerComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChildLayoutConfigComputation extends Function1<List<LayoutComponent.Config>, Map<String, TileDefinition>> {

	@Override
	public List<Config> apply(Map<String, TileDefinition> arg) {
		List<LayoutComponent.Config> configs = new ArrayList<>();
		addChildConfigsRecursive(configs, arg);
		return configs;
	}

	private void addChildConfigsRecursive(List<Config> configs, Map<String, TileDefinition> definitions) {
		definitions.values().forEach(definition -> {
			configs.add(definition.getComponent());
			addChildConfigsRecursive(configs, definition.getTiles());
		});
	}

}

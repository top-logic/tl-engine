/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;

/**
 * {@link AbstractComponentTileSupplier} for a {@link GroupTileComponent} retrieving a
 * {@link ComponentTile} for each child of the component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GroupComponentTile extends AbstractComponentTileSupplier {

	private GroupTileComponent _tileGroup;

	/** 
	 * Creates a new {@link GroupComponentTile}.
	 */
	public GroupComponentTile(LayoutComponent tileContainer, GroupTileComponent tileGroup) {
		super(tileContainer);
		_tileGroup = tileGroup;
	}

	private GroupTileComponent tileGroup() {
		return _tileGroup;
	}

	@Override
	public List<ComponentTile> get() {
		return tileGroup().getChildList()
			.stream()
			.map(GroupComponentTile::createTiles)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	private static List<ComponentTile> createTiles(LayoutComponent component) {
		return component.getConfig().getTileInfo().getTileFactory().createTiles(component);
	}

}

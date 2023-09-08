/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.SimpleDecoratingLayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.control.CompositeTileControl;

/**
 * {@link LayoutControlProvider} creating a {@link CompositeTileControl} for a
 * {@link TileListComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileListControlProvider extends SimpleDecoratingLayoutControlProvider<TileListControlProvider> {

	private static class TileListComponentTile extends AbstractComponentTileSupplier {

		private TileListComponent _tileComponent;

		public TileListComponentTile(LayoutComponent tileContainer, TileListComponent tileComponent) {
			super(tileContainer);
			_tileComponent = tileComponent;
		}

		@Override
		public List<ComponentTile> get() {
			return _tileComponent.createAllTiles();
		}

	}

	/**
	 * Creates a new {@link TileListControlProvider}.
	 */
	public TileListControlProvider(InstantiationContext context,
			PolymorphicConfiguration<TileListControlProvider> config) {
		super(context, config);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		LayoutComponent container = container(component);
		TileListComponent tileListComponent = (TileListComponent) component;
		TileListComponentTile rootTile = new TileListComponentTile(container, tileListComponent);
		Control content;
		if (tileListComponent.getConfig().getNoCardKey() == null) {
			content = new CompositeTileControl(rootTile);
		} else {
			content = new CompositeTileControl(rootTile, tileListComponent.getConfig().getNoCardKey());
		}

		LayoutControlAdapter control = new LayoutControlAdapter(content);
		control.listenForInvalidation(component);
		/* Ensure that control creates scrollbars, when not all tiles can be displayed on the
		 * screen. */
		control.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
		return control;
	}

	private LayoutComponent container(LayoutComponent component) {
		LayoutComponent enclosingContainer = RootTileComponent.getRootTile(component);
		if (enclosingContainer == null) {
			// Legacy tile container
			enclosingContainer = TileUtils.enclosingTileContainer(component);
		}
		return enclosingContainer;
	}

}

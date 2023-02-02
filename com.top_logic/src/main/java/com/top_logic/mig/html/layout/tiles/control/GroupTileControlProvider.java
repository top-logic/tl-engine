/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.SimpleDecoratingLayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.component.GroupComponentTile;

/**
 * {@link DecoratingLayoutControlProvider} for a {@link GroupTileComponent}.
 * 
 * <p>
 * The children of a {@link GroupTileComponent} are displayed in a tile layout.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GroupTileControlProvider extends SimpleDecoratingLayoutControlProvider<GroupTileControlProvider> {

	/**
	 * Creates a new {@link GroupTileControlProvider}.
	 */
	public GroupTileControlProvider(InstantiationContext context,
			PolymorphicConfiguration<GroupTileControlProvider> config) {
		super(context, config);
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		GroupTileComponent group = (GroupTileComponent) component;

		RootTileComponent root = RootTileComponent.getRootTile(group);
		CompositeTileControl tileControl = new CompositeTileControl(new GroupComponentTile(root, group));
		return toLayoutControl(tileControl);
	}

	private LayoutControlAdapter toLayoutControl(HTMLFragment tileControl) {
		LayoutControlAdapter lc = new LayoutControlAdapter(tileControl);
		/* Ensure that control creates scrollbars, when not all tiles can be displayed on the
		 * screen. */
		lc.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
		return lc;
	}

}


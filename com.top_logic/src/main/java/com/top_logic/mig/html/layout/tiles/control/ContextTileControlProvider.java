/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.ContextMenuLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;

/**
 * {@link ContextMenuLayoutControlProvider} for {@link ContextTileComponent}.
 * 
 * <p>
 * Default view for a {@link ContextTileComponent} is displaying its
 * {@link ContextTileComponent#getSelector() selector}. The {@link ContextTileComponent#getContent()
 * content} is displayed by {@link RootTileComponent} in a separate {@link LayoutControl}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContextTileControlProvider extends ContextMenuLayoutControlProvider<ContextTileControlProvider> {

	/**
	 * Creates a new {@link ContextTileControlProvider}.
	 */
	public ContextTileControlProvider(InstantiationContext context, Config<ContextTileControlProvider> config) {
		super(context, config);
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		ContextTileComponent contextTile = (ContextTileComponent) component;
		ContentControl result = createContentWithMenu(component);
		LayoutComponent selector = contextTile.getSelector();
		if (selector != null) {
			result.setView(strategy.createLayout(selector, contextTile.getToolBar()));
		}
		return result;
	}

}


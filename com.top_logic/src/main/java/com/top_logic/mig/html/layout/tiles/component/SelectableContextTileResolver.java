/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileUtils;

/**
 * {@link ComponentResolver} adding an {@link UpdateTileLayoutSelectionListener} at the
 * {@link SelectionChannel#NAME selection channel} of the resolved component.
 * 
 * <p>
 * This {@link ComponentResolver} can be used for components displayed in a
 * {@link TileContainerComponent} to ensure that the content of the corresponding {@link TileLayout}
 * is displayed, when the selection changes.
 * </p>
 * 
 * @see UpdateTileLayoutSelectionListener
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectableContextTileResolver extends ComponentResolver {

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		TileContainerComponent container = TileUtils.enclosingTileContainer(component);
		if (container == null) {
			// Not in a tile context.
			return;

		}
		ComponentChannel componentChannel = component.getChannel(SelectionChannel.NAME);
		componentChannel.addListener(new UpdateTileLayoutSelectionListener(container));
	}

}


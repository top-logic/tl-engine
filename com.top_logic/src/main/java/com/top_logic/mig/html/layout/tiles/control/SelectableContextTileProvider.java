/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link AbstractContextTileControlProvider} for {@link Selectable} components.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectableContextTileProvider<C extends SelectableContextTileProvider.Config<?>>
		extends AbstractContextTileControlProvider<C> {

	/**
	 * Configuration for a {@link SelectableContextTileProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends SelectableContextTileProvider<?>>
			extends AbstractContextTileControlProvider.Config<I> {
		// No properties here
	}

	/**
	 * Creates a new {@link SelectableContextTileProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SelectableContextTileProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected String getChannelName() {
		return SelectionChannel.NAME;
	}

	@Override
	protected ChannelListener createListener(TileContainerComponent enclosingTileContainer) {
		return (ComponentChannel sender, Object oldSelection, Object newSelection) -> {
			displayContent(enclosingTileContainer);
		};
	}

}

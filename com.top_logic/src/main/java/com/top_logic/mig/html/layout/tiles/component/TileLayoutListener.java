/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.mig.html.layout.tiles.TileLayout;

/**
 * {@link PropertyListener} that is informed, when the displayed {@link TileLayout} has changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileLayoutListener extends PropertyListener {

	/**
	 * {@link EventType} used to register a {@link TileLayoutListener} at a
	 * {@link TileContainerComponent}.
	 */
	EventType<TileLayoutListener, TileContainerComponent, TileLayout> TILE_LAYOUT_CHANGED =
		new EventType<>("tileLayoutChanged") {

			@Override
			public EventType.Bubble dispatch(TileLayoutListener listener,
					TileContainerComponent sender,
					TileLayout oldValue,
					TileLayout newValue) {
				return listener.layoutChanged(sender, oldValue, newValue);
			}
		
		};

	/**
	 * Handles the change of a displayed {@link TileLayout},i.e. the value of
	 * {@link TileContainerComponent#displayedLayout()} of the sender has changed.
	 * 
	 * @param sender
	 *        The component whose layout has changed.
	 * @param oldValue
	 *        The former layout.
	 * @param newValue
	 *        The current layout.
	 * 
	 * @return Whether the event should bubble.
	 */
	Bubble layoutChanged(TileContainerComponent sender, TileLayout oldValue, TileLayout newValue);

}


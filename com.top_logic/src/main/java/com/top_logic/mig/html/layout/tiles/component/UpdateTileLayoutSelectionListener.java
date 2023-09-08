/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;

/**
 * 
 * {@link ChannelListener} (e.g. for a {@link SelectionChannel}), that reacts on changes and adapts
 * the displayed {@link TileLayout}.
 * 
 * <p>
 * When the value changes to "non-null" than the content of the current displayed {@link TileRef} is
 * displayed; when it is set to "null", then the "context" for deeper tiles is not longer valid and
 * these tiles must not longer be displayed.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class UpdateTileLayoutSelectionListener implements ChannelListener {

	private final TileContainerComponent _container;

	/**
	 * Creates a new {@link UpdateTileLayoutSelectionListener}.
	 * 
	 * @param container
	 *        The {@link TileContainerComponent} ancestor of the {@link LayoutComponent} on which
	 *        {@link ComponentChannel} this listener is registered.
	 */
	public UpdateTileLayoutSelectionListener(TileContainerComponent container) {
		_container = container;
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		/* The step into the tile can not happen direct, because the displayed component may not be
		 * allowed yet. This happens, because ChannelListener are informed direct, but the inner
		 * component may be informed *after* this ChannelListener is triggered so that the component
		 * has still the old model and is not allowed yet. */
		LayoutContext lc = DefaultDisplayContext.getDisplayContext().getLayoutContext();
		lc.notifyInvalid(dc -> {
			// NOTE: oldValue is always null because legacy component event
			// ModelEventListener.MODEL_SELECTED is fired
			handleNewValueSelected(sender, newValue);
		});
	}

	private void handleNewValueSelected(ComponentChannel sender, Object newValue) {
		LayoutComponent sendingComp = sender.getComponent();
		TileLayout currentLayout = _container.displayedLayout();
		TileLayout newLayout;
		if (newValue != null) {
			if (TileUtils.displayedInTile(sendingComp, _container, currentLayout)) {
				newLayout = ((TileRef) currentLayout).getContentTile();
			} else {
				// The component whose selection is changed is not currently displayed.
				return;
			}
		} else {
			Set<LayoutComponent> ancestorsAndSelf = ancestorsAndSelf(sendingComp);
			List<TileLayout> displayedLayoutPath = _container.displayedLayoutPath();
			findLayoutForSender:
			{
				for (int i = displayedLayoutPath.size() - 1; i >= 0; i--) {
					TileLayout layout = displayedLayoutPath.get(i);
					if (ancestorsAndSelf.contains(_container.getTileComponent(layout))) {
						newLayout = layout;
						break findLayoutForSender;
					}
				}
				// tile for sender is currently not on the tile stack. Ignore change
				return;
			}

		}

		_container.displayTileLayout(newLayout);
	}

	private Set<LayoutComponent> ancestorsAndSelf(LayoutComponent comp) {
		LayoutComponent parent = comp.getParent();
		if (parent == null || parent == _container) {
			return Collections.singleton(comp);
		}
		Set<LayoutComponent> ancestorsAndSelf = new HashSet<>();
		ancestorsAndSelf.add(comp);
		do {
			ancestorsAndSelf.add(parent);
			parent = parent.getParent();
		} while (parent != null && comp != _container);
		return ancestorsAndSelf;
	}

}

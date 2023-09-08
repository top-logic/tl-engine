/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.Objects;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.mig.html.layout.FindFirstMatchingComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;

/**
 * {@link TilePreview} that expects a {@link TileLayout} for a {@link TileListComponent}. The result
 * list is <b>not</b> a list with a {@link ComponentTile} for the component, but a list containing a
 * {@link ComponentTile} for each option of the component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InlinedTileProvider extends AbstractTileProvider<InlinedTileProvider.Config> {

	private static final Property<SelectionChanged> LISTENER =
		TypedAnnotatable.property(SelectionChanged.class, "listener");

	private static final class SelectionChanged implements ChannelListener {

		private final TileContainerComponent _container;

		private TileRef _tileRef;

		public SelectionChanged(TileContainerComponent container) {
			_container = container;
		}

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			/* The step into the tile can not happen direct, because the displayed component may not
			 * be allowed yet. This happens, because ChannelListener are informed direct, but the
			 * inner component may be informed *after* this ChannelListener is triggered so that the
			 * component has still the old model and is not allowed yet. */
			LayoutContext lc = DefaultDisplayContext.getDisplayContext().getLayoutContext();
			lc.notifyInvalid(dc -> handleNewValueSelected(newValue));
		}

		private void handleNewValueSelected(Object newValue) {
			TileLayout currentLayout = _container.displayedLayout();
			TileLayout newLayout;
			if (newValue != null) {
				/* TileRef is inlined: Therefore the step into the content must occur when the
				 * container of the TileRef is displayed */
				if (currentLayout == _tileRef.container()) {
					newLayout = _tileRef.getContentTile();
				} else {
					// The component whose selection is changed is not currently on tile stack.
					return;
				}
			} else {
				List<TileLayout> displayedLayoutPath = _container.displayedLayoutPath();
				TileLayout displayedContainer = TileUtils.getContainer(_tileRef);
				if (displayedLayoutPath.indexOf(displayedContainer) != -1) {
					newLayout = displayedContainer;
				} else {
					// tile for sender is currently not on the tile stack. Ignore change
					return;
				}
			}

			_container.displayTileLayout(newLayout);
		}

		void setTile(TileRef tileRef) {
			Objects.requireNonNull(tileRef);
			_tileRef = tileRef;
		}

	}

	/**
	 * Typed configuration interface definition for {@link InlinedTileProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractTileProvider.Config<InlinedTileProvider> {

		@Override
		@BooleanDefault(true)
		boolean isComponentDisplayedInline();

	}

	/**
	 * Create a {@link InlinedTileProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InlinedTileProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public List<? extends ComponentTile> getDisplayTiles(ContainerComponentTile tile) {
		LayoutComponent tileComponent = tile.getTileComponent();
		TileListComponent component = FindFirstMatchingComponent.getFirstOfType(TileListComponent.class, tileComponent);
		TileContainerComponent container = tile.container();

		addSelectionListener(component, container, (TileRef) tile.getBusinessObject());

		return component.createAllTiles();
	}

	private void addSelectionListener(LayoutComponent tileComponent, TileContainerComponent container,
			TileRef tileRef) {
		SelectionChanged listener = tileComponent.get(LISTENER);
		if (listener == null) {
			listener = new SelectionChanged(container);
			tileComponent.getChannel(SelectionChannel.NAME).addListener(listener);
			tileComponent.set(LISTENER, listener);
		}
		/* Update represented TileLayout: It may be that the item is recreated (cloned), e.g. when
		 * the configuration of the parent layout it changed. */
		listener.setTile(tileRef);
	}

}

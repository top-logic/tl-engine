/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import javax.swing.event.ChangeListener;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link DecoratingLayoutControlProvider} for a {@link LayoutComponent} that serves as context
 * component for other components in {@link TileLayout}.
 * 
 * <p>
 * The created {@link LayoutControl} reacts on a {@link EventType property event} of the represented
 * component and steps into the corresponding {@link TileLayout}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractContextTileControlProvider<C extends AbstractContextTileControlProvider.Config<?>>
		extends DecoratingLayoutControlProvider<C> {

	/**
	 * Typed configuration interface definition for {@link AbstractContextTileControlProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AbstractContextTileControlProvider<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration of the {@link LayoutControlProvider} creating the actual
		 * {@link LayoutControl} for the component.
		 * 
		 * @return May be <code>null</code>.
		 */
		PolymorphicConfiguration<LayoutControlProvider> getProvider();

	}

	private LayoutControlProvider _innerProvider;

	/**
	 * Create a {@link AbstractContextTileControlProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractContextTileControlProvider(InstantiationContext context, C config) {
		super(context, config);
		_innerProvider = context.getInstance(config.getProvider());
	}

	/**
	 * @see com.top_logic.layout.structure.DecoratingLayoutControlProvider#createLayoutControl(com.top_logic.layout.structure.LayoutControlProvider.Strategy,
	 *      com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		return super.createLayoutControl(strategy, component);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		LayoutControl layout;
		if(_innerProvider != null ) {
			layout = _innerProvider.createLayoutControl(strategy, component);
		} else {
			layout = strategy.createDefaultLayout(component);
		}
		addContextTileListener(component, layout);
		return layout;
	}

	private void addContextTileListener(LayoutComponent component, LayoutControl layout) {
		TileContainerComponent tileContainerComponent = TileUtils.enclosingTileContainer(component);
		if (tileContainerComponent == null) {
			return;
		}
		layout.addListener(AbstractControlBase.ATTACHED_PROPERTY,
			new AttachedPropertyListener() {
				private String _name = getChannelName();
				private ChannelListener _listener = createListener(tileContainerComponent);

				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					ComponentChannel channel = tileContainerComponent.getChannel(_name);
					if (newValue) {
						channel.addListener(_listener);
					} else {
						channel.removeListener(_listener);
					}
				}
			});
	}

	/**
	 * This method expects that the current displayed {@link TileLayout} is a {@link TileRef}. In
	 * this case the content of the {@link TileRef} is displayed.
	 * 
	 * @param container
	 *        The {@link TileContainerComponent} in which the {@link TileRef} to step into is
	 *        currently displayed.
	 */
	protected void displayContent(TileContainerComponent container) {
		DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(
			displayContext -> {
				TileRef tileRef = (TileRef) container.displayedLayout();
				container.displayTileLayout(tileRef.getContentTile());
			});
	}

	/**
	 * The {@link EventType} representing the property to observe.
	 */
	protected abstract String getChannelName();

	/**
	 * Creates a {@link ChangeListener} that is called when the {@link LayoutComponent} provides a
	 * new value in its {@link #getChannelName()} channel.
	 * 
	 * @param enclosingTileContainer
	 *        The next enclosing {@link TileContainerComponent} of the {@link LayoutComponent} this
	 *        {@link LayoutControlProvider} creates a {@link LayoutControl} for.
	 */
	protected abstract ChannelListener createListener(TileContainerComponent enclosingTileContainer);

}

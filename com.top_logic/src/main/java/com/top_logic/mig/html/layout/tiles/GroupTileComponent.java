/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.editor.commands.AddTileCommand;
import com.top_logic.layout.editor.commands.DeleteComponentCommand;
import com.top_logic.layout.editor.commands.EditComponentCommand;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutList;
import com.top_logic.mig.html.layout.tiles.control.GroupTileControlProvider;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;

/**
 * {@link Layout} in a tile context.
 * 
 * <p>
 * A {@link GroupTileComponent} is the holder for inner components which are displayed as tile. The
 * information how the children are displayed as tiles can be found in the
 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getTileInfo()} of the child
 * component.
 * </p>
 * 
 * @see RootTileComponent
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Group of tiles")
public class GroupTileComponent extends LayoutList implements Selectable, BoundCheckerDelegate {

	/**
	 * Configuration of a {@link GroupTileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Layout.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ItemDefault(GroupTileControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			Layout.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(AddTileCommand.DEFAULT_COMMAND_ID);
			registry.registerCommand(EditComponentCommand.DEFAULT_COMMAND_ID);
			registry.registerCommand(DeleteComponentCommand.DEFAULT_COMMAND_ID);
		}

	}

	private final BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

	/**
	 * Creates a new {@link GroupTileComponent}.
	 */
	public GroupTileComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	/**
	 * Typed access to {@link #getSelected()}.
	 */
	public final LayoutComponent selectedComponent() {
		Object selected = getSelected();
		if (selected instanceof LayoutComponent) {
			return (LayoutComponent) selected;
		}
		return null;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Returns the actual component (or ancestor) contained in corresponding
	 * {@link RootTileComponent#displayedPath()} or <code>null</code>.
	 */
	public LayoutComponent getDisplayedComponent() {
		return RootTileComponent.getDisplayedComponent(this);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		selectionChannel().addListener(this::handleSelectionChanged);
	}

	/**
	 * Handles the change of {@link #getSelected()}
	 * 
	 * @param sender
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param oldValue
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param newValue
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 */
	private void handleSelectionChanged(ComponentChannel sender, Object oldValue, Object newValue) {
		if (oldValue instanceof LayoutComponent) {
			((LayoutComponent) oldValue).setVisible(false);
		}
		if (newValue instanceof LayoutComponent) {
			// child is visible iff this component is visible
			((LayoutComponent) newValue).setVisible(isVisible());
		}
	}

	@Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return CollectionUtil.singletonOrEmptyList(selectedComponent());
	}

	@Override
	protected void onRemove(int index, LayoutComponent removed) {
		if (removed == getSelected()) {
			clearSelection();
		}
		super.onRemove(index, removed);
	}

	@Override
	protected void onSet(List<LayoutComponent> oldChildren) {
		if (getSelected() != null) {
			if (oldChildren.contains(getSelected()) && !getChildList().contains(getSelected())) {
				clearSelection();
			}
		}
		super.onSet(oldChildren);
	}

	@Override
	public boolean makeVisible(LayoutComponent child) {
		boolean visible = makeVisible();
		if (visible) {
			setSelected(child);
		}
		return child.isVisible();
	}

	@Override
	protected void setChildVisibility(boolean visible) {
		LayoutComponent displayedComponent = selectedComponent();
		if (displayedComponent != null) {
			displayedComponent.setVisible(visible);
		}
	}

	@Override
	public boolean isOuterFrameset() {
		return false;
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}


/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * A {@link LayoutComponent} that offers a single selection channel.
 * 
 * <p>
 * Even if there is only a single selection channel, this channel may transmit a multi-selection. In
 * that case, the concrete type of {@link #getSelected()} is a {@link Collection} type.
 * </p>
 * 
 * <p>
 * Additionally, an implementation can optionally extends its configuration interface with
 * {@link SelectableConfig} and call {@link #linkSelectionChannel(Log)} from its
 * {@link LayoutComponent#linkChannels(Log)} method to allow explicitly wiring its selection channel
 * in the component configuration.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Selectable extends IComponent {

	/**
	 * Common configuration options for {@link LayoutComponent} implementing {@link Selectable}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface SelectableConfig extends IComponent.ComponentConfig {

		/** @see #getSelection() */
		String SELECTION = "selection";

		/** @see #getPartnerGroup() */
		String PARTNER_GROUP = "partnerGroup";

		/** @see #getDefaultSelection() */
		String DEFAULT_SELECTION = "defaultSelection";

		/**
		 * Whether the component always tries to select some object.
		 * 
		 * @see ListSelectionProvider
		 */
		@Name(DEFAULT_SELECTION)
		@BooleanDefault(true)
		boolean getDefaultSelection();

		/**
		 * The source of the component's selection.
		 * 
		 * @implNote This setting is not displayed for UI configuration, because the selection
		 *           channel is normally only used as source channel. To synchronize multiple
		 *           selection channels to the same value, {@link #getPartnerGroup()} can be used.
		 */
		@Name(SELECTION)
		@Hidden
		ModelSpec getSelection();

		/**
		 * @see #getSelection()
		 */
		void setSelection(ModelSpec value);

		/**
		 * Name to identify all {@link Selectable} components that should share a common selection.
		 * 
		 * <p>
		 * The name is arbitrary and only used to bind all selection channels of components that are
		 * in the same partner group to a single common source channel.
		 * </p>
		 * 
		 * <p>
		 * If a value is given, all selectable components with the same setting send and receive
		 * selection events to/from each other. This can be used to links the selection of otherwise
		 * independent components together.
		 * </p>
		 */
		@Name(PARTNER_GROUP)
		@Nullable
		String getPartnerGroup();

	}

	/**
	 * Value to return from {@link LayoutComponent#channels()} implementation.
	 */
	Map<String, ChannelSPI> MODEL_AND_SELECTION_CHANNEL =
		LayoutComponent.channels(LayoutComponent.MODEL_CHANNEL, SelectionChannel.INSTANCE);

	/**
	 * Set the given object as selected, if it is contained in the list.
	 * 
	 * @param selection
	 *        The new selection of this component. An argument of <code>null</code> is equivalent to
	 *        {@link #clearSelection()}.
	 * @return <code>true</code>, if this call changed the selection.
	 */
	default boolean setSelected(Object selection) {
		return selectionChannel().set(selection);
	}

	/**
	 * Return the currently selected object.
	 * 
	 * If there is no selected object or the component is in some invalid state the method will
	 * return <code>null</code>.
	 * 
	 * @return The currently selected object or <code>null</code>.
	 */
	default Object getSelected() {
		return selectionChannel().get();
	}

	/**
	 * Reset the Selection to "No selection".
	 * 
	 * <p>
	 * This is equivalent to {@link #setSelected(Object)} with <code>null</code>
	 * as argument.
	 * </p>
	 */
	default void clearSelection() {
		setSelected(null);
	}

	/**
	 * The {@link ComponentChannel} that represents the current selection.
	 */
	default ComponentChannel selectionChannel() {
		return getChannel(SelectionChannel.NAME);
	}

	/**
	 * Links the {@link #selectionChannel()} to configured sources.
	 */
	default void linkSelectionChannel(Log log) {
		Config layoutConfig = getConfig();
		if (layoutConfig instanceof SelectableConfig) {
			SelectableConfig config = (SelectableConfig) layoutConfig;

			ModelSpec selection = config.getSelection();
			if (selection != null) {
				LayoutComponent self = (LayoutComponent) this;
				ChannelLinking channelLinking = self.getChannelLinking(selection);
				selectionChannel().linkChannel(log, self, channelLinking);
			}

			// handle partnerGroups
			String groupName = config.getPartnerGroup();
			if (groupName != null) {
				ComponentChannel partnerChannel = getMainLayout().makePartnerChannel(groupName);
				selectionChannel().link(partnerChannel);
			}
		}
	}

}

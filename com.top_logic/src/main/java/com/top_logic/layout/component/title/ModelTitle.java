/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.title;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.DisplayChannelValue;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TitleProvider} displaying the label of the model of a component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelTitle<C extends ModelTitle.Config<?>> extends ConfiguredTitleProvider<C> {

	/**
	 * Configuration options for {@link ModelTitle}.
	 */
	@TagName("model-title")
	public interface Config<I extends ModelTitle<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link ModelSpec} to get the value to render from.
		 */
		@Mandatory
		ModelSpec getModel();

		/** @see #getModel() */
		void setModel(ModelSpec value);

		/**
		 * Optional {@link ResKey} to display the {@link #getModel()} value.
		 * 
		 * <p>
		 * If a value is set, it is translations are expected to contain a <code>{0}</code>
		 * placeholder where the {@link #getModel()} value should be dynamically inserted.
		 * </p>
		 * 
		 * <p>
		 * If no value is given, only the {@link #getModel()} value is displayed.
		 * </p>
		 */
		@Label("title")
		ResKey getKey();

		/** @see #getKey() */
		void setKey(ResKey value);
	}

	/**
	 * Creates a new {@link ModelTitle}.
	 */
	public ModelTitle(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createTitle(LayoutComponent component) {
		Protocol log = new LogProtocol(ModelTitle.class);
		ComponentChannel channel = ChannelLinking.resolveChannel(log, component, getConfig().getModel());
		if (log.hasErrors()) {
			return Fragments.empty();
		}
		return createTitle(channel);
	}

	@Override
	public ResKey getSimpleTitle(LayoutComponent.Config component) {
		return getConfig().getKey();
	}

	/**
	 * Creates the title for the given channel.
	 * 
	 * @param channel
	 *        The channel to derive title object for
	 * 
	 * @return A {@link HTMLFragment} displaying the title for the value of the given channel.
	 */
	protected HTMLFragment createTitle(ComponentChannel channel) {
		DisplayChannelValue control = new DisplayChannelValue(channel);
		control.setKey(getConfig().getKey());
		return control;
	}

}


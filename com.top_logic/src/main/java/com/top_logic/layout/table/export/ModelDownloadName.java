/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link DownloadNameProvider} generating filenames based on the model of a component.
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 */
public class ModelDownloadName<C extends ModelDownloadName.Config<?>> extends ConfiguredDownloadNameProvider<C> {

	/**
	 * Configuration options for {@link ModelDownloadName}.
	 */
	@TagName("model-download-name")
	public interface Config<I extends ModelDownloadName<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link ModelSpec} to get the value to render from.
		 */
		@Mandatory
		ModelSpec getModel();

		/** @see #getModel() */
		void setModel(ModelSpec value);
	}

	/**
	 * Creates a new {@link ModelDownloadName}.
	 */
	public ModelDownloadName(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public String createDownloadName(LayoutComponent component, ResKey resKey) {
		Protocol log = new LogProtocol(ModelDownloadName.class);
		ComponentChannel channel = ChannelLinking.resolveChannel(log, component, getConfig().getModel());
		if (log.hasErrors()) {
			return StringServices.EMPTY_STRING;
		}
		return createDownloadName(channel, resKey);
	}

	/**
	 * Creates the download-name for the given channel.
	 * 
	 * @param channel
	 *        The channel to derive download-name object for
	 * 
	 * @return A {@link HTMLFragment} displaying the download-name for the value of the given channel.
	 */
	protected String createDownloadName(ComponentChannel channel, ResKey key) {
		Object channelValue = channel.get();
		return Resources.getInstance().getMessage(key, channelValue);
	}

}
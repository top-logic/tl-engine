/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link DownloadNameProvider} generating filenames where the label of the configured model is
 * passed to use in res-key placeholder '{0}'.
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 */
public class ModelDownloadName<C extends ModelDownloadName.Config<?>> extends AbstractConfiguredInstance<C>
		implements DownloadNameProvider {

	/**
	 * Configuration options for {@link ModelDownloadName}.
	 */
	@TagName("model-download-name")
	public interface Config<I extends ModelDownloadName<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link ModelSpec} to get the model whose label is used for the dynamic part of the
		 * filename.
		 */
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
		Object model = getModel(component);
		return createDownloadName(model, resKey);
	}

	private Object getModel(LayoutComponent component) {
		ModelSpec modelSpec = getConfig().getModel();
		if (modelSpec != null) {
			Protocol log = new LogProtocol(ModelDownloadName.class);
			ComponentChannel channel = ChannelLinking.resolveChannel(log, component, modelSpec);
			if (log.hasErrors()) {
				return null;
			}
			return channel.get();
		} else {
			return component.getModel();
		}
	}

	/**
	 * Creates the download-name for the given model.
	 * 
	 * @param model
	 *        The model to create the download-name for
	 * @param key
	 *        the key with the placeholder '{0}' to be replaced by the dynamic part of the name.
	 * 
	 * @return The translated label for the given key using the given model as single parameter.
	 */
	protected String createDownloadName(Object model, ResKey key) {
		return Resources.getInstance().getMessage(key, model);
	}

}
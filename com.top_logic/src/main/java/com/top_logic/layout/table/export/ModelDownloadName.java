/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;

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

		/** Configuration name of {@link #getDownloadNameTemplate()}. */
		String DOWNLOAD_NAME_TEMPLATE = "downloadNameTemplate";

		/**
		 * Template for the download name containing a placeholder '{0}' that is filled with the
		 * model name.
		 */
		@Name(DOWNLOAD_NAME_TEMPLATE)
		@Mandatory
		ResKey1 getDownloadNameTemplate();

		/**
		 * The {@link ModelSpec} to get the model whose label is used for the dynamic part of the
		 * filename.
		 * 
		 * <p>
		 * If not set, the target model of the export command is used instead. This is typically the
		 * model of the underlying component.
		 * </p>
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
	public ResKey createDownloadName(LayoutComponent component, Object model) {
		Object modelForName;

		ModelSpec modelSpec = getConfig().getModel();
		if (modelSpec != null) {
			modelForName = ChannelLinking.eval(component, modelSpec);
		} else {
			modelForName = model;
		}

		return createDownloadName(modelForName);
	}

	/**
	 * Creates the download-name for the given model.
	 * 
	 * @param model
	 *        The model to create the download-name for
	 * @return The translated label for the given key using the given model as single parameter.
	 */
	protected ResKey createDownloadName(Object model) {
		return getConfig().getDownloadNameTemplate().fill(model);
	}

}
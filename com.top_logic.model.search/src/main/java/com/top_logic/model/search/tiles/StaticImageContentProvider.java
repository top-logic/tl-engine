/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link AbstractPreviewContent} delivering a static configured image.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class StaticImageContentProvider extends AbstractPreviewContent<StaticImageContentProvider.Config> {

	/**
	 * Typed configuration interface definition for {@link StaticImageContentProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractPreviewContent.Config<StaticImageContentProvider> {
		/**
		 * Image to display in the content area of the tile preview.
		 */
		@Mandatory
		ThemeImage getImage();
	}

	/**
	 * Create a {@link StaticImageContentProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StaticImageContentProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		return imagePreview(getConfig().getImage());
	}

}


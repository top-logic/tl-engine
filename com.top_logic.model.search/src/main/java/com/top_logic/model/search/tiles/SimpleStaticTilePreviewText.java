/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;

/**
 * {@link TilePreviewPartProvider.Text} computing the text using an configured {@link ResKey}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class SimpleStaticTilePreviewText extends AbstractConfiguredInstance<SimpleStaticTilePreviewText.Config>
		implements TilePreviewPartProvider.Text {

	/**
	 * Typed configuration interface definition for {@link SimpleStaticTilePreviewText}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<SimpleStaticTilePreviewText> {

		/**
		 * {@link ResKey} for the internationalized text.
		 * 
		 * <p>
		 * The {@link ResKey} may have one free variable ({0}) which is filled with the default
		 * translation of the preview model.
		 * </p>
		 */
		@Mandatory
		@ItemDisplay(ItemDisplayType.MONOMORPHIC)
		ResKey1 getText();
		
	}

	/**
	 * Create a {@link SimpleStaticTilePreviewText}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SimpleStaticTilePreviewText(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		return Fragments.message(getConfig().getText().fill(model));
	}
}


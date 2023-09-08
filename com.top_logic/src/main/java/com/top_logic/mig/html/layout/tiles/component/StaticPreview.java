/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.tiles.TileLayout;

/**
 * {@link LabelBasedPreview} with an additional {@link ResKey} that is displayed as description.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class StaticPreview<C extends StaticPreview.Config> extends LabelBasedPreview<C> {

	/**
	 * Configuration of a {@link StaticPreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LabelBasedPreview.Config<StaticPreview<?>> {

		/** Configuration name for the value of {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/**
		 * A more specific description of the {@link TileLayout}.
		 * 
		 * @return May be <code>null</code>.
		 */
		@Name(DESCRIPTION)
		ResKey getDescription();

		/**
		 * Setter for {@link #getDescription()}.
		 */
		void setDescription(ResKey key);

	}
	
	/**
	 * Creates a new {@link StaticPreview}.
	 */
	public StaticPreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		ResKey description = getConfig().getDescription();
		if (description != null) {
			return message(description);
		} else {
			return empty();
		}
	}

	/**
	 * Creates a configuration for a {@link StaticPreview} with the given icon and description.
	 * 
	 * @param icon
	 *        May be <code>null</code>, when no icon shall be displayed.
	 * @param description
	 *        May be <code>null</code>, when no description shall be displayed.
	 */
	public static StaticPreview.Config newStaticPreview(ThemeImage icon, ResKey description) {
		Config staticPreview = TypedConfiguration.newConfigItem(Config.class);
		staticPreview.setIcon(icon);
		staticPreview.setDescription(description);
		return staticPreview;
	}

}


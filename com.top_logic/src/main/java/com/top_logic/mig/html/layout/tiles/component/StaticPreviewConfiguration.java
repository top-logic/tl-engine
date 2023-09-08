/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link ConfigurationItem} used to collect necessary data to create a new {@link StaticPreview}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	LabelConfiguration.LABEL_NAME,
	StaticPreviewConfiguration.DESCRIPTION_NAME,
	StaticPreviewConfiguration.IMAGE_NAME,
})
public interface StaticPreviewConfiguration extends LabelConfiguration {

	/** Configuration name for the value of the {@link #getDescription()}. */
	String DESCRIPTION_NAME = "description";

	/** Configuration name for the value of the {@link #getImage()}. */
	String IMAGE_NAME = "image";

	/**
	 * The {@link ResKey#text(String) literal text description} string in the preview.
	 * 
	 * @see StaticPreview.Config#getDescription()
	 */
	@Name(DESCRIPTION_NAME)
	String getDescription();

	/**
	 * The {@link ThemeImage icon} for the preview.
	 * 
	 * @return An icon for the {@link StaticPreview}. May be <code>null</code>.
	 * 
	 * @see StaticPreview.Config#getIcon()
	 */
	@Name(IMAGE_NAME)
	ThemeImage getImage();

}


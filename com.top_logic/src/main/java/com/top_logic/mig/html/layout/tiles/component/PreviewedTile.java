/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.tiles.TileLayout;

/**
 * {@link TileLayout} that has a configured {@link TilePreview}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PreviewedTile extends ConfigurationItem {

	/**
	 * Name of the option to configure value of {@link #getPreview()}.
	 */
	String PREVIEW_NAME = "preview";

	/**
	 * The {@link TilePreview} that is used to create a peview for this tile.
	 */
	@Name(PREVIEW_NAME)
	PolymorphicConfiguration<? extends TilePreview> getPreview();

	/**
	 * Sets the value of {@link #getPreview()}.
	 */
	void setPreview(PolymorphicConfiguration<? extends TilePreview> preview);

}


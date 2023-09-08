/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;

/**
 * Provider that delivers a part of the {@link ModelBasedTilePreview}.
 * 
 * @see Text
 * @see Content
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TilePreviewPartProvider {

	/**
	 * Creates the part of the preview to display.
	 * 
	 * @param model
	 *        The model to create preview part for.
	 */
	HTMLFragment createPreviewPart(Object model);

	/**
	 * {@link TilePreviewPartProvider} that creates a textual part of the tile preview, either the
	 * label or the description.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	interface Text extends TilePreviewPartProvider {
		// marker interface
	}

	/**
	 * {@link TilePreviewPartProvider} that creates the actual content area of the tile preview.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	interface Content extends TilePreviewPartProvider {
		// marker interface
	}
}


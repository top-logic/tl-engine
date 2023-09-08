/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryDataSource;

/**
 * It represents a part of an {@link GalleryModel}, that holds references to an image and its
 * thumbnail (minified preview version).
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface GalleryImage extends Named {
	
	/**
	 * Name of the {@link GalleryImage}.
	 */
	@Override
	String getName();

	/**
	 * image in regular resolution.
	 */
	BinaryDataSource getImage();

	/**
	 * image in minified resolution, maybe <code>null</code> if not available.
	 */
	BinaryDataSource getThumbnail();

}

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
	 * Whether a value holding gallery images in the given way is a gallery: several images in an
	 * order of their own.
	 *
	 * <p>
	 * Only a gallery stores images uploaded into it as images of its own and deletes the images it no
	 * longer holds; a single gallery image, and a set of them without order, are plain references to
	 * images that exist already. The storage of a model attribute and the field editing it follow
	 * this rule alike.
	 * </p>
	 *
	 * @param multiple
	 *        Whether the value holds several gallery images rather than one.
	 * @param ordered
	 *        Whether the order of the gallery images is part of the value.
	 */
	static boolean isGallery(boolean multiple, boolean ordered) {
		return multiple && ordered;
	}
	
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

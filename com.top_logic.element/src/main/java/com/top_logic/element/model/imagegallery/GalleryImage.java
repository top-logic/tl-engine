/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.imagegallery;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.model.imagegallery.impl.GalleryImageBase;

/**
 * Interface for {@link #GALLERY_IMAGE_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceTemplateGenerator}
 */
public interface GalleryImage extends com.top_logic.element.model.imagegallery.impl.GalleryImageBase,
		com.top_logic.layout.image.gallery.GalleryImage {

	/**
	 * @implNote Overridden to resolve conflict that both interfaces,
	 *           {@link com.top_logic.element.model.imagegallery.impl.GalleryImageBase} and
	 *           {@link com.top_logic.layout.image.gallery.GalleryImage}, define the same method.
	 * 
	 * @see com.top_logic.model.TLNamed#getName()
	 */
	@Override
	default String getName() {
		return GalleryImageBase.super.getName();
	}

	/**
	 * @implNote Overridden to resolve conflict that both interfaces,
	 *           {@link com.top_logic.element.model.imagegallery.impl.GalleryImageBase} and
	 *           {@link com.top_logic.layout.image.gallery.GalleryImage}, define the same method.
	 * 
	 * @see com.top_logic.element.model.imagegallery.impl.GalleryImageBase#getImage()
	 */
	@Override
	default BinaryData getImage() {
		return GalleryImageBase.super.getImage();
	}

	/**
	 * @implNote Overridden to resolve conflict that both interfaces,
	 *           {@link com.top_logic.element.model.imagegallery.impl.GalleryImageBase} and
	 *           {@link com.top_logic.layout.image.gallery.GalleryImage}, define the same method.
	 * 
	 * @see com.top_logic.element.model.imagegallery.impl.GalleryImageBase#getThumbnail()
	 */
	@Override
	default BinaryData getThumbnail() {
		return GalleryImageBase.super.getThumbnail();
	}

}

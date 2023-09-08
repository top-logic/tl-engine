/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.List;

import com.top_logic.layout.DisplayDimension;

/**
 * Default implementation of {@link GalleryModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultGalleryModel extends AbstractGalleryModel {

	private List<GalleryImage> _images;

	/**
	 * Create a new {@link DefaultGalleryModel}.
	 */
	public DefaultGalleryModel(List<GalleryImage> images, DisplayDimension width, DisplayDimension height) {
		super(width, height);
		setImages(images);
	}

	/**
	 * Create a new {@link DefaultGalleryModel} with default size.
	 */
	public DefaultGalleryModel(List<GalleryImage> images) {
		setImages(images);
	}

	@Override
	public List<GalleryImage> getImages() {
		return _images;
	}

	@Override
	public void internalSetImages(List<GalleryImage> images) {
		_images = images;
	}
}

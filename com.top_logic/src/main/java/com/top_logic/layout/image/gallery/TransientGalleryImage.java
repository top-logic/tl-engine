/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link GalleryImage}, that holds it's data in memory only.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TransientGalleryImage implements GalleryImage {

	private BinaryData _imageData;
	private BinaryData _thumbnailData;

	/**
	 * Create a new {@link GalleryImage} with regular image and it's thumbnail.
	 * 
	 * @param imageData
	 *        - must not be <code>null</code>
	 * @param thumbnailData
	 *        - may be <code>null</code>
	 */
	public TransientGalleryImage(BinaryData imageData, BinaryData thumbnailData) {
		assertNonEmptyImage(imageData);
		_imageData = imageData;
		_thumbnailData = thumbnailData;
	}

	/**
	 * Create a new {@link GalleryImage} with regular image only.
	 * 
	 * @param imageData
	 *        - must not be <code>null</code>
	 */
	public TransientGalleryImage(BinaryData imageData) {
		this(imageData, null);
	}

	private void assertNonEmptyImage(BinaryData imageData) {
		assert imageData != null : "Image data must not be empty!";
	}

	@Override
	public BinaryData getThumbnail() {
		return _thumbnailData;
	}

	@Override
	public String getName() {
		return _imageData.getName();
	}

	@Override
	public BinaryData getImage() {
		return _imageData;
	}
}

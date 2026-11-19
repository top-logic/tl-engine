/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.layout.image.gallery.TransientGalleryImage;

/**
 * How the values of a field relate to the images a {@link ReactImageFieldControl} shows and
 * uploads.
 *
 * <p>
 * The control works on images alone: it asks this mapping for the image a value of the field shows
 * and for the value an uploaded file becomes. The values themselves are what the field holds - the
 * files of a binary attribute, the images of a gallery.
 * </p>
 */
public interface ImageValues {

	/**
	 * Values that are the image files themselves, {@link BinaryDataSource}s.
	 */
	ImageValues FILES = new ImageValues() {
		@Override
		public BinaryDataSource imageOf(Object value) {
			return value instanceof BinaryDataSource file ? file : null;
		}

		@Override
		public Object valueOf(BinaryData file) {
			return file;
		}
	};

	/**
	 * Values that are the images of a gallery, {@link GalleryImage}s.
	 *
	 * <p>
	 * An uploaded file becomes a {@link TransientGalleryImage}; the storage of the gallery makes it
	 * persistent when the value is stored.
	 * </p>
	 */
	ImageValues GALLERY = new ImageValues() {
		@Override
		public BinaryDataSource imageOf(Object value) {
			return value instanceof GalleryImage image ? image.getImage() : null;
		}

		@Override
		public Object valueOf(BinaryData file) {
			return new TransientGalleryImage(file);
		}
	};

	/**
	 * The image the given value of the field shows.
	 *
	 * @param value
	 *        A single value of the field.
	 * @return The image, <code>null</code> if the value is none this mapping knows or has no image.
	 */
	BinaryDataSource imageOf(Object value);

	/**
	 * The value of the field the given uploaded file becomes.
	 *
	 * @param file
	 *        The uploaded image file.
	 * @return The value to put into the field.
	 */
	Object valueOf(BinaryData file);

}

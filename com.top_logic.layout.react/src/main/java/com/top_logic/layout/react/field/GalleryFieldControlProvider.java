/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.layout.react.control.form.ImageValues;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * Edits the images of a gallery, values of type {@link GalleryImage}, in an image field.
 *
 * <p>
 * The same image field that edits image files, see {@link ImageFieldControlProvider}: each image of
 * the gallery is a preview, and an uploaded file becomes a new image of the gallery. The file picker
 * offers images unless the field states the accepted types itself.
 * </p>
 *
 * <p>
 * It {@link #appliesTo(FieldSpec) applies} to a gallery only, several images in an order of their
 * own: a single gallery image or a set of them without order is a plain reference, chosen from the
 * existing images.
 * </p>
 *
 * @see FieldControlRegistry#GALLERY
 */
@Label("Image gallery")
@InApp
@TargetType(value = TLTypeKind.REF, name = GalleryFieldControlProvider.GALLERY_IMAGE_TYPE)
public class GalleryFieldControlProvider extends ImageFieldControlProvider {

	/** The qualified name of the model type of a gallery image. */
	public static final String GALLERY_IMAGE_TYPE = "tl.imagegallery:GalleryImage";

	/** The file types the file picker offers for a gallery. */
	public static final String IMAGE_TYPES = "image/*";

	/**
	 * Creates a {@link GalleryFieldControlProvider}.
	 */
	public GalleryFieldControlProvider() {
		super(ImageValues.GALLERY, IMAGE_TYPES);
	}

	/**
	 * Applies only to a gallery, see {@link GalleryImage#isGallery(boolean, boolean)}: only a gallery
	 * stores the images uploaded into it.
	 */
	@Override
	public boolean appliesTo(FieldSpec field) {
		return GalleryImage.isGallery(field.isMultiple(), field.isOrdered());
	}

}

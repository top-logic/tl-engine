/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.imagegallery;

/**
 * Factory for <code>tl.imagegallery</code> objects.
 * 
 * <p>
 * Note: this is generated code. Do not modify. Instead, create a subclass and register this in the module system.
 * </p>
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.FactoryGenerator}
 */
public class ImageGalleryFactory extends com.top_logic.element.meta.kbbased.AbstractElementFactory {

	/**
	 * Name of the structure <code>tl.imagegallery</code> defined by {@link ImageGalleryFactory}.
	 */
	public static final String TL_IMAGEGALLERY_STRUCTURE = "tl.imagegallery";

	/**
	 * Lookup {@link GalleryImage} type.
	 */
	public static com.top_logic.model.TLClass getGalleryImageType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(TL_IMAGEGALLERY_STRUCTURE).getType(GalleryImage.GALLERY_IMAGE_TYPE);
	}

	/**
	 * Lookup {@link GalleryImage#IMAGE_ATTR} of {@link GalleryImage}.
	 */
	public static com.top_logic.model.TLProperty getImageGalleryImageAttr() {
		return (com.top_logic.model.TLProperty) getGalleryImageType().getPart(GalleryImage.IMAGE_ATTR);
	}

	/**
	 * Lookup {@link GalleryImage#NAME_ATTR} of {@link GalleryImage}.
	 */
	public static com.top_logic.model.TLProperty getNameGalleryImageAttr() {
		return (com.top_logic.model.TLProperty) getGalleryImageType().getPart(GalleryImage.NAME_ATTR);
	}

	/**
	 * Lookup {@link GalleryImage#THUMBNAIL_ATTR} of {@link GalleryImage}.
	 */
	public static com.top_logic.model.TLProperty getThumbnailGalleryImageAttr() {
		return (com.top_logic.model.TLProperty) getGalleryImageType().getPart(GalleryImage.THUMBNAIL_ATTR);
	}

	/**
	 * Name of type <code>GalleryImage</code> in structure {@link #TL_IMAGEGALLERY_STRUCTURE}.
	 * 
	 * @deprecated Use {@link GalleryImage#GALLERY_IMAGE_TYPE}.
	 */
	@Deprecated
	public static final String GALLERY_IMAGE_NODE = GalleryImage.GALLERY_IMAGE_TYPE;

	/**
	 * Storage table name of {@link #GALLERY_IMAGE_NODE} objects.
	 */
	public static final String KO_NAME_GALLERY_IMAGE = "GalleryImage";


	/**
	 * Create an instance of {@link GalleryImage} type.
	 */
	public final GalleryImage createGalleryImage(com.top_logic.model.TLObject context) {
		return (GalleryImage) createObject(getGalleryImageType(), context);
	}

	/**
	 * Create an instance of {@link GalleryImage} type.
	 */
	public final GalleryImage createGalleryImage() {
		return createGalleryImage(null);
	}

	/**
	 * The singleton instance of {@link ImageGalleryFactory}.
	 */
	public static ImageGalleryFactory getInstance() {
		return (ImageGalleryFactory) com.top_logic.element.model.DynamicModelService.getFactoryFor(TL_IMAGEGALLERY_STRUCTURE);
	}
}

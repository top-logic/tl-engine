/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;

/**
 * Model of an image gallery.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface GalleryModel extends PropertyObservable, NamedModel, TypedAnnotatable {

	/**
	 * {@link EventType}, for observing {@link GalleryModel#getImages()} changes.
	 * 
	 * @see PropertyObservable#addListener(EventType, PropertyListener)
	 */
	EventType<GalleryModelListener, GalleryModel, List<GalleryImage>> IMAGES_PROPERTY =
		new EventType<>("images") {

			@Override
			public EventType.Bubble dispatch(GalleryModelListener listener,
					GalleryModel sender, List<GalleryImage> oldValue, List<GalleryImage> newValue) {
				listener.notifyImagesChanged(sender, oldValue, newValue);
				return Bubble.CANCEL_BUBBLE;
			}

		};

	/**
	 * the image, which shall be displayed first, when showing this {@link GalleryModel} at
	 *         GUI.
	 * 
	 * @throws AssertionError
	 *         if {@link #getImages()} is empty
	 * 
	 * @see #hasMainImage()
	 */
	GalleryImage getMainImage();

	/**
	 * true, if this {@link GalleryModel} has a main image, due to {@link #getImages()} is
	 *         not empty.
	 */
	boolean hasMainImage();

	/**
	 * Main image of this {@link GalleryModel}, must be part of {@link #getImages()}, <b>must
	 * not</b> be <code>null</code>.
	 * 
	 * @see #getMainImage()
	 * 
	 * @throws IllegalArgumentException
	 *         if main image is not part of {@link #getImages()}
	 */
	void setMainImage(GalleryImage mainImage);

	/**
	 * images of the gallery in their order.
	 */
	List<GalleryImage> getImages();

	/**
	 * Set the images of this {@link GalleryModel}, whereby the first image of the list becomes the
	 * main image, <b>must not</b> be <code>null</code>.
	 * 
	 * @see #getImages()
	 * @see #getMainImage()
	 */
	void setImages(List<GalleryImage> images);

	/**
	 * {@link GalleryViewConfiguration view configuration} of this {@link GalleryModel}.
	 */
	GalleryViewConfiguration getViewConfiguration();

}

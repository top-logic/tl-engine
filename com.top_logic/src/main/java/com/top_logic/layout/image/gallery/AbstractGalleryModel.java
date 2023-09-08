/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.List;

import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * Base class for implementations of {@link GalleryModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractGalleryModel extends PropertyObservableBase
		implements GalleryModel, LazyTypedAnnotatableMixin {

	private GalleryViewConfiguration _galleryViewConfiguration;

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	/**
	 * Create a new {@link AbstractGalleryModel}.
	 */
	public AbstractGalleryModel() {
		_galleryViewConfiguration = new GalleryViewConfiguration();
	}

	/**
	 * Create a new {@link AbstractGalleryModel}, with a given gallery size.
	 */
	public AbstractGalleryModel(DisplayDimension width, DisplayDimension height) {
		_galleryViewConfiguration = new GalleryViewConfiguration();
		_galleryViewConfiguration.setGalleryWidth(width);
		_galleryViewConfiguration.setGalleryHeight(height);
	}

	@Override
	public GalleryImage getMainImage() {
		assert getImages().size() > 0 : "Empty gallery model do not have a main image!";
		return getImages().get(0);
	}

	@Override
	public boolean hasMainImage() {
		return getImages().size() > 0;
	}

	@Override
	public void setMainImage(GalleryImage mainImage) {
		List<GalleryImage> oldImages = getImages();
		if (getImages().remove(mainImage)) {
			getImages().add(0, mainImage);
			notifyListeners(IMAGES_PROPERTY, this, oldImages, getImages());
		} else {
			StringBuilder imageNames = new StringBuilder();
			for (int i = 0; i < getImages().size(); i++) {
				GalleryImage image = getImages().get(i);
				if (i > 0) {
					imageNames.append(", ");
				}
				imageNames.append(image.getName());
			}
			throw new IllegalArgumentException(
				"Main image '" + mainImage.getName() + "' must be part of image list: [" + imageNames.toString() + "]");
		}
	}

	@Override
	public final void setImages(List<GalleryImage> images) {
		assert images != null : "Images must not be empty!";
		List<GalleryImage> oldImages = getImages();
		internalSetImages(images);
		notifyListeners(IMAGES_PROPERTY, this, oldImages, images);
	}

	/**
	 * Store {@link GalleryImage}s in internal data structure (e.g. field).
	 */
	protected abstract void internalSetImages(List<GalleryImage> images);

	@Override
	public abstract List<GalleryImage> getImages();

	@Override
	public GalleryViewConfiguration getViewConfiguration() {
		return _galleryViewConfiguration;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}
}
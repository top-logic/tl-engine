/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.model.imagegallery.GalleryImage;
import com.top_logic.element.model.imagegallery.ImageGalleryFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.image.gallery.TransientGalleryImage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ListStorage}, that stores a list of {@link GalleryImage}s. In addition to regular
 * {@link ListStorage} it creates image wrapper on demand, if formerly transient images shall be
 * stored.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ImageGalleryStorage<C extends ImageGalleryStorage.Config<?>> extends ListStorage<C> {

	/**
	 * Configuration options for {@link ImageGalleryStorage}.
	 */
	@TagName("gallery-storage")
	public interface Config<I extends ImageGalleryStorage<?>> extends ListStorage.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Create a new {@link ImageGalleryStorage}.
	 */
	public <PersistentGalleryImage extends com.top_logic.element.model.imagegallery.GalleryImage> ImageGalleryStorage(
			InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void checkBasicValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws IllegalArgumentException {
		if (!(aValue instanceof com.top_logic.layout.image.gallery.GalleryImage)) {
			throw new IllegalArgumentException("Expected value of type GalleryImage in attribute '"
				+ attribute.getName() + "'.");
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object newObject)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		Pair<List<GalleryImage>, HashSet<GalleryImage>> imageModifications =
			createImageModifications(aMetaAttributed, attribute, newObject);
		storeImageList(aMetaAttributed, attribute, imageModifications.getFirst());
		deleteUnreferencedImages(imageModifications.getSecond());
	}

	private void storeImageList(TLObject object, TLStructuredTypePart attribute, List<GalleryImage> transformedImages)
			throws NoSuchAttributeException {
		super.internalSetAttributeValue(object, attribute, transformedImages);
	}

	@SuppressWarnings("unchecked")
	private Pair<List<GalleryImage>, HashSet<GalleryImage>> createImageModifications(
			TLObject object, TLStructuredTypePart attribute, Object newObject) {
		List<com.top_logic.layout.image.gallery.GalleryImage> newImageList =
			(List<com.top_logic.layout.image.gallery.GalleryImage>) newObject;
		HashSet<GalleryImage> unreferencedImages =
			new HashSet<>((Collection<GalleryImage>) object.tValue(attribute));
		List<GalleryImage> persistentImages =
			new ArrayList<>(newImageList.size());

		for (int i = 0; i < newImageList.size(); i++) {
			com.top_logic.layout.image.gallery.GalleryImage image = newImageList.get(i);
			if (isTransientImage(image)) {
				persistentImages.add(i, createPersistentImage(image));
			} else {
				persistentImages.add(i, castToPersistentImage(image));
				unreferencedImages.remove(image);
			}
		}

		return new Pair<>(persistentImages, unreferencedImages);
	}

	private boolean isTransientImage(Object image) {
		return image instanceof TransientGalleryImage;
	}

	private GalleryImage createPersistentImage(Object image) {
		TransientGalleryImage transientImage = (TransientGalleryImage) image;
		ImageGalleryFactory instance = ImageGalleryFactory.getInstance();
		GalleryImage persistentImage = instance.createGalleryImage();
		persistentImage.setImage(transientImage.getImage());
		persistentImage.setThumbnail(transientImage.getThumbnail());
		persistentImage.setName(transientImage.getName());
		return persistentImage;
	}

	private GalleryImage castToPersistentImage(com.top_logic.layout.image.gallery.GalleryImage image) {
		return (GalleryImage) image;
	}

	private void deleteUnreferencedImages(HashSet<GalleryImage> unreferencedImages) {
		KBUtils.deleteAll(unreferencedImages);
	}

	@Override
	public boolean supportsLiveCollectionsInternal() {
		return false;
	}

	@Override
	public List<?> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		throw new UnsupportedOperationException(
			"Cannot create a live-collection for attribute " + qualifiedName(attribute) + ".");
	}

	/**
	 * Creates a configuration for the {@link ImageGalleryStorage}.
	 */
	public static Config<?> imageGalleryConfig() {
		return defaultConfig(Config.class, null, HistoryType.CURRENT);
	}

}

/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.image.gallery.AbstractGalleryModel;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.layout.image.gallery.GalleryModel;
import com.top_logic.layout.image.gallery.GalleryViewConfiguration;

/**
 * Adaptor for adding named {@link GalleryModel}s to {@link FormGroup}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryField extends AbstractFormField implements GalleryModel {

	@Inspectable
	private GalleryModel _galleryModel;

	/**
	 * Create a new {@link GalleryField}.
	 */
	protected GalleryField(String name, DisplayDimension width, DisplayDimension height) {
		super(name, !MANDATORY, !IMMUTABLE, NORMALIZE, NO_CONSTRAINT);
		_galleryModel = new InternalGalleryModel(this, width, height);
		installEventForward();
	}

	/**
	 * Create a new {@link GalleryField}.
	 */
	protected GalleryField(String name) {
		super(name, !MANDATORY, !IMMUTABLE, NORMALIZE, NO_CONSTRAINT);
		_galleryModel = new InternalGalleryModel(this);
		installEventForward();
	}

	private void installEventForward() {
		_galleryModel.addListener(GalleryModel.GLOBAL_LISTENER_TYPE, new GenericPropertyListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue,
					Object newValue) {
				if (type != IMAGES_PROPERTY) {
					GalleryField.this.firePropertyChanged((EventType) type, sender, oldValue, newValue);
				}
				return Bubble.BUBBLE;
			}
		});
		addValueListener(new ValueListener() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				GalleryField.this.firePropertyChanged((EventType) IMAGES_PROPERTY, field, oldValue, newValue);
			}
		});
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitGalleryField(this, arg);
	}

	@Override
	public GalleryImage getMainImage() {
		return _galleryModel.getMainImage();
	}

	@Override
	public boolean hasMainImage() {
		return _galleryModel.hasMainImage();
	}

	@Override
	public void setMainImage(GalleryImage mainImage) {
		_galleryModel.setMainImage(mainImage);
	}

	@Override
	public void setImages(List<GalleryImage> images) {
		_galleryModel.setImages(images);
	}

	@Override
	public List<GalleryImage> getImages() {
		return _galleryModel.getImages();
	}

	@Override
	public GalleryViewConfiguration getViewConfiguration() {
		return _galleryModel.getViewConfiguration();
	}

	@Override
	protected Object parseRawValue(Object rawValue) throws CheckException {
		return rawValue;
	}

	@Override
	protected Object unparseValue(Object value) {
		return value;
	}

	@Override
	protected Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException {
		if (value == null) {
			return Collections.emptyList();
		}
		checkForImageList(value);
		return value;
	}

	@SuppressWarnings("unchecked")
	private void checkForImageList(Object value) {
		Iterator<Object> galleryIterator = ((List<Object>) value).iterator();
		while (galleryIterator.hasNext()) {
			GalleryImage.class.cast(galleryIterator.next());
		}
	}

	private static class InternalGalleryModel extends AbstractGalleryModel {

		private GalleryField _valueProvider;

		InternalGalleryModel(GalleryField valueProvider) {
			_valueProvider = valueProvider;
		}

		InternalGalleryModel(GalleryField valueProvider, DisplayDimension width, DisplayDimension height) {
			super(width, height);
			_valueProvider = valueProvider;
		}

		@Override
		public void internalSetImages(List<GalleryImage> images) {
			_valueProvider.setValue(images);
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<GalleryImage> getImages() {
			return (List<GalleryImage>) _valueProvider.getValue();
		}
	}
}

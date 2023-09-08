/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.tag.AbstractFormFieldControlTag;

/**
 * Displays a {@link GalleryField}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryTag extends AbstractFormFieldControlTag {

	private DisplayDimension width;

	private DisplayDimension height;

	/**
	 * JSP tag attribute setter.
	 * 
	 * @param width
	 *        width of gallery in px.
	 */
	public void setWidth(String width) {
		initWidth(DisplayDimension.parseDimension(width));
	}

	/**
	 * Internal setter for the width attribute.
	 */
	public void initWidth(DisplayDimension value) {
		this.width = value;
	}

	/**
	 * JSP tag attribute setter.
	 * 
	 * @param height
	 *        height of gallery in px.
	 */
	public void setHeight(String height) {
		initHeight(DisplayDimension.parseDimension(height));
	}

	/**
	 * Internal setter for the height attribute.
	 */
	public void initHeight(DisplayDimension value) {
		this.height = value;
	}

	@SuppressWarnings("hiding")
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		GalleryField galleryField = (GalleryField) member;
		if (hasIndividualSize()) {
			GalleryViewConfiguration galleryViewConfiguration = galleryField.getViewConfiguration();
			DisplayDimension width = getSizeToUse(this.width, galleryViewConfiguration.getGalleryWidth());
			DisplayDimension height = getSizeToUse(this.height, galleryViewConfiguration.getGalleryHeight());
			galleryViewConfiguration.setGalleryWidth(width);
			galleryViewConfiguration.setGalleryHeight(height);
		}

		return new GalleryControl(galleryField, new FormModeModelAdapter(galleryField), !galleryField.isImmutable());
	}

	private boolean hasIndividualSize() {
		return (width != null) || (height != null);
	}

	private DisplayDimension getSizeToUse(DisplayDimension configuredSize, DisplayDimension defaultSize) {
		if (configuredSize != null) {
			return configuredSize;
		} else {
			return defaultSize;
		}
	}

	@Override
	protected void teardown() {
		this.width = null;
		this.height = null;
		super.teardown();
	}

}

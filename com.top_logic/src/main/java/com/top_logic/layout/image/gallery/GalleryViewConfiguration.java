/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;

/**
 * Configuration of view aspects of an image gallery (e.g. width and height).
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryViewConfiguration {

	/**
	 * Configuration of {@link GalleryViewConfiguration}.
	 */
	public interface Config extends PolymorphicConfiguration<GalleryViewConfiguration> {

		/**
		 * Configuration name of property, that holds gallery width.
		 */
		public static final String GALLERY_WIDTH = "gallery-width";

		/**
		 * Configuration name of property, that holds gallery height.
		 */
		public static final String GALLERY_HEIGHT = "gallery-height";

		/**
		 * width of gallery.
		 */
		@Name(GALLERY_WIDTH)
		int getGalleryWidth();

		/**
		 * height of gallery.
		 */
		@Name(GALLERY_HEIGHT)
		int getGalleryHeight();

	}

	private DisplayDimension _width;

	private DisplayDimension _height;

	/**
	 * Create a new {@link GalleryViewConfiguration}.
	 */
	GalleryViewConfiguration() {
		_width = ThemeFactory.getTheme().getValue(Icons.GALLERY_WIDTH);
		_height = ThemeFactory.getTheme().getValue(Icons.GALLERY_HEIGHT);
	}

	/**
	 * Width of the image gallery as CSS value.
	 */
	public DisplayDimension getGalleryWidth() {
		return _width;
	}

	void setGalleryWidth(DisplayDimension value) {
		_width = value;
	}

	/**
	 * Height of the image gallery as CSS value.
	 */
	public DisplayDimension getGalleryHeight() {
		return _height;
	}

	void setGalleryHeight(DisplayDimension value) {
		_height = value;
	}

}

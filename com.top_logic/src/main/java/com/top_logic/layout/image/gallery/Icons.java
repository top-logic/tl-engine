/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/** Image to display when no image can be found. */
	@DefaultValue("/images/noPreviewImage.png")
	public static ThemeVar<Img> NO_PREVIEW_IMAGE;

	/**
	 * Height of an image shown in an image gallery attribute in pixels.
	 */
	@DefaultValue("200px")
	public static ThemeVar<DisplayDimension> GALLERY_HEIGHT;

	/**
	 * Width of an image shown in an image gallery attribute in pixels.
	 */
	@DefaultValue("240px")
	public static ThemeVar<DisplayDimension> GALLERY_WIDTH;

}

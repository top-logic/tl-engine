/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.util;

import javax.imageio.ImageIO;

import com.top_logic.base.chart.ImageComponent;

/**
 * Enumeration of image types / format named used by {@link ImageIO}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public enum ImageType {
	/** JPG image type */
	JPG("jpg", ImageComponent.MIME_JPEG),

	/** PNG image type */
	PNG("png", ImageComponent.MIME_PNG);

	private String _formatName;
	private String _mimeType;

	private ImageType(String formatName, String mimeType) {
		_formatName = formatName;
		_mimeType = mimeType;
	}

	/**
	 * format name of this {@link ImageType}, used by {@link ImageIO}.
	 */
	public String getFormatName() {
		return _formatName;
	}

	/**
	 * mime type of this {@link ImageType}.
	 */
	public String getMimeType() {
		return _mimeType;
	}
}
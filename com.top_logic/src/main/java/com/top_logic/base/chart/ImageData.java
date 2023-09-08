/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.awt.Dimension;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Description of an image to be rendered.
 * 
 * @see DefaultImageData
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImageData {

	/**
	 * The dimension in pixels of the {@link #getBytes() encoded image}.
	 */
	Dimension getDimension();

	/**
	 * The encoded form of the image.
	 * 
	 * <p>
	 * The format of the encoding is determined by {@link BinaryData#getContentType()} of the
	 * result.
	 * </p>
	 */
	BinaryData getBytes();

	/**
	 * The height in pixels of a header area of the image that should be prevented from being
	 * scrolled out of sight.
	 * 
	 * @return Header area height, <code>0</code> Means no header area.
	 */
	int getHeaderHeight();

	/**
	 * Width in pixels of a left border area of the image that should not be scrolled out of sight.
	 * 
	 * @return Description area width, <code>0</code> Means no description area.
	 */
	int getDescriptionWidth();

}

/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.top_logic.layout.DisplayContext;

/**
 * TODO BHU.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicImage {
	
	/**
	 * Prepares the image for being rendered in the given size.
	 */
	void prepare(DisplayContext context, int width, int height);
	
	/**
	 * Identifier of this image, used e.g. for its image map.
	 */
	String getID();

	/**
	 * Alternative text of this image.
	 */
	String getLabel();
	
	/**
	 * The width of the image in pixels.
	 */
	int getWidth();

	/**
	 * The height of the image in pixels.
	 */
	int getHeight();
	
	/**
	 * Writes the image data to the given stream.
	 * 
	 * @param format
	 *        The image format, e.g. {@code PNG}.
	 */
	void encode(DisplayContext context, OutputStream output, String format) throws IOException;

	/**
	 * Mapping of objects displayed in the image to the polygon coordinates of their areas, or
	 * <code>null</code> if this image has no clickable areas.
	 */
	Map getImageMap();
	
}

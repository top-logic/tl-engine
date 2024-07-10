/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.awt.Dimension;
import java.io.IOException;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;

/**
 * An interface for components that supports generation of image components.
 *
 * @see ImageControl
 *
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public interface ImageComponent {

	/** Path to the image that is used in case no image could be created. */
	public static final String DEFAULT_IMG_PATH = "/themes/core/misc/brokenImage.png";

	/**
	 * Type to request rendering of PNG images.
	 * 
	 * @see #createImage(DisplayContext, String, String, Dimension)
	 */
	public static final String IMAGE_TYPE_PNG = ".png";

	/**
	 * Mime type of GIF images.
	 * 
	 * @see BinaryData#getContentType()
	 */
	public static final String MIME_GIF = HTMLConstants.TYPE_GIF;

	/**
	 * Mime type of PNG images.
	 * 
	 * @see BinaryData#getContentType()
	 */
	public static final String MIME_PNG = HTMLConstants.TYPE_PNG;

	/**
	 * Mime type of JPEG images.
	 * 
	 * @see BinaryData#getContentType()
	 */
	public static final String MIME_JPEG = HTMLConstants.TYPE_JPEG;

	/**
	 * This method is called before the writeImage(...) method and returns the dimension of the
	 * image.
	 * 
	 * @param imageId
	 *        the unique ID of the image to create; may be <code>null</code> if only one image is
	 *        created within a component.
	 * @param dimension
	 *        the dimension as specified in some img-tag
	 */
	void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException;

	/**
	 * Actually renders the image.
	 * 
	 * @param imageId
	 *        the unique ID of the image to create; may be <code>null</code> if only one image is
	 *        created within a component.
	 * @param imageType
	 *        the type of image to create, e.g. {@link #IMAGE_TYPE_PNG}.
	 * @param dimension
	 *        the dimension the image should have.
	 * @return {@link ImageData} with the rendered image. See e.g.
	 *         {@link ChartUtil#encode(boolean, java.awt.image.BufferedImage)} or
	 *         {@link DefaultImageData}.
	 */
	ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException;

	/**
	 * This method returns the image map for the given image ID.
	 *
	 * @param imageId
	 *        the unique ID of the image to create; may be <code>null</code> if only one image is
	 *        created within a component.
	 * @param mapName
	 *        Unique identifier for the image map. The written map must have the given name as "id"
	 *        and as "name". Must not be <code>null</code>.
	 * @param dimension
	 *        the dimension the image should have.
	 */
	HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension) throws IOException;

}

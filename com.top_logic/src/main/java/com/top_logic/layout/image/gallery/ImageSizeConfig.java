/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of image size thresholds. (e.g. maximum size of uploaded images).
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ImageSizeConfig extends ConfigurationItem {

	/**
	 * Configuration name of property, that holds maximum size of uploaded images in megabytes
	 */
	public static final String MAX_UPLOAD_SIZE_MEGABYTE = "max-upload-size-megabyte";

	/**
	 * Configuration name of property, that holds maximum image size, that will be stored in
	 * database - in megabytes
	 */
	public static final String MAX_DATABASE_SIZE_MEGABYTE = "max-database-size-megabyte";

	/**
	 * Configuration name of property, that holds width, that shall be applied at forced image
	 * transformation.
	 */
	public static final String TRANSFORMED_IMAGE_WIDTH = "transformed-image-width";

	/**
	 * Configuration name of property, that holds height, that shall be applied at forced image
	 * transformation.
	 */
	public static final String TRANSFORMED_IMAGE_HEIGHT = "transformed-image-height";


	/**
	 * maximum size of uploaded files in megabyte.
	 */
	@Name(MAX_UPLOAD_SIZE_MEGABYTE)
	int getMaxUploadSizeMegabyte();

	/**
	 * maximum file size, that is allowed to be stored in database - in megabyte.
	 */
	@Name(MAX_DATABASE_SIZE_MEGABYTE)
	int getMaxDatabaseSizeMegabyte();

	/**
	 * width of image, that shall be applied at forced transformation
	 */
	@Name(TRANSFORMED_IMAGE_WIDTH)
	int getTransformedImageWidth();

	/**
	 * height of image, that shall be applied at forced transformation
	 */
	@Name(TRANSFORMED_IMAGE_HEIGHT)
	int getTransformedImageHeight();
}
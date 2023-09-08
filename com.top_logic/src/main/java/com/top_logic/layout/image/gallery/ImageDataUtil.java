/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.BinaryDataSourceProxy;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.image.util.ImageType;
import com.top_logic.util.Resources;

/**
 * Utility for {@link BinaryDataSource} handling, representing images.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ImageDataUtil {

	private static final Map<String, ImageType> IMAGE_TYPE_MAP;

	static {
		IMAGE_TYPE_MAP = new HashMap<>();
		IMAGE_TYPE_MAP.put("jpg", ImageType.JPG);
		IMAGE_TYPE_MAP.put("jpeg", ImageType.JPG);
		IMAGE_TYPE_MAP.put("png", ImageType.PNG);
	}

	/**
	 * Creates a {@link BinaryData} of requested mime type out of an image name and a corresponding
	 * {@link BinaryData}. The image name must contain the suffix of requested mime type (e.g.
	 * 'imageName.jpg'). The suffix must match the mime type encoding of the image data.
	 */
	public static BinaryData castMimeType(String imageName, BinaryData imageData) {
		return new DefaultDataItem(imageName, imageData, MimeTypes.getInstance().getMimeType(imageName));
	}

	/**
	 * Creates a {@link BinaryDataSource} of requested mime type out of an image name and a
	 * corresponding {@link BinaryDataSource}. The image name must contain the suffix of requested
	 * mime type (e.g. 'imageName.jpg'). The suffix must match the mime type encoding of the image
	 * data.
	 */
	public static BinaryDataSource castMimeType(String imageName, BinaryDataSource imageData) {
		return new BinaryDataSourceProxy(imageData) {
			@Override
			public String getName() {
				return imageName;
			}

			@Override
			public String getContentType() {
				return MimeTypes.getInstance().getMimeType(imageName);
			}
		};
	}

	/**
	 * {@link ImageType} registered to given image extension. Maybe <code>null</code>, when
	 *         image extension is unknown.
	 * 
	 * @see #isSupportedImageExtension(String)
	 */
	public static ImageType getImageType(String imageExtension) {
		return IMAGE_TYPE_MAP.get(imageExtension.toLowerCase());
	}

	/**
	 * true, if the given image file name has a supported extension.
	 * 
	 * @see #isSupportedImageExtension(String)
	 */
	public static boolean isSupportedImageFilename(String name) {
		return isSupportedImageExtension(FilenameUtils.getExtension(name));
	}

	/**
	 * true, if the given image extension is registered to an {@link ImageType}, false
	 *         otherwise.
	 * 
	 * @see #getImageType(String)
	 */
	public static boolean isSupportedImageExtension(String imageExtension) {
		return IMAGE_TYPE_MAP.containsKey(imageExtension.toLowerCase());
	}

	/**
	 * set of all image extensions, that are registered to an {@link ImageType}.
	 * 
	 * @see #getImageType(String)
	 * @see #isSupportedImageExtension(String)
	 */
	public static Set<String> getSupportedImageExtensions() {
		return new HashSet<>(IMAGE_TYPE_MAP.keySet());
	}

	/**
	 * Returns an internationalised message that the selected image file has unsupported image
	 * extension.
	 */
	public static String noValidImageExtension() {
		ResKey msg = I18NConstants.NO_VALID_IMAGE_ERROR;
		Set<String> extensions = getSupportedImageExtensions();
		return Resources.getInstance().getMessage(msg, extensions);
	}

}

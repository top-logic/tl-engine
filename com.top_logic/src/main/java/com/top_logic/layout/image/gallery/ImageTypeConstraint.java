/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import org.apache.commons.io.FilenameUtils;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;

/**
 * {@link AbstractConstraint}, that checks, if a given file name has the appropriate image file
 * suffix.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class ImageTypeConstraint extends AbstractConstraint {

	/** Singleton instance of {@link ImageTypeConstraint} */
	public static final ImageTypeConstraint INSTANCE = new ImageTypeConstraint();

	private ImageTypeConstraint() {
		// Singleton
	}

	@Override
	public boolean check(Object value) throws CheckException {
		String imageName = (String) value;
		if (isNotValidImageFile(imageName)) {
			throw new CheckException(ImageDataUtil.noValidImageExtension());
		}
		return true;
	}

	private boolean isNotValidImageFile(String imageName) {
		String imageExtension = FilenameUtils.getExtension(imageName);
		return !ImageDataUtil.isSupportedImageExtension(imageExtension);
	}
}

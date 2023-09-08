/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.util.Resources;

/**
 * {@link AbstractConstraint}, that checks, that a given image size has not been exceeded.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ImageSizeConstraint extends AbstractConstraint {

	/** Static instance of {@link ImageSizeConstraint} */
	public static final ImageSizeConstraint INSTANCE = new ImageSizeConstraint();

	private ImageSizeConstraint() {
		// Singleton
	}

	@Override
	public boolean check(Object value) throws CheckException {
		for (BinaryDataSource file : DataField.toItems(value)) {
			checkMaximumSize(file);
		}
		return true;
	}

	private void checkMaximumSize(BinaryDataSource file) throws CheckException {
		if (file.getSize() > getMaxFileSize()) {
			throw new CheckException(Resources.getInstance().getString(
				I18NConstants.ERROR_MAX_IMAGE_SIZE_EXCEEDED__NAME_MAX_UNIT.fill(file.getName(), getMaxSizeMegabyte(),
					"MB")));
		}
	}

	private long getMaxFileSize() {
		return getMaxSizeMegabyte() * 1024 * 1024;
	}

	private int getMaxSizeMegabyte() {
		return ApplicationConfig.getInstance().getConfig(ImageSizeConfig.class).getMaxUploadSizeMegabyte();
	}
}

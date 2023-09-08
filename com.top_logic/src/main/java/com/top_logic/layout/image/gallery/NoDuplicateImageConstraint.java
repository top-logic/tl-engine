/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.table.TableModel;
import com.top_logic.util.Resources;

/**
 * {@link AbstractConstraint}, that ensures, a uploaded image is not already part of the gallery.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class NoDuplicateImageConstraint extends AbstractConstraint {

	private TableModel _tableModel;

	/**
	 * Create a new {@link NoDuplicateImageConstraint}.
	 */
	public NoDuplicateImageConstraint(TableModel tableModel) {
		_tableModel = tableModel;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		if (value != null) {
			String fileName = (String) value;
			if (isDuplicate(fileName)) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.DUPLICATE_IMAGE_ERROR));
			}
		}
		return true;
	}

	private boolean isDuplicate(String fileName) {
		for (Object image : _tableModel.getAllRows()) {
			if (isSame(image, fileName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSame(Object image, String fileName) {
		return fileName.equals(((GalleryImage) image).getName());
	}

}

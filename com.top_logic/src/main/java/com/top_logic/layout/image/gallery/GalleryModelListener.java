/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.List;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link GalleryModelListener} are listener to inform about changes of {@link GalleryModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface GalleryModelListener extends PropertyListener {

	/**
	 * Informs about a change of the {@link List} of the {@link GalleryModel}.
	 * 
	 * @param source
	 *        the {@link GalleryModel} whose {@link List} has changed.
	 * @param oldValue
	 *        the old value before the change
	 * @param newValue
	 *        the value after the change.
	 */
	public void notifyImagesChanged(GalleryModel source, List<GalleryImage> oldValue, List<GalleryImage> newValue);

}

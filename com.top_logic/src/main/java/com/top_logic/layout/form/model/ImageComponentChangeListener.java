/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the {@link ImageModel#getImageComponent()} of an {@link ImageModel}.
 * 
 * @see ImageModel#IMAGE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ImageComponentChangeListener extends PropertyListener {

	/**
	 * Handles change of the {@link ImageModel#getImageComponent() image} of the {@link ImageModel}.
	 * 
	 * @param sender
	 *        The model whose image changed.
	 * @param oldValue
	 *        The old image.
	 * @param newValue
	 *        The new image.
	 * 
	 * @see ImageModel#getImageComponent()
	 */
	void handleImageChanged(ImageModel sender, ImageComponent oldValue, ImageComponent newValue);

}


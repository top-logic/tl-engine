/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.awt.Dimension;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Observable {@link ImageModel}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class DefaultImageModel extends PropertyObservableBase implements ImageModel {

	private ImageComponent imageComponent;
	private Dimension dimension;

	public DefaultImageModel(ImageComponent aComp, Dimension aDimension) {
		this.imageComponent = aComp;
		this.dimension = aDimension;
    }

	@Override
	public void setDimension(Dimension dim) {
		Dimension oldDimension = getDimension();
		Dimension newDimension = dim;
		this.dimension = newDimension;

		notifyListeners(DIMENSION_PROPERTY, this, oldDimension, newDimension);
	}

	@Override
	public void setImageComponent(ImageComponent comp) {
		ImageComponent oldComponent = getImageComponent();
		ImageComponent newComponent = comp;
		this.imageComponent = newComponent;

		notifyListeners(IMAGE_PROPERTY, this, oldComponent, newComponent);
	}

	@Override
	public Dimension getDimension() {
		return this.dimension;
	}

	@Override
	public ImageComponent getImageComponent() {
		return this.imageComponent;
	}

}

/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.awt.Dimension;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public interface ImageModel extends PropertyObservable {

	/**
	 * Event type of {@link #getImageComponent()}.
	 * 
	 * @see ImageComponentChangeListener
	 */
	EventType<ImageComponentChangeListener, ImageModel, ImageComponent> IMAGE_PROPERTY =
		new NoBubblingEventType<>("image") {

			@Override
			protected void internalDispatch(ImageComponentChangeListener listener, ImageModel sender,
					ImageComponent oldValue, ImageComponent newValue) {
				listener.handleImageChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Event type of {@link #getDimension()}.
	 * 
	 * @see DimensionListener
	 */
	EventType<DimensionListener, Object, Dimension> DIMENSION_PROPERTY =
		new NoBubblingEventType<>("dimension") {

			@Override
			protected void internalDispatch(DimensionListener listener, Object sender, Dimension oldValue,
					Dimension newValue) {
				listener.handleDimensionChanged(sender, oldValue, newValue);
			}

		};
	

	/** 
	 * Returns the underlying {@link ImageComponent}
	 */
	public ImageComponent getImageComponent();
	
	/** 
	 * Sets the underlying {@link ImageComponent}.
	 */
	public void setImageComponent(ImageComponent aComp);
	
	/** 
	 * Returns the {@link Dimension} of the wrapped image
	 */
	public Dimension getDimension();
	
	/** 
	 * Set the {@link Dimension} of the wrapped image
	 */
	public void setDimension(Dimension aDim);
	
}

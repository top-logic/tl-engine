/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} to specify the dimensions of the input field for all input elements
 * generated for the annotated attribute.
 * 
 * @implNote In contrast to {@link InputSize#getValue()} and {@link MultiLine#getRows()}, the
 *           dimensions here are given in CSS size values.
 */
@TagName("dimensions")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface TLDimensions extends TLAttributeAnnotation {

	/** @see #getWidth() */
	String WIDTH = "width";

	/** @see #getHeight() */
	String HEIGHT = "height";

	/**
	 * The CSS width value to set on the input input element of the annotated attribute.
	 */
	@Name(WIDTH)
	@Nullable
	DisplayDimension getWidth();

	/**
	 * @see #getWidth()
	 */
	void setWidth(DisplayDimension value);

	/**
	 * The CSS height value to set on the input input element of the annotated attribute.
	 * 
	 * <p>
	 * Note: the value may not be relevant for all types of potential input fields.
	 * </p>
	 */
	@Name(HEIGHT)
	@Nullable
	DisplayDimension getHeight();

	/**
	 * @see #getHeight()
	 */
	void setHeight(DisplayDimension value);

	/**
	 * Creates a {@link TLDimensions} annotation with only the {@link #getWidth()} set to the given
	 * value.
	 */
	static TLDimensions width(DisplayDimension value) {
		TLDimensions result = TypedConfiguration.newConfigItem(TLDimensions.class);
		result.setWidth(value);
		return result;
	}

	/**
	 * Creates a {@link TLDimensions} annotation with only the {@link #getWidth()} set to the given
	 * value.
	 */
	static TLDimensions height(DisplayDimension value) {
		TLDimensions result = TypedConfiguration.newConfigItem(TLDimensions.class);
		result.setHeight(value);
		return result;
	}

}

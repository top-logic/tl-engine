/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;

/**
 * Annotation that defines the default {@link Visibility} of a {@link TLModelPart} durcing creation.
 * 
 * <p>
 * When the annotation is absent, the value of {@link TLVisibility} is also used during creation
 * </p>
 * 
 * @see TLVisibility
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("create-visibility")
@InApp
public interface TLCreateVisibility extends TLAttributeAnnotation {

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * The specification of the visibility during creation.
	 */
	@Name(VALUE)
	Visibility getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(Visibility value);

}


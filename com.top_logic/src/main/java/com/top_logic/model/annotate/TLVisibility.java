/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;

/**
 * Annotation that defines the default {@link Visibility} of a {@link TLModelPart}.
 * 
 * @see TLCreateVisibility Specifying the visibility in object creation forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("visibility")
@InApp
public interface TLVisibility extends TLAttributeAnnotation {

	/** Default {@link Visibility} when no {@link TLVisibility} annotation is set. */
	Visibility DEFAULT_VISIBILITY = Visibility.EDITABLE;

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * The specification of the visibility.
	 */
	@Name(VALUE)
	Visibility getValue();

}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} to specify a CSS class to be consistently set for all input
 * elements generated for the annotated attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("css-class")
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface TLCssClass extends TLAttributeAnnotation, TLTypeAnnotation {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The CSS class to set on input elements for the annotated attribute.
	 */
	@Name(VALUE)
	@Nullable
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

}

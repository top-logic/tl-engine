/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
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
	 * 
	 * <p>
	 * There are a number of pre-defined CSS classes for highlighting:
	 * </p>
	 * 
	 * <ul>
	 * <li><code>tl-info</code></li>
	 * <li><code>tl-success</code></li>
	 * <li><code>tl-warning</code></li>
	 * <li><code>tl-danger</code></li>
	 * <li><code>tl-debug</code></li>
	 * <li><code>tl-accent</code></li>
	 * </ul>
	 * 
	 * <p>
	 * All these classes can be combined with the class <code>tl-lighter</code> to make the
	 * highlighting less prominent.
	 * </p>
	 */
	@Name(VALUE)
	@Nullable
	@Label("Static CSS class")
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

	/**
	 * Algorithm to compute a CSS class for some value of the annotated attribute.
	 * 
	 * <p>
	 * If not given or if the algorithm return <code>null</code>, the static CSS class is used.
	 * </p>
	 */
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends CssClassProvider> getDynamicCssClass();

}

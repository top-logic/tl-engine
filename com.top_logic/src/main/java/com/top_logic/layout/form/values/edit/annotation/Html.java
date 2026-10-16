/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;

/**
 * States that the text of a configuration property is HTML.
 *
 * <p>
 * A form edits such a property with a WYSIWYG editor for formatted text, if the application
 * contains one. Otherwise, the HTML source is edited with an input for multi-line text, whose
 * number of rows is taken from a {@link MultiLine} annotation of the same property if there is
 * one. A form that displays the property without allowing to edit it shows the formatted text.
 * </p>
 *
 * <p>
 * The annotation applies to the text the property is edited as: a plain text value, the text of a
 * value with a format, or each translation of an internationalized text.
 * </p>
 *
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("html")
public @interface Html {

	/** Name of {@link #features()}. */
	String FEATURES = "features";

	/**
	 * Name of the set of editing features the editor offers.
	 *
	 * <p>
	 * A feature set is defined by the configuration of the editor, e.g. the feature set
	 * <code>macro</code>, which additionally offers editing the HTML source, for a text with
	 * embedded TL-Script expressions. The empty name stands for the standard feature set of the
	 * editor.
	 * </p>
	 */
	String features() default "";

}

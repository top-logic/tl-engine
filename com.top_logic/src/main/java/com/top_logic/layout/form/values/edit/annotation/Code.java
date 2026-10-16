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
 * States that the text of a configuration property is source code in a certain
 * {@link #language() language}.
 *
 * <p>
 * A form edits such a property with a code editor for the language, if the application contains
 * one. Otherwise, the text is edited with an input for multi-line text, whose number of rows is
 * taken from a {@link MultiLine} annotation of the same property if there is one.
 * </p>
 *
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("code")
public @interface Code {

	/** Name of {@link #language()}. */
	String LANGUAGE = "language";

	/**
	 * The language the text is written in.
	 */
	CodeLanguage language() default CodeLanguage.PLAIN;

}

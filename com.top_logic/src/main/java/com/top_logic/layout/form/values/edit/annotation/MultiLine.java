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
import com.top_logic.layout.form.values.MultiLineText;

/**
 * States that the text of a configuration property spans several lines.
 *
 * <p>
 * A form displays such a property with an input for multi-line text. The annotation applies to the
 * text the property is edited as: a plain text value, the text of a value with a format, or each
 * translation of an internationalized text.
 * </p>
 *
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 *
 * @see CommonMark
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("multi-line")
@ControlAnnotation
public @interface MultiLine {

	/** Name of {@link #rows()}. */
	String ROWS = "rows";

	/**
	 * The number of visible rows of the input.
	 */
	int rows() default MultiLineText.DEFAULT_ROWS;

}

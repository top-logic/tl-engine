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
 * States that the text of a configuration property is written in <i>CommonMark</i> syntax.
 *
 * <p>
 * Such a text spans several lines, so the annotation implies {@link MultiLine}: a form edits it with
 * an input for multi-line text, whose number of rows is taken from a {@link MultiLine} annotation of
 * the same property if there is one. A form that displays the property without allowing to edit it
 * shows the formatted text.
 * </p>
 *
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 *
 * @see "https://commonmark.org/"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("common-mark")
public @interface CommonMark {

	// Pure marker annotation.

}

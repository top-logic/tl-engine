/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.editor.Editor;

/**
 * Annotation to a configuration property getter selecting an {@link Editor} strategy for building
 * its form representation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("property-editor")
public @interface PropertyEditor {

	/**
	 * The strategy class creating the form representation for the annotated property.
	 */
	Class<? extends Editor> value();

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.FormBuilder;

/**
 * Annotation to {@link ConfigurationItem}s providing {@link FormBuilder}s for custom display parts.
 * 
 * @see UseTemplate
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TagName("form-builders")
public @interface UseBuilder {

	/**
	 * Builder class the form.
	 */
	Class<? extends FormBuilder<?>>[] value();

}

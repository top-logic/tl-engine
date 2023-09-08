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
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Annotation to a {@link ConfigurationItem} property that controls the visibility of its input
 * element in forms.
 * 
 * @see DynamicMode Updating the display mode dynamically.
 * @see ReadOnly Unconditionally making a property non-editable.
 * @see Hidden Unconditionally hiding a property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("dynamic-mode")
public @interface DynamicMode {

	/**
	 * The {@link GenericFunction} that computes the {@link FieldMode} for the input field.
	 */
	Class<? extends GenericFunction<FieldMode>> fun();

	/**
	 * Specification of argument references for {@link #fun() function}.
	 */
	Ref[] args() default {};

}

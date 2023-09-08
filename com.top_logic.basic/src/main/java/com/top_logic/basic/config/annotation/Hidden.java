/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating that hides a configuration property from the user interface by default.
 * 
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 * 
 * @see "com.top_logic.layout.form.values.edit.annotation.DynamicMode"
 * @see ReadOnly Unconditionally making a property non-editable.
 * @see Hidden Unconditionally hiding a property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("hidden")
public @interface Hidden {

	/**
	 * Whether the property should be hidden.
	 */
	boolean value() default true;

}

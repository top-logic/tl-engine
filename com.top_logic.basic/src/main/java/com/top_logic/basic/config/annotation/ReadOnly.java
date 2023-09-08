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
 * Annotating that prevents editing a configuration property in a user interface by default.
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
@TagName("read-only")
public @interface ReadOnly {

	/**
	 * The scope of the {@link ReadOnly} annotation.
	 * 
	 * <p>
	 * The default value is {@link ReadOnlyMode#ON}
	 * </p>
	 */
	ReadOnlyMode value() default ReadOnlyMode.ON;

	/**
	 * Mode for fine-grained control of the scope of a {@link ReadOnly} annotation.
	 */
	public enum ReadOnlyMode {
		/**
		 * The annotated property can be edited.
		 */
		OFF,

		/**
		 * The property itself cannot be edited, but if it has inner structure, those values can be
		 * edited if they are not annotated {@link ReadOnly} themselves.
		 */
		LOCAL,

		/**
		 * The property and its potential contents are completely {@link ReadOnly}.
		 */
		ON;
	}

}

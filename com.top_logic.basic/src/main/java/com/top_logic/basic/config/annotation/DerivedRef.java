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

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.func.Identity;

/**
 * Marks a {@link ConfigurationItem} property as derived by a simple path expression.
 * 
 * <p>
 * Annotating a property {@link DerivedRef} is a shortcut for using the {@link Identity} function
 * with a single path argument to another property.
 * </p>
 * 
 * @see Derived Complex custom algorithms.
 * 
 * @see PropertyKind#DERIVED
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("derived-ref")
public @interface DerivedRef {

	/**
	 * List of property names to navigate from the base model the annotated property is defined on.
	 */
	String[] value() default {};

	/**
	 * General path to the referenced property.
	 * 
	 * <p>
	 * Properties of related models can be specified using a reference path to that property.
	 * </p>
	 * 
	 * @see #value() A simplified form of a path.
	 */
	Step[] steps() default {};

}

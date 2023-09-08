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
import com.top_logic.basic.func.GenericFunction;

/**
 * Marks a {@link ConfigurationItem} property as derived by the given {@link GenericFunction}
 * algorithm.
 * 
 * @see DerivedRef Simple algorithms performing navigations.
 * 
 * @see PropertyKind#DERIVED
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("derived")
public @interface Derived {

	/**
	 * The function class that computes the value of the annotated property.
	 */
	Class<? extends GenericFunction<?>> fun();
	
	/**
	 * Specification of {@link #fun()} arguments.
	 */
	Ref[] args();

}

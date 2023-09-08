/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.format.DotSeparatedIdentifierArray;

/**
 * Reference to another property of a {@link ConfigurationItem}.
 * 
 * <p>
 * Describes an access path (consecutive dereferencing of configuration properties) that yields a
 * certain value. <br/>
 * Example: <code>@Ref({ "foo", "bar", "fubar"})</code><br/>
 * Means: <code>this.getFoo().getBar().getFubar()</code><br/>
 * (Under the assumption that the properties have no other names annotated to them.)
 * </p>
 */
@TagName("ref")
public @interface Ref {

	/**
	 * Simple path to the referenced property.
	 * 
	 * <p>
	 * Properties of related models can be specified using a reference path to that property.
	 * </p>
	 * 
	 * <p>
	 * This is a short-cut notation for {@link #steps()}.
	 * </p>
	 */
	@Format(DotSeparatedIdentifierArray.class)
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
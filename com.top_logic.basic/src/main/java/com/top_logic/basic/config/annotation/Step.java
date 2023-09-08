/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

/**
 * Single step of a {@link Ref} or {@link DerivedRef} path.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("step")
public @interface Step {

	/**
	 * Name of the property to access in this step.
	 */
	String value();

	/**
	 * Expected base configuration type on which the {@link #value() property} is declared.
	 * 
	 * <p>
	 * If the actual base item, on which this step is evaluated is not of the specified type,
	 * this step evaluates to <code>null</code>.
	 * </p>
	 */
	Class<?> type() default Void.class;
}
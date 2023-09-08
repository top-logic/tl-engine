/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for an implementation class that can be selected for in-app development.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface InApp {

	/**
	 * Whether the annotated implementation is usable for in-app development.
	 */
	boolean value() default true;

	/**
	 * List of classifiers selecting valid usage scenarios of the annotated implementation.
	 * 
	 * <p>
	 * The empty list of classifiers means either "valid in all situations" regardless of the
	 * classifiers required or "not acceptable at all", depending on the acceptable classifiers
	 * annotation.
	 * </p>
	 */
	String[] classifiers() default {};

	/**
	 * The priority of the annotated implementation.
	 * 
	 * <p>
	 * The UI selects the implementation with the highest priority as default value for a mandatory
	 * property.
	 * </p>
	 */
	int priority() default 0;

}

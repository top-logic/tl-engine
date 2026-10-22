/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Declares a value an element supplies to the script of the annotated property on each
 * evaluation, such as the row a row-class script is evaluated for.
 *
 * <p>
 * The script sees the value as a variable behind the {@link Inputs inputs} of its configuration,
 * named {@link #value()}, or by the name configured in the property {@link #renamedBy()} of the
 * supplying element. The annotations of a property declare its supplied values in the order of
 * their values on evaluation.
 * </p>
 *
 * <p>
 * The declaration is read by both the runtime and the script editor, so that both offer the same
 * variables, see {@link InputScript}.
 * </p>
 *
 * @implNote Read by {@link InputScript#compile(ConfigurationItem, String)} for the runtime and by
 *           {@link InputScript.Variables} for the script editor.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Supplies.List.class)
public @interface Supplies {

	/**
	 * The default name of the supplied variable.
	 */
	String value();

	/**
	 * The name of a configuration property that renames the supplied variable, empty for a variable
	 * that cannot be renamed.
	 *
	 * <p>
	 * The property is looked up at the configuration holding the script, then at the
	 * configurations containing it, so that an element can rename the value for the scripts of its
	 * parts (e.g. a table for the scripts of its columns). A non-empty value of the first
	 * configuration declaring the property is the name of the variable.
	 * </p>
	 */
	String renamedBy() default "";

	/**
	 * Container of repeated {@link Supplies} annotations.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface List {

		/**
		 * The supplied values, in the order of their values on evaluation.
		 */
		Supplies[] value();
	}
}

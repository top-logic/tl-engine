/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a parameter of a {@link ToolMethod}-annotated method.
 *
 * @see ToolMethod
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface P {

	/**
	 * Description of the parameter. Sent to the LLM.
	 */
	String value();

	/**
	 * Whether this parameter is required. Defaults to {@code true}.
	 */
	boolean required() default true;

}

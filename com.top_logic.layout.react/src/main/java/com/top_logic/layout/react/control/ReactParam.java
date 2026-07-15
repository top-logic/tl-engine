/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes one argument of a {@link ReactCommandHandler}, so the headless agent interface can advertise
 * the command's argument schema instead of leaving a consumer to guess it.
 *
 * <p>
 * Declared on the command method via {@link ReactCommandHandler#params()}. It documents the {@code key}
 * expected in the command's {@code arguments} map, its informal {@link #type()} and whether it is
 * {@link #required()}. This lives next to the command (not on a headless interface) so the schema
 * cannot drift from the handler.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReactParam {

	/**
	 * The argument key, as expected in the command's {@code arguments} map.
	 */
	String name();

	/**
	 * Informal type hint for the value (e.g. {@code "string"}, {@code "int"}, {@code "boolean"},
	 * {@code "string[]"}).
	 */
	String type() default "string";

	/**
	 * Whether the argument must be supplied.
	 */
	boolean required() default false;

	/**
	 * Human/agent readable description of the argument's meaning.
	 */
	String description() default "";
}

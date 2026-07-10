/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to a configuration property or type specifying an alternative label for display in the
 * UI.
 * 
 * @see #value()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD })
public @interface Label {

	/**
	 * Resource key suffix under which the {@link #option()} label of a type is stored.
	 */
	String OPTION_SUFFIX = "@option";

	/**
	 * The (English) label of the annotated element when displayed in the UI.
	 */
	String value();

	/**
	 * Optional label used where the annotated <em>type itself</em> is displayed — as an option in
	 * a type selection, or as the text of a documentation reference.
	 *
	 * <p>
	 * Relevant when {@link #value()} is a rendering template with embedded property references
	 * (e.g. {@code Delete file '{file-name}'.}): such a label only makes sense expanded against an
	 * instance. The option label is stored under the {@link #OPTION_SUFFIX}-suffixed resource key
	 * of the annotated type, where displays of the type look it up (falling back to
	 * {@link #value()} when empty).
	 * </p>
	 */
	String option() default "";

}

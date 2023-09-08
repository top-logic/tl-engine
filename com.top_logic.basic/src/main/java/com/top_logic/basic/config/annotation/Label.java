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
@Retention(SOURCE)
@Target({ TYPE, METHOD, FIELD })
public @interface Label {

	/**
	 * The (English) label of the annotated element when displayed in the UI.
	 */
	String value();

}

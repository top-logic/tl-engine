/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation.defaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation class for annotating a default value of type {@link Class}.
 * 
 * <p>
 * This annotation can be annotated to functions (e. g. a property of the configuration) or to a
 * field (e. g. when defining a theme variable in Icons class) to set its default value.
 * </p>
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@TagName("class-default")
public @interface ClassDefault {

	Class<?> value();

}

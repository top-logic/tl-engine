/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationValueBinding;

/**
 * Annotation class for annotating a custom {@link ConfigurationValueBinding} to a property.
 * 
 * <p>
 * This annotation can also be used to define a {@link ConfigurationValueBinding} for all objects of
 * an annotated class.
 * </p>
 * 
 * @see Format
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("value-binding")
public @interface Binding {

	Class<? extends ConfigurationValueBinding<?>> value();
	
}

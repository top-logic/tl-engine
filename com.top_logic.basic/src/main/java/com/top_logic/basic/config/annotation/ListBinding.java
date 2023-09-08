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

import com.top_logic.basic.config.AbstractListBindingFuture;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.SimpleListValueBindingFuture;
import com.top_logic.basic.config.VoidValueProvider;

/**
 * Annotation class for annotating a {@link ConfigurationValueBinding} which
 * reads a list of tags and uses a {@link ConfigurationValueProvider} for the
 * single entries.
 * 
 * If no {@link ConfigurationValueBinding} is given, for simple types the
 * default {@link ConfigurationValueProvider} is used. If no tag is given a
 * default tag name is expected. If no attribute is given
 * {@link AbstractListBindingFuture#DEFAULT_VAUE_ATTRIBUTE_NAME value} is
 * expected.
 * 
 * @see MapBinding
 * @see SimpleListValueBindingFuture
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("list-binding")
public @interface ListBinding {

	String tag() default AbstractListBindingFuture.STRING_DEFAULT;

	String attribute() default AbstractListBindingFuture.STRING_DEFAULT;

	Class<? extends ConfigurationValueProvider<?>> format() default VoidValueProvider.class;

}

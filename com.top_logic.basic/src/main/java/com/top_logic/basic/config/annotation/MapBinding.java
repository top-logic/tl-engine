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
import com.top_logic.basic.config.SimpleMapValueBindingFuture;
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
 * expected. If no key attribute is given
 * {@link SimpleMapValueBindingFuture#DEFAULT_KEY_ATTRIBUTE_NAME value} is
 * expected.
 * 
 * @see ListBinding
 * @see SimpleMapValueBindingFuture
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("map-binding")
public @interface MapBinding {

	/**
	 * Name of the key attribute.
	 */
	String key() default AbstractListBindingFuture.STRING_DEFAULT;

	/**
	 * Format of the key of the entry.
	 */
	Class<? extends ConfigurationValueProvider<?>> keyFormat() default VoidValueProvider.class;

	/**
	 * Name of the tag holding the key and the value.
	 */
	String tag() default AbstractListBindingFuture.STRING_DEFAULT;

	/**
	 * Name of the value attribute.
	 */
	String attribute() default AbstractListBindingFuture.STRING_DEFAULT;

	/**
	 * Format of the value of the entry.
	 */
	Class<? extends ConfigurationValueProvider<?>> valueFormat() default VoidValueProvider.class;

}

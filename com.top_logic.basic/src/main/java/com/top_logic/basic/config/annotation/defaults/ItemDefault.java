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

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation class for marking a property which is of type
 * {@link PropertyKind#ITEM} to have a default instance of that type as default
 * value. The default value is unmodifiable.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("item-default")
public @interface ItemDefault {

	/**
	 * The configuration interface to instantiate by default.
	 * 
	 * <p>
	 * If none is given, the property type is used. If the given type is an implementation class, a
	 * {@link PolymorphicConfiguration} filled with that implementation class is used.
	 * </p>
	 */
	Class<?> value() default Void.class;

}

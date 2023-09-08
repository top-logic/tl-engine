/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Annotation for a {@link ConfigurationItem} property getter method that associates a
 * {@link PropertyInitializer} with that property.
 * 
 * <p>
 * Note: The annotated {@link PropertyInitializer} is only considered, if the configuration item
 * with the annotated property is constructed in a declarative form. A {@link PropertyInitializer}
 * is not used when constructing a plain configuration item
 * {@link TypedConfiguration#newConfigItem(Class) by code}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("value-initializer")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValueInitializer {

	/**
	 * The {@link PropertyInitializer} to use for the annotated property.
	 */
	Class<? extends PropertyInitializer> value();

}

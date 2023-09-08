/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation.defaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation selecting a class to be used by default for a {@link PropertyDescriptor} that holds
 * polymorphic configurations.
 * 
 * <p>
 * The annotated property must be declared of configuration type (or list, map, array of such type).
 * </p>
 * 
 * <p>
 * If the declared type of the property is a subtype of {@link PolymorphicConfiguration}, then the
 * {@link #value()} selects the {@link PolymorphicConfiguration#getImplementationClass()} to be used
 * if none is explicitly specified in the configuration.
 * </p>
 * 
 * <p>
 * If the declared type of the annotated property is of plain {@link ConfigurationItem} type, the
 * {@link #value()} specifies the {@link ConfigurationItem#getConfigurationInterface() configuration
 * interface} that is used for the property, if no explicit configuration type is specified in the
 * configuration.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("implementation-class-default")
public @interface ImplementationClassDefault {

	/**
	 * The implementation class for a {@link PolymorphicConfiguration} property, or the concrete
	 * configuration type for a general {@link ConfigurationItem} property if none is explicitly
	 * specified in the configuration.
	 * 
	 * <p>
	 * If the configuration is edited in a UI, this value is used to select a default option for an
	 * otherwise mandatory property.
	 * </p>
	 */
	Class<?> value();

}


/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Default annotation for {@link PropertyDescriptor} of type {@link PropertyKind#LIST}.
 * 
 * <p>
 * The value is a sequence of {@link ConfigurationItem}s which are created and taken as default. If
 * a class is not an extension of {@link ConfigurationItem} it is treated as shortcut for a
 * {@link PolymorphicConfiguration} with the given class as
 * {@link PolymorphicConfiguration#getImplementationClass() implementation class}.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("list-default")
public @interface ListDefault {

	/**
	 * Classes of the {@link ConfigurationItem} in the default value.
	 */
	Class<?>[] value();

}

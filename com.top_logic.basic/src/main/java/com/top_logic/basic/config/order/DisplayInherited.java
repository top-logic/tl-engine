/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Specifies, how inherited properties are handled, when a {@link ConfigurationItem} is used for
 * describing a UI.
 * 
 * <p>
 * Note: This annotation is "inherited" to derived interfaces. This allows to define a consistent
 * display policy ({@link DisplayInherited.DisplayStrategy#APPEND}, or
 * {@link DisplayInherited.DisplayStrategy#PREPEND}) among a hierarchy of configuration just at the
 * root of the hierarchy. A value of {@link DisplayInherited.DisplayStrategy#IGNORE} is only locally
 * used for the annotated interface but not considered during determining the inherited strategy.
 * </p>
 * 
 * <p>
 * If no annotation is given throughout the hierarchy, the default strategy is
 * {@link DisplayInherited.DisplayStrategy#PREPEND}.
 * </p>
 * 
 * @see DisplayOrder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TagName("display-inherited")
public @interface DisplayInherited {

	/**
	 * Strategy how to handle inherited properties.
	 */
	DisplayStrategy value();

	/**
	 * Option how to handle inherited properties of a class annotated with {@link DisplayInherited}.
	 */
	public enum DisplayStrategy {
		/**
		 * Prepend the properties of the inherited class to the properties of the class annotated
		 * with {@link DisplayInherited}.
		 */
		PREPEND,

		/**
		 * Append the properties of the inherited class to the properties of the class annotated
		 * with {@link DisplayInherited}.
		 */
		APPEND,

		/**
		 * Ignore properties of inherited classes.
		 * 
		 * <p>
		 * If a property of an inherited class should be displayed, it must be included in the list
		 * of property names given in the classes {@link DisplayOrder} annotation.
		 * </p>
		 */
		IGNORE,
	}

}

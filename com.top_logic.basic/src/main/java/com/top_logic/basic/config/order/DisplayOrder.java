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
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.StringConstantReference;

/**
 * Specifies the display order of properties for a {@link ConfigurationItem}.
 * 
 * @see DisplayInherited
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TagName("display-order")
public @interface DisplayOrder {

	/**
	 * The order of property names for display.
	 * 
	 * <p>
	 * Properties that are not listed are not displayed.
	 * </p>
	 */
	@ListBinding(tag = "property", attribute = "name", format = StringConstantReference.class)
	String[] value();
	
}

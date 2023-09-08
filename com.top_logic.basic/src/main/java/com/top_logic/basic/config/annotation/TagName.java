/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Annotation of {@link ConfigurationItem} interfaces specifying a custom tag name.
 * 
 * <p>
 * The annotated custom tag name can be used in the context of other configurations instead of the
 * <code>config:interface="..."</code> attribute for selecting the concrete configuration type.
 * </p>
 * 
 * <p>
 * Note: The tag names must be unique in all contexts the annotated configuration interface may be
 * used.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TagName("tag-name")
public @interface TagName {

	/**
	 * @see #value()
	 */
	String VALUE = "value";

	/**
	 * The custom tag name to use as short-cut for the annotated {@link ConfigurationItem}
	 * interface.
	 */
	@Name(VALUE)
	String value();

}

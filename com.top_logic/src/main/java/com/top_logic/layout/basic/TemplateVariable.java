/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * Annotation of a {@link Control}'s getter method or render method that marks the method as
 * accessible through the {@link WithProperties} interface.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TemplateVariable {

	/**
	 * The name of the property through which the annotated getter or render function can be
	 * accessed.
	 * 
	 * @see WithProperties#getPropertyValue(String)
	 * @see WithProperties#renderProperty(DisplayContext, TagWriter, String)
	 */
	String value();

}

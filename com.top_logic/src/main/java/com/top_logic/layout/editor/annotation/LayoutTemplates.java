/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.CommaSeparatedStringArray;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;

/**
 * Annotation providing the arguments for the {@link LayoutTemplatesDefault} and
 * {@link LayoutTemplateDefault} provider.
 * 
 * <p>
 * The annotated {@link PropertyDescriptor} must have {@link LayoutTemplatesDefault} or
 * {@link LayoutTemplateDefault} as its {@link ComplexDefault}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("layout-templates")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LayoutTemplates {

	/**
	 * List of layout template names to use as default values.
	 * 
	 * <p>
	 * To be effective, the annotated property must use the {@link LayoutTemplatesDefault} or
	 * {@link LayoutTemplateDefault} provider as {@link ComplexDefault}.
	 * </p>
	 */
	@Format(CommaSeparatedStringArray.class)
	String[] value();

}

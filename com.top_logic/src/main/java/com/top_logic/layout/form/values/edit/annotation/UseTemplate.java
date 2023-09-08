/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.TemplateProvider;

/**
 * Annotation to {@link ConfigurationItem} types for {@link TemplateProvider providing templates}
 * for display.
 * 
 * @see DisplayOrder Simplified form of specifying the visual presentation.
 * @see UseBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TagName("template-provider")
public @interface UseTemplate {

	/**
	 * Factory class to create a HTML template for rendering a {@link ConfigurationItem} model.
	 */
	Class<? extends TemplateProvider> value();

}

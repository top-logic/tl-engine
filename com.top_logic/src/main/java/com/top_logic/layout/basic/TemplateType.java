/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.template.WithProperties;

/**
 * Annotation to a {@link ThemeVar} definition of type {@link HTMLTemplateFragment} defining the
 * context object type that is rendered with the annotated template.
 * 
 * @see IconsBase Defining code-relevant theme variables.
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface TemplateType {
	/**
	 * The context type in which a {@link HTMLTemplateFragment} is evaluated.
	 */
	Class<? extends WithProperties> value();
}

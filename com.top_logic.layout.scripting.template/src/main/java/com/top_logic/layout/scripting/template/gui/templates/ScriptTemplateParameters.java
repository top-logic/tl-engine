/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * States that a list property holds the parameters of a script template, each a
 * {@link TemplateEditModel.Parameter} with a name and a default value.
 *
 * <p>
 * In the template editor, the parameters are displayed as a table with a row for each parameter.
 * </p>
 *
 * @see TemplateEditModel#getParameters()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("script-template-parameters")
@ControlAnnotation
public @interface ScriptTemplateParameters {

	// Pure marker.

}

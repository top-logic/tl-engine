/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormElement;

/**
 * Implementation of a {@link FormElement} creating a corresponding template for rendering.
 * 
 * @see #createTemplate(FormEditorContext)
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Label("Form part")
public interface FormElementTemplateProvider {

	/**
	 * Creates the actual template.
	 * 
	 * @param context
	 *        Context information for template creation.
	 * 
	 * @return The template for rendering this form element.
	 */
	HTMLTemplateFragment createTemplate(FormEditorContext context);

	/**
	 * Whether the element is rendered over the entire line.
	 * 
	 * @param modelType
	 *        Context type in which this {@link FormElementTemplateProvider} is evaluated.
	 */
	boolean getWholeLine(TLStructuredType modelType);

	/**
	 * Determines whether the {@link FormElementTemplateProvider} is visible.
	 * 
	 * @param context
	 *        Context information for template creation.
	 * 
	 * @implSpec By default it is <code>true</code>.
	 */
	default boolean isVisible(FormEditorContext context) {
		return true;
	}

}

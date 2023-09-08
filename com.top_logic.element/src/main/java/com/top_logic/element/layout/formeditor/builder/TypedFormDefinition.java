/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.formeditor.FormTypeProperty;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * Configuration for a typed {@link FormDefinition}.
 *
 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
 */
public interface TypedFormDefinition extends TypeTemplateParameters {

	/** Configuration name for the value of the {@link #getFormDefinition()}. */
	String FORM_DEFINITION_NAME = "formDefinition";

	/** The form belonging to the type. */
	@FormTypeProperty(TYPE)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Name(FORM_DEFINITION_NAME)
	FormDefinition getFormDefinition();

	/** @see #getFormDefinition() */
	void setFormDefinition(FormDefinition formDefinition);
}

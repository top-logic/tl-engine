/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.model.form.definition.FormDefinition;

/**
 * A template of a {@link FormDefinition} with a name.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormDefinitionTemplate {

	private String _name;

	private FormDefinition _formDefinition;

	/**
	 * Creates a new {@link FormDefinitionTemplate}.
	 * 
	 * @param name
	 *        The displayed name.
	 * @param formDefinition
	 *        The display description of a form.
	 */
	public FormDefinitionTemplate(String name, FormDefinition formDefinition) {
		_name = name;
		_formDefinition = formDefinition;
	}

	/**
	 * The name of the template.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The display description of a form.
	 */
	public FormDefinition getFormDefinition() {
		return _formDefinition;
	}
}

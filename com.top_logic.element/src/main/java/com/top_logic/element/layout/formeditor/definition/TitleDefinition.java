/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.implementation.TitleDefinitionTemplateProvider;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.definition.Heading;

/**
 * A definition of a form title. {@link TagName} is "formTitle".
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("title")
public interface TitleDefinition extends FormElement<TitleDefinitionTemplateProvider>, TextDefinition {

	/** Configuration name for the value of the {@link #getLevel()}. */
	String LEVEL_NAME = "level";

	@Override
	@Mandatory
	ResKey getLabel();

	/**
	 * Sets the level of the heading for the HTML-tag.
	 */
	void setLevel(Heading level);

	/**
	 * Returns the level of the heading for the HTML-tag.
	 */
	@Name(LEVEL_NAME)
	Heading getLevel();
}

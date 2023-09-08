/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.model.form.implementation.SeparatorDefinitionTemplateProvider;

/**
 * A definition of a separator. {@link TagName} is "separator".
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("separator")
public interface SeparatorDefinition extends FormElement<SeparatorDefinitionTemplateProvider> {

	/** Configuration name for the value of the {@link #getVisible()}. */
	String VISIBLE_NAME = "visible";

	/**
	 * Whether the separator is rendered with a border or just separating elements without being
	 * visible.
	 * 
	 * @param visible
	 *        Whether the separator is visible at the GUI.
	 */
	void setVisible(Boolean visible);

	/**
	 * Returns whether the separator is visible at the GUI.
	 * 
	 * @return Whether the separator is visible at the GUI.
	 */
	@Name(VISIBLE_NAME)
	@BooleanDefault(true)
	Boolean getVisible();
}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration of tuples of a {@link TLStructuredType} and a {@link FormDefinition}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Abstract
public interface FormsTemplateParameter extends ConfigurationItem {

	/**
	 * Property name for {@link #getForms()}.
	 */
	String FORMS = "forms";

	/**
	 * Determines the form view.
	 * 
	 * @see FormDefinition
	 */
	@Name(FORMS)
	@Key(TypedFormDefinition.TYPE)
	@Hidden
	Map<TLModelPartRef, TypedFormDefinition> getForms();

}

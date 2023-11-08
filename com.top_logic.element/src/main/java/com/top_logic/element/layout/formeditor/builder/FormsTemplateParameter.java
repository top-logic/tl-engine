/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
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
	 * Form definitions to use for displaying objects in this component.
	 * 
	 * <p>
	 * To display an object, the first form definition is this list is used that matches the
	 * object's type. If no such form definition is found, a form definition is used that is
	 * annotated to the object's type. If there is no such form definition either, a generic form is
	 * rendered displaying all properties of the object.
	 * </p>
	 * 
	 * @see FormDefinition
	 */
	@Name(FORMS)
	@Key(TypedFormDefinition.TYPE)
	@DisplayMinimized
	Map<TLModelPartRef, TypedFormDefinition> getForms();

}

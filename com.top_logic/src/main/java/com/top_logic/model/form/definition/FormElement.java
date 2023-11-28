/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Definition of a form content.
 * 
 * <p>
 * A {@link FormElement} is rendered through its {@link FormElementTemplateProvider} implementation.
 * </p>
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Abstract
public interface FormElement<T extends FormElementTemplateProvider>
		extends PolymorphicConfiguration<T>, ConfigPart, FormContextDefinition {

	/**
	 * @see #getFormContextDefinition()
	 */
	String FORM_CONTEXT_DEFINITION = "form-context-definition";

	/**
	 * The context in which this {@link FormElement} is defined.
	 */
	@Container
	@Name(FORM_CONTEXT_DEFINITION)
	FormContextDefinition getFormContextDefinition();

	@Override
	@Hidden
	Class<? extends T> getImplementationClass();

	@Override
	@DerivedRef({ FORM_CONTEXT_DEFINITION, FormContextDefinition.FORM_CONTEXT_TYPE })
	TLModelPartRef getFormContextType();

}

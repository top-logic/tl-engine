/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

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
public interface FormElement<T extends FormElementTemplateProvider> extends PolymorphicConfiguration<T> {
	@Override
	@Hidden
	Class<? extends T> getImplementationClass();
}

/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;

/**
 * Test component with an empty {@link FormContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyForm extends FormComponent {

	/**
	 * Creates a {@link EmptyForm}.
	 */
	public EmptyForm(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		return new FormContext("context", I18NConstants.CONTEXT);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}

}

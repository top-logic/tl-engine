/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;

/**
 * Test case of {@link FormComponent} failing while {@link #getFormContext() creating} the
 * {@link FormContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FailingFormDemo extends FormComponent {

	/**
	 * Creates a {@link FailingFormDemo} from configuration.
	 */
	public FailingFormDemo(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		throw new RuntimeException("Intentionally thrown from form context creation.");
	}

}

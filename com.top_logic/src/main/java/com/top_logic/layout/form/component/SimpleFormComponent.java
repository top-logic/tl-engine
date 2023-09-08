/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.FormContext;

/**
 * The {@link SimpleFormComponent} can be used with simple JSP and HTML pages.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleFormComponent extends FormComponent {

	public SimpleFormComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		return null;
	}

}


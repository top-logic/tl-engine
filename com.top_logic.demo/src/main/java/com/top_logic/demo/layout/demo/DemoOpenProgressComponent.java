/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;

/**
 * {@link FormComponent} for opening a {@link DemoProgressComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoOpenProgressComponent extends FormComponent {

	public DemoOpenProgressComponent(InstantiationContext context, Config attributes) throws ConfigurationException {
		super(context, attributes);
	}

	@Override
	public FormContext createFormContext() {
		return new FormContext(this);
	}

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;

/**
 * {@link FormComponent} for manually executed test cases, concerning client-side focus restore and
 * tabbing between {@link FormField}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoTabbingComponent extends FormComponent {

	public static final String FIELD1 = "field1";

	public static final String FIELD2 = "field2";

	public DemoTabbingComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		StringField field1 = FormFactory.newStringField(FIELD1);
		StringField field2 = FormFactory.newStringField(FIELD2);

		context.addMember(field1);
		context.addMember(field2);
		return context;
	}
}

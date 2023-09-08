/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;

/**
 * Test class to test dynamic toolbar.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDynamicToolbarTitle extends FormComponent {

	/** Name of the field holding the individuell toobar title text. */
	public static final String TITLE_FIELD = "toolbarTitle";

	/**
	 * Creates a new {@link TestDynamicToolbarTitle}.
	 */
	public TestDynamicToolbarTitle(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		StringField titleField = FormFactory.newStringField(TITLE_FIELD);
		titleField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				String newTitle = (String) newValue;
				TestDynamicToolbarTitle component = TestDynamicToolbarTitle.this;
				DisplayValue toolbarTitle;
				if (StringServices.isEmpty(newTitle)) {
					toolbarTitle = new ResourceText(component.getTitleKey());
				} else {
					toolbarTitle = ConstantDisplayValue.valueOf(newTitle);
				}
				component.getToolBar().setTitle(toolbarTitle);
			}
		});
		context.addMember(titleField);
		return context;
	}

}


/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;

/**
 * Demo From Component for testing.
 * 
 * @author    <a href=mailto:twi@top-logic.com>twi</a>
 */
public class LRMDemoFormComponent extends FormComponent {
	
	public LRMDemoFormComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	/**
	 * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
	 */
	@Override
	public FormContext createFormContext() {
		FormContext theFormContext = new FormContext("LRMDemoFormContext", I18NConstants.LAYOUT_RELATION_MANAGER);
		StringField theValueField = FormFactory.newStringField("MasterDemoModel");
		Object model = this.getModel();
		if (model != null) {
			theValueField.setAsString(model.toString());
		}
		theFormContext.addMember(theValueField);
		return theFormContext;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof DemoTreeNodeModel;
	}

}

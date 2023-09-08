/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link FormComponent} for demonstrating partner relations in
 * {@link WindowComponent}s.
 * 
 * @author <a href=mailto:twi@top-logic.com>twi</a>
 */
public class SeparateWindowPartnerTestComponent extends FormComponent {
	
	public SeparateWindowPartnerTestComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	/**
	 * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
	 */
	@Override
	public FormContext createFormContext() {
		FormContext theFormContext = new FormContext("SeparateWindowPartnerTestFormContext", I18NConstants.SEPARATE_WINDOWS);
		StringField theValueField = FormFactory.newStringField("SepWinPartnerTestModel");
		theValueField.setAsString((String)this.getModel());
		theFormContext.addMember(theValueField);
		
		theFormContext.addMember(FormFactory.newCommandField("save_sepwinPartnerTest", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
		        String theValue = (String) SeparateWindowPartnerTestComponent.this.getFormContext().getField("SepWinPartnerTestModel").getValue();
				SeparateWindowPartnerTestComponent.this.setModel(theValue);
				return HandlerResult.DEFAULT_RESULT;
			}
		}));
		return theFormContext;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof String;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		if (hasFormContext()) {
			this.getFormContext().getField("SepWinPartnerTestModel").setValue(newModel);
		}
	}

}

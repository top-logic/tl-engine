/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.tool.boundsec.assistent.demo;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;


/**
 * For testing!
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class TestingNameInputComponent extends FormComponent {

    public TestingNameInputComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof String;
    }
    
    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        AssistentComponent theAssistent = AssistentComponent.getEnclosingAssistentComponent(this);
        
		ComponentName theStepName = theAssistent.getCurrentStepInfoName();
		String theContextKey = theStepName.localName() + "_FORM_CONTEXT";
        
        FormContext theContext  = (FormContext) theAssistent.getData(theContextKey);
        String      theFileName = (String) theAssistent.getData("FILE_NAME");
        
        if(theContext == null) {
            StringField theName = FormFactory.newStringField("name", true, false, new StringLengthConstraint(1, 40));
            if (theFileName != null) {
                theName.setValue(theFileName);
            }
			theContext = new FormContext(this);
            theContext.addMember(theName);
            theAssistent.setData(theContextKey, theContext);
        }
        return theContext;
    }
        
}

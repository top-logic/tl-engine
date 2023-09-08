/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.tool.dataImport.AbstractDataImporter;

/**
 * The DataImportEndStepComponent is the last step of the DataImportAssistant.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DataImportEndStepComponent extends FormComponent {

    /**
     * Creates a new instance of this class.
     */
    public DataImportEndStepComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext(this);
        AbstractDataImporter theImporter = DataImportAssistant.getImporter(this);
        ProgressResult theResult = theImporter.getCommitResult();
        DataImportAssistant.createMessageFieldsFor(theResult, theContext, getResPrefix());

        StringField theField = FormFactory.newStringField(DataImportAssistant.FIELD_MESSAGE, IMMUTABLE);
        String theMessage = getResString(theResult != null && theResult.isSuccess() ? "commitSucceeded" : "commitFailed");
        theField.setValue(theMessage);
        theContext.addMember(theField);

        return theContext;
   }

}

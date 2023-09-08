/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportPreviewComponent extends FormComponent {

    /** 
     * Creates a {@link GenericDataImportPreviewComponent}.
     */
    public GenericDataImportPreviewComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	/**
	 * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
	 */
    @Override
	public FormContext createFormContext() {
        return null;
    }

//
//    private static class PreviewRowObject {
//        
//        private DataObject plainObject;
//        private DataObject convertedObject;
//        
//        private ValidationResult validResult;
//        
//        public boolean isCreate() {
//            return false;
//        }
//        
//        public boolean isValid() {
//            return validResult.isValid();
//        }
//    }
}


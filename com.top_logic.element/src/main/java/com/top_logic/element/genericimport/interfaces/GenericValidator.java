/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * The GenericValidator checks if a given DataObject matches functional constraints.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericValidator {
 
    public ValidationResult validateSimpleTypes(GenericValueMap aDO, GenericCache aCache);
    
    public ValidationResult validateReferenceTypes(GenericValueMap aDO, GenericCache aCache);
    
    public static class ValidationResult {

        public static final ValidationResult VALID_RESULT = new ValidationResult(null);
        
        private final GenericValueMap object;
        private List                  errors;
        
        public ValidationResult(GenericValueMap anObject) {
            this.object = anObject;
        }
        
        public GenericValueMap getValueMap() {
            return this.object;
        }
        
        public void addError(AttributeError anError) {
            if (this.errors == null) {
                this.errors = new ArrayList(2);
            }
            this.errors.add(anError);
        }
        
        public List getErrors() {
            if (this.errors == null) {
                return Collections.EMPTY_LIST;
            }
            return this.errors;
        }
        
        public boolean isValid() {
            return this.errors == null;
        }

        public static void printErrorMessages(PrintWriter out, ValidationResult aResult) {
            if (! aResult.isValid()) {
                Resources    theRes = Resources.getInstance();
                for (Iterator theErrors = aResult.getErrors().iterator(); theErrors.hasNext();) {
                    AttributeError theError = (AttributeError) theErrors.next();
                    out.print(theError.getAttributeName());
                    out.print(": ");
					out.println(theRes.getString(theError.getErrorI18NKey()));
                    out.flush();
                }
            }
        }
    }
    
    
    public static class AttributeError {
        private final String attr;

		private final ResKey message;
        
		public AttributeError(String anAttributeName, ResKey anErrorI18NKey) {
            this.attr    = anAttributeName;
            this.message = anErrorI18NKey;
        }
        
        public String getAttributeName() {
            return (this.attr);
        }
        
		public ResKey getErrorI18NKey() {
            return (this.message);
        }
    }
}


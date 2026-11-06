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
 * The {@link GenericValidator} checks if a given {@link GenericValueMap} matches functional
 * constraints.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericValidator {
 
    /**
     * Checks the values of the non-reference attributes of the given imported data.
     *
     * @param aDO
     *        The imported data to check.
     * @param aCache
     *        The cache of already imported objects.
     * @return The result of the check, never {@code null}.
     */
    public ValidationResult validateSimpleTypes(GenericValueMap aDO, GenericCache aCache);
    
    /**
     * Checks the values of the reference attributes of the given imported data.
     *
     * @param aDO
     *        The imported data to check.
     * @param aCache
     *        The cache of already imported objects, used to resolve references.
     * @return The result of the check, never {@code null}.
     */
    public ValidationResult validateReferenceTypes(GenericValueMap aDO, GenericCache aCache);
    
    /**
     * Result of a validation by a {@link GenericValidator}, collecting {@link AttributeError}s.
     */
    public static class ValidationResult {

        /**
         * Result without errors.
         */
        public static final ValidationResult VALID_RESULT = new ValidationResult(null);
        
        private final GenericValueMap object;
        private List                  errors;
        
        public ValidationResult(GenericValueMap anObject) {
            this.object = anObject;
        }
        
        /**
         * The validated data.
         */
        public GenericValueMap getValueMap() {
            return this.object;
        }
        
        /**
         * Adds an error to this result, making it invalid.
         *
         * @param anError
         *        The error to add.
         */
        public void addError(AttributeError anError) {
            if (this.errors == null) {
                this.errors = new ArrayList(2);
            }
            this.errors.add(anError);
        }
        
        /**
         * The list of {@link AttributeError}s found, empty if this result is valid.
         */
        public List getErrors() {
            if (this.errors == null) {
                return Collections.EMPTY_LIST;
            }
            return this.errors;
        }
        
        /**
         * Whether no errors were found.
         */
        public boolean isValid() {
            return this.errors == null;
        }

        /**
         * Prints the messages of all errors of the given result.
         *
         * @param out
         *        The writer to print to.
         * @param aResult
         *        The result whose errors are printed.
         */
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
    
    
    /**
     * A validation error of a single attribute.
     */
    public static class AttributeError {
        private final String attr;

		private final ResKey message;
        
		public AttributeError(String anAttributeName, ResKey anErrorI18NKey) {
            this.attr    = anAttributeName;
            this.message = anErrorI18NKey;
        }
        
        /**
         * The name of the invalid attribute.
         */
        public String getAttributeName() {
            return (this.attr);
        }
        
		/**
		 * The message key describing the problem.
		 */
		public ResKey getErrorI18NKey() {
            return (this.message);
        }
    }
}


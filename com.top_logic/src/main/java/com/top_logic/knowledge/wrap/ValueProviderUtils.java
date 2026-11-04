/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;

/**
 * Static helper methods that connect {@link ValueProvider}s.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class ValueProviderUtils {
    
    /** 
     * Copy attribute from {@code aContext} into {@code aModel}.
     * 
     * @param extractSingleValue when true Collections will be reduced to a single value.
     * 
     * @return The object actually set, <code>null</code> when !{@link FormField#isChanged()}. 
     */
    public static Object setAttribute(String attribute, FormContext aContext, ValueProvider aModel, boolean extractSingleValue) {
        FormField theField = aContext.getField(attribute);
        Object    theValue  = null; 
        if (theField.isChanged() && theField.isValid())  {          
        	theValue = theField.getValue();
            if (extractSingleValue && theValue instanceof Collection<?>) {
                theValue = CollectionUtil.getSingleValueFromCollection((Collection<?>) theValue);
            }
            aModel.setValue(attribute, theValue);
            
        }
        return theValue;
    }
    
    /** 
     * Copy attribute from {@code aContext} into {@code aModel}.
     * 
     * @return The object actually set, <code>null</code> when !{@link FormField#isChanged()} or !
     *         {@link FormField#isValid()}.
     */
    public static Object setAttribute(String attribute, FormContext aContext, ValueProvider aModel) {
        FormField theField = aContext.getField(attribute);
        Object    theValue  = null; 
        if (theField.isChanged() && theField.isValid())  {  
        	theValue = theField.getValue();
            aModel.setValue(attribute, theValue);
            
        }
        return theValue;
    }

    /** 
     * Copy value of {@link FormField} for {@code fieldName} in {@code aContext} into {@code aModel}
     * as attribute.
     * 
     * @return The object actually set, <code>null</code> when !{@link FormField#isChanged()} or !
     *         {@link FormField#isValid()}.
     */
    public static Object setAttribute(String fieldName, FormContext aContext, String attribute, ValueProvider aModel) {
        FormField theField = aContext.getField(fieldName);
        Object    theValue  = null; 
        if (theField.isChanged() && theField.isValid())  {
        	theValue = theField.getValue();
            aModel.setValue(attribute, theValue);
            
        }
        return theValue;
    }

    /**
     * Same as {@link #setAttribute(String, FormContext, ValueProvider, boolean)}, but ignores
     * attributes without a field in the given context.
     *
     * @return The object actually set, <code>null</code> if there is no such field or nothing was
     *         set.
     */
    public static Object setAttributeIgnore(String attribute, FormContext aContext, ValueProvider aModel, boolean extractSingleValue) {
        if (aContext.hasMember(attribute)) {
            return setAttribute(attribute, aContext, aModel, extractSingleValue);
        }
        return null;
    }
    
    /**
     * Same as {@link #setAttribute(String, FormContext, String, ValueProvider)}, but ignores
     * fields not contained in the given context.
     *
     * @return The object actually set, <code>null</code> if there is no such field or nothing was
     *         set.
     */
    public static Object setAttributeIgnore(String fieldName, FormContext aContext, String attrName, ValueProvider aModel) {
        if (aContext.hasMember(fieldName)) {
            return setAttribute(fieldName, aContext, attrName, aModel);
        }
        return null;
    }

    /** 
     * Copy attribute from {@code aContext} into {@code aModel}.
     * 
     * @return The object actually set, <code>null</code> when !{@link FormField#isChanged()} or !
     *         {@link FormField#isValid()}.
     */
    public static Object setAttributeIgnore(String attribute, FormContext aContext, ValueProvider aModel) {
        if (aContext.hasMember(attribute)) {
            return setAttribute(attribute, aContext, aModel);
        }
        return null;
    }
    
    /** 
     * Copy attribute from a {@link FormGroup} into {@code aModel}.
     * 
     * @return The object actually set, <code>null</code> when !{@link FormField#isChanged()} or !
     *         {@link FormField#isValid()}.
     */
    public static Object setAttribute(String attribute, FormGroup aFormGroup, ValueProvider aModel) {
        FormField theField = aFormGroup.getField(attribute);
        Object    theValue  = null; 
        if (theField.isChanged() && theField.isValid())  {  
            theValue = theField.getValue();
            aModel.setValue(attribute, theValue);
            
        }
        return theValue;
    }

    

}


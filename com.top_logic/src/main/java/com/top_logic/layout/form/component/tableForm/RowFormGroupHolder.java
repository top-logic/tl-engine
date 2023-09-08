/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.tableForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RowFormGroupHolder implements ValueListener {
    
    SelectField           baseField;
    FormComponent         target;
    RowFormGroupGenerator generator;
    /** Map < String, FormGroup > */
//    Map                   rowGroupNames;

    public RowFormGroupHolder(FormContext aFormContext, SelectField aBaseField, Collection someSelected, FormComponent aTarget, RowFormGroupGenerator aGenerator) {
        super();
        this.baseField     = aBaseField;
        this.target        = aTarget;
        this.generator     = aGenerator;
        aBaseField.addValueListener(this);
        if (someSelected != null) {
            for (Iterator theIt = someSelected.iterator(); theIt.hasNext(); ) {
                this.createParameterFields(aFormContext, theIt.next());
            }
        }

//        this.rowGroupNames = new HashMap();
    }

    @Override
	public final void valueChanged(FormField aField, Object anOldValue, Object aNewValue) {
        Collection theAdded   = copyExept((Collection) aNewValue,  (Collection) anOldValue);
        Collection theRemoved = copyExept((Collection) anOldValue, (Collection) aNewValue);
        FormContext theFC = this.target.getFormContext();
        
        for (Iterator theIt = theRemoved.iterator(); theIt.hasNext();) {
            Object theElement = theIt.next();
            removeParameterFields(theFC, theElement);
        }
        
        for (Iterator theIt = theAdded.iterator(); theIt.hasNext();) {
            Object theElement = theIt.next();
            createParameterFields(theFC, theElement);
        }
        
    }
    
    private Collection copyExept(Collection someObjects, Collection someExceptions) {
        if (someObjects == null) {
            return Collections.EMPTY_LIST;
        }
        if (someExceptions == null) {
            return someExceptions;
        }
        Collection theResult = new ArrayList(someObjects);
        theResult.removeAll(someExceptions);
        return theResult;
    }
    
    protected void createParameterFields(FormContext aFC, Object aParameter) {
        this.generator.addFormFields(this.target, aFC, aParameter);
    }

    private void removeParameterFields(FormContext aFC, Object aParameter) {
        String    theRowGroupName = this.generator.getRowGroupName(aParameter);
        FormGroup theGroup        = (FormGroup) aFC.getMember(theRowGroupName);
        for (Iterator theIt = theGroup.getMemberNames(); theIt.hasNext(); ) {
            FormMember theEditField   = theGroup.getMember(((String) theIt.next()));
            this.target.removeControl(theEditField.getQualifiedName());
        }
        aFC.removeMember(theGroup);
    }
    
    

}

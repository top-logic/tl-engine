/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.tableForm;

import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;


/**
 * An {@link Accessor} that interprets certain property request as requests for form fields
 * and delegates these requests to the configured form group. 
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TableFieldAccessor extends ReadOnlyAccessor<Object> {
    
    /** Prefix identifying columns containing form fields */
    public static final String FIELD_PREFIX = "field.";
    
    /** The form group holding the row groups */
    FormGroup group;
    /** The accessor to the non-field columns */
    Accessor  parentAccessor;
    
    RowFormGroupGenerator generator;
    
	public TableFieldAccessor(RowFormGroupGenerator aGenerator, FormGroup aGroup) {
		this(WrapperAccessor.INSTANCE, aGenerator, aGroup);
	}

    public TableFieldAccessor(Accessor aParentAccessor, RowFormGroupGenerator aGenerator, FormGroup aGroup) {
        super();
        this.group          = aGroup;
        this.parentAccessor = aParentAccessor;
        this.generator         = aGenerator;
    }

    /**
     * Overridden to fetch the named field if requested
     * 
     * @param anObject     the object to fetch the value from
     * @param aProperty    the name of the property to fetch
     * @return the named field if the property starts with the field prefix,
     *         the value of the named property from the given object otherwise
     * 
     * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object anObject, String aProperty) {
        if (aProperty.startsWith(FIELD_PREFIX)) {
            String    theFiledName = aProperty.substring(FIELD_PREFIX.length());
            String    theId        = this.generator.getRowGroupName(anObject);
            if (this.group.hasMember(theId)) {
                FormGroup theGroup     = (FormGroup) this.group.getMember(theId);
                FormField theField     =  theGroup.getField(theFiledName);
				return generator.getControlProvider().createFragment(theField);
            }
            return null;
        } else {
            return this.parentAccessor.getValue(anObject, aProperty);
        }
        
    }

    @Override
	public void setValue(Object object, String property, Object value) {
        throw new UnsupportedOperationException("Readonly accessor.");
    }

}

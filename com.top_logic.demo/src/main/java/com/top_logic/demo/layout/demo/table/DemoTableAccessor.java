/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.beans.IntrospectionException;

import com.top_logic.demo.layout.form.demo.model.DemoPerson;
import com.top_logic.layout.basic.BeanAccessor;
import com.top_logic.layout.table.CellObject;

/**
 * Example of an Acessor as used in the DemoTableComponenet.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DemoTableAccessor extends BeanAccessor {

    /** 
     * Creates a new DemoTableAccessor for DemoPersons.
     */
    public DemoTableAccessor() throws IntrospectionException {
        super(DemoPerson.class);
    }
    
    /**
     * anObject for any value staring with this_
     */
    @Override
	public Object getValue(Object anObject, String aProperty) {
        if (aProperty.startsWith("tooltip_")) {
            return new CellObject(
                    super.getValue(anObject, aProperty.substring(8)),
                    anObject.toString());
        }
        
        if (aProperty.startsWith("fillColumn")) {
        	return new CellObject(
        			aProperty,
        			aProperty);
        }

		if (aProperty.startsWith("classificationDemo")) {
			return null;
		}
        return super.getValue(anObject, aProperty);
    }
}


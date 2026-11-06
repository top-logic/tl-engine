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
 * Example of an accessor as used in the demo tables.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DemoTableAccessor extends BeanAccessor {

    /** 
     * Creates a new {@link DemoTableAccessor} for {@link DemoPerson}s.
     */
    public DemoTableAccessor() throws IntrospectionException {
        super(DemoPerson.class);
    }
    
    /**
     * Wraps the value in a {@link CellObject} with tooltip for properties starting with
     * {@code tooltip_}, and in a {@link CellObject} showing the property name for properties
     * starting with {@code fillColumn}.
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


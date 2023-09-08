/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.bean;

import java.beans.IntrospectionException;

import com.top_logic.basic.DummyIDFactory;


/**
 * A DataObject exporting his own fields as attributes.
 * This class should be extended to make sense.
 *
 * @author    <a href="mailto:asc@top-logic.com">asc</a>
 */
public class DirectDataObject extends BeanDataObject {
    
    /**
     * This constructor exports its own fields as attributes.
     * 
     * @throws IntrospectionException if the instance could not be created.
     */
    public DirectDataObject() throws IntrospectionException {
        // protected Constructor who uses
        // himself (this) as bean
        Class theClass = this.getClass();

        bean = this;
        meta = BeanMORepository.getInstance().getMetaObject(theClass);
		id = DummyIDFactory.createId();
    }

}

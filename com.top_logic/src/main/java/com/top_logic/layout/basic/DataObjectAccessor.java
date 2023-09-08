/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.layout.Accessor;
import com.top_logic.util.error.TopLogicException;

/**
 * Access the attributes of a {@link DataObject}.
 *  
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class DataObjectAccessor implements Accessor {

	/**
	 * Name for an "artificial" property of a {@link DataObject} object that is
	 * mapped to the {@link DataObject}s {@link DataObject#getIdentifier()} getter.
	 */
	public static final String IDENTIFIER_PROPERTY = BasicTypes.IDENTIFIER_ATTRIBUTE_NAME;

    /** The DataObject itself... */
    public static final String SELF = "_self";

	/**
	 * Singleton instance of this class.
	 */
	public static final DataObjectAccessor INSTANCE = new DataObjectAccessor();
	
	/**
	 * Singleton constructor.	 
	 */
	public DataObjectAccessor() {
		super();
	}
	
	@Override
	public Object getValue(Object object, String property) {
        DataObject theDataObject = (DataObject) object;

        if (IDENTIFIER_PROPERTY.equals(property)) {
            return theDataObject.getIdentifier();
        }
        else if (SELF.equals(property)) {
            return (theDataObject);
        }
        else {
            try {
				return theDataObject.getAttributeValue(property);
			} catch (NoSuchAttributeException e) {
				throw new TopLogicException(DataObjectAccessor.class, ".getvaluefailed", e);
			}
        }
	}

	/**
	 * Forbid setting the artificial attributes and provide a setter for the others.
	 * 
	 * @see com.top_logic.layout.Accessor#setValue(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Object object, String property, Object value) {
        DataObject theDataObject = (DataObject) object;
        if (IDENTIFIER_PROPERTY.equals(property)) {
            throw new UnsupportedOperationException();
        } 
        else if (SELF.equals(property)) {
            throw new UnsupportedOperationException();
        }
        else {
			theDataObject.setAttributeValue(property, value);
        }
	}

}

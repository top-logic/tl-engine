/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.dob.DataObject;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.UpdateException;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * The GenericUpdateHandler updates a business object with values from a {@link DataObject}.
 * Typically used for a business object of type {@link Wrapper} or {@link Wrapper}.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericUpdateHandler {
    /** 
     * This method updates the given business object with the given map of values.
     * 
     * @param anObject   The business object to update.
     * @param aDO        The imported data object with the values to update.
     * @param aFKeyAttr  The name of the foreign key attribute.
     * @return Returns the updated business object.
     */
    public Object updateBusinessObject(Object anObject, GenericValueMap aDO, String aFKeyAttr) throws UpdateException;
}


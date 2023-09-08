/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.interfaces;

import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * The GenericCreateHandler creates an business object out of {@link DataObject}. 
 * Typically the handler creates {@link Wrapper} or {@link Wrapper}. 
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface GenericCreateHandler {
    /** 
     * Create a business object from the imported data object.
     * 
     * @param aDO       The data object to create a business object from.
     * @param aFKeyAttr The name of the foreign key attribute for this data object.
     * @return Returns the business object.
     */
    public Object createBusinessObject(GenericValueMap aDO, String aFKeyAttr) throws CreateException;
}


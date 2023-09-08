/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class SupplierFilter extends CompanyContactFilter {

    /** 
     * Creates a {@link SupplierFilter}.
     * 
     */
    public SupplierFilter() {
    }

    @Override
	public boolean accept(Object anObject) {
        if (!(anObject instanceof CompanyContact)) {
            return false;
        }
        
        CompanyContact theContact = (CompanyContact) anObject;
        Boolean isSupplierB = (Boolean) theContact.getValue(COSContactConstants.SUPPLIER);
        boolean isSupplier = (isSupplierB == null) ? false : isSupplierB.booleanValue();
        
        return isSupplier;
    }
}


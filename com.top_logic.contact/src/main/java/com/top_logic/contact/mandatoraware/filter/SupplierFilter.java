/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.TLObject;

/**
 * Accept the value if it is a CompanyContact that may act as a Supplier
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
@Deprecated
public class SupplierFilter extends MandatorAwareContactFilter {
	
    /**
     * Default public CTor for FilterFactory
     */
    public SupplierFilter() {
        super();
    }
    
    /** 
     * Check for CompanyContact and supplier property
     */
    @Override
	protected boolean checkBasics(TLObject anAttributed, EditContext editContext,
			Object anObject) {
        if (!(anObject instanceof CompanyContact)) {
            return false;
        }
        
        CompanyContact theContact = (CompanyContact) anObject;
        Boolean isSupplierB = (Boolean) theContact.getValue(COSContactConstants.SUPPLIER);
        boolean isSupplier = (isSupplierB == null) ? false : isSupplierB.booleanValue();
        
        return isSupplier;
    }

}

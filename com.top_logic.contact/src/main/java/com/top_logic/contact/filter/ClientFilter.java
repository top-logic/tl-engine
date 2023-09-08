/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ClientFilter extends CompanyContactFilter {

    /** 
     * Creates a {@link ClientFilter}.
     * 
     */
    public ClientFilter() {
    }
    
    /** 
     * Check for CompanyContact and supplier property
     */
    protected boolean checkBasics(Wrapper anAttributed, TLStructuredTypePart aMetaAttribute, AttributeUpdateContainer anAttributeUpdateContainer, FormContext aContext, Object anObject) {
        if (!(anObject instanceof CompanyContact)) {
            return false;
        }
        
        CompanyContact theContact = (CompanyContact) anObject;
        Boolean isClientB = (Boolean) theContact.getValue(COSContactConstants.CLIENT);
        boolean isClient = (isClientB == null) ? false : isClientB.booleanValue();
        
        return isClient;
    }

}


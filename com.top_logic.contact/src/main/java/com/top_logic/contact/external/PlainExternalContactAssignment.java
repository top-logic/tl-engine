/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

import java.util.Date;

import com.top_logic.util.Utils;


/**
 * Implement the assignment extension for ExternalContacts.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class PlainExternalContactAssignment extends PlainExternalContact implements
        ExternalContactAssignment {
    
    private final Date start;
    private final Date end;
    
    public PlainExternalContactAssignment(String uNumber, String firstName, String familyName, String division, String eMail, String phone, Date aStart, Date anEnd, String aSysId) {
        super(uNumber, firstName, familyName, division, eMail, phone, aSysId);
        this.start = aStart;
        this.end = anEnd;
    }
    
    @Override
	public boolean equalsContact(ExternalContact contact) {
        boolean isEqual = super.equalsContact(contact);
        if (isEqual && contact instanceof ExternalContactAssignment) {
            isEqual = isEqual &&
                Utils.equals(start, ((ExternalContactAssignment) contact).getStartDate()) && 
                Utils.equals(end, ((ExternalContactAssignment) contact).getEndDate());
        }
        
        return isEqual;
    }

    @Override
	public Date getEndDate() {
        return this.end;
    }

    @Override
	public Date getStartDate() {
        return this.start;
    }
}


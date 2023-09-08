/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.external.ExternalContact;

/**
 * This helper class contains useful static method for sending e-mails.
 */
public class ContactMailHelper extends MailHelper {
    
	/**
	 * Creates a new {@link ContactMailHelper}.
	 */
	public ContactMailHelper(InstantiationContext context, Config<MailHelper> config) {
		super(context, config);
	}

	@Override
	protected String internalGetEmailAddress(Object anObject) {
	    if (anObject instanceof PersonContact) {
	        return ((PersonContact) anObject).getMail();
	    }
	    else if (anObject instanceof ExternalContact) {
	        return ((ExternalContact) anObject).getEMail();
	    }
	    else {
	        return super.internalGetEmailAddress(anObject);
	    }
	}
    
    /**
     * This method returns an empty list if all person contacts in the given
     * collection has a valid e-mail address. In the other case the list
     * contains all person contacts which have no valid e-mail address.
     * 
     * @param ofPersonContacts A collection of {@link PersonContact}s.
     *        <code>Null</code> is not allowed.
     * @return Returns an empty list or a list with all person contacts which
     *         have no valid e-mail address.
     */
    public static List<PersonContact> checkPersonContactsEMailAddresses(Collection<PersonContact> ofPersonContacts){
        ArrayList<PersonContact>  theResult   = new ArrayList<>(ofPersonContacts.size());
        MailHelper theHelper   = MailHelper.getInstance();
        for (PersonContact theContact : ofPersonContacts) {
            if(! theHelper.checkAddressFormat(theContact.getMail())){
                theResult.add(theContact);
            }
            
        }
        theResult.trimToSize();
        return theResult;
    }
}

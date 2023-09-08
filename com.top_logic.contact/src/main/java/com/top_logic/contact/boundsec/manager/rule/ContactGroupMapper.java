/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.boundsec.manager.rule;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.manager.SimpleGroupMapper;

/**
 * TODO TSA this class
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ContactGroupMapper extends SimpleGroupMapper {

    @Override
	public Object map(Object anObject) {
        if (anObject instanceof PersonContact) {
        	Person thePerson = ((PersonContact) anObject).getPerson();
        	return thePerson == null ? null : thePerson.getRepresentativeGroup();
        } else {
            return super.map(anObject);
        }
    }
}

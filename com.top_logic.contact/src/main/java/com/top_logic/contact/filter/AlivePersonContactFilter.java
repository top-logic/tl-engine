/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.filter;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Accept the value if it is a PersonContact for {@link Person#isAlive()}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
@Deprecated
public class AlivePersonContactFilter extends ContextFreeAttributeValueFilter {

    /**
     * Default public CTor for FilterFactory
     */
    public AlivePersonContactFilter() {
        super();
    }

    /**
     * Accept the value if it is a PersonContact for {@link Person#isAlive()}.
     */
    @Override
	public boolean accept(Object anObject) {
    	if (anObject instanceof PersonContact) {
    		PersonContact thePC = (PersonContact) anObject;
    		Person        theP  = thePC.getPerson();
    		return theP != null && theP.isAlive();
    	}
        return false;
    }

}

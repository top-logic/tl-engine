/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Matcher;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.LabelProvider;

/**
 * Provide Labels for {@link PersonContact} or {@link Person}.
 */
public class PersonContactLabelProvider implements LabelProvider, Matcher{

	public static final PersonContactLabelProvider INSTANCE = new PersonContactLabelProvider();
	
	protected PersonContactLabelProvider() {
		// protected to enforce usage of INSTANCE
	}

	@Override
	public String getLabel(Object object) {
		if(object instanceof PersonContact){
			return ((PersonContact)object).getFullname();
		}else if(object instanceof Person){
			return ((Person) object).getFullName();
		}
		Logger.error("Encountered unexpected object type",this);
		return"";
	}

	@Override
	public boolean match(String searchText, Object object) {
		String loginID = null;
		if(object instanceof PersonContact){
			loginID = ((PersonContact)object).getPersonID();
		}else if(object instanceof Person){
			loginID = ((Person)object).getName();
		}
		return (loginID != null && loginID.toLowerCase().indexOf(searchText) >= 0);
    }
}

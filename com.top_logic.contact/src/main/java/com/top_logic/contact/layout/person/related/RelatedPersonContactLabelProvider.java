/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.PersonContactLabelProvider;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link PersonContactLabelProvider} that adds the {@link Person#getName()}, if the
 * {@link PersonContact} has a local account.
 * 
 * @author <a href="mailto:tri@top-logic.com">tri</a>
 */
public class RelatedPersonContactLabelProvider extends PersonContactLabelProvider {
	
	public static final RelatedPersonContactLabelProvider INSTANCE = new RelatedPersonContactLabelProvider();

	protected RelatedPersonContactLabelProvider() {
	}

	@Override
	public String getLabel(Object object) {
		String result = super.getLabel(object);
		if(object instanceof PersonContact){
			try{
				Person aPerson = ((PersonContact)object).getPerson();
				if(aPerson!=null){
					result+=" ("+aPerson.getName()+")";
				}
			}catch(Exception e){
				Logger.error("Problem providing label for contact.",e,this);
			}
		}
		return result;
	}


}

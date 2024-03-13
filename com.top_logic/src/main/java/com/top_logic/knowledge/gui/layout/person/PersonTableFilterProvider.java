/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.filter.FirstCharacterFilterProvider;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class PersonTableFilterProvider extends FirstCharacterFilterProvider {

	/**
	 * Creates a new instance of this class.
	 */
	public PersonTableFilterProvider() {
		super(PersonLastNameMapping.INSTANCE);
	}

	private static class PersonLastNameMapping implements LabelProvider {
        
		static final PersonLastNameMapping INSTANCE = new PersonLastNameMapping();
        
		@Override
		public String getLabel(Object object) {
			if (object instanceof Person) {
				Person account = (Person) object;
				UserInterface user = account.getUser();
				return user == null ? account.getName() : user.getName();
            }
			return String.valueOf(object);
        }
    }

}

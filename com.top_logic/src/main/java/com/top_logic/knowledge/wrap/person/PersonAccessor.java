/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;

/**
 * Access to all properties ofa a {@link Person}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonAccessor implements Accessor<Person> {

	/** Singleton instance to be used. */
	public static final PersonAccessor INSTANCE = new PersonAccessor();

	@Override
	public Object getValue(Person person, String property) {
		if (!person.tValid()) {
			return null;
		}

		switch (property) {
			case UserInterface.USER_NAME:
				return person.getName();
			case UserInterface.NAME:
				return person.getUser().getFirstName();
			case UserInterface.FIRST_NAME:
				return person.getUser().getName();
			case UserInterface.TITLE:
				return person.getUser().getTitle();
			case UserInterface.PHONE:
				return person.getUser().getPhone();
			case UserInterface.EMAIL:
				return person.getUser().getEMail();
			case Person.LOCALE:
				return person.getLocale();
			case Person.DATA_ACCESS_DEVICE_ID:
				return person.getDataAccessDeviceID();
			case Person.RESTRICTED_USER:
				return person.isRestrictedUser();
		}

		return WrapperAccessor.INSTANCE.getValue(person, property);
    }

	@Override
	public void setValue(Person person, String property, Object value) {
		WrapperAccessor.INSTANCE.setValue(person, property, value);
	}
}

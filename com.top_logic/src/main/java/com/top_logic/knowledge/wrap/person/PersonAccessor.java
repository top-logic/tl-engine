/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;

/**
 * Access to forward all {@link PersonAttributes#PERSON_INFO}s to {@link Person#getUser()}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PersonAccessor implements Accessor<Person> {

	/** Singleton instance to be used. */
	public static final PersonAccessor INSTANCE = new PersonAccessor();

	/** {@link PersonAttributes#PERSON_INFO} converted to a Set */
	private static final Set<String> PERSON_INFOS = new HashSet<>(Arrays.asList(PersonAttributes.PERSON_INFO));

	@Override
	public Object getValue(Person person, String property) {
		if (!person.tValid()) {
			return null;
		}
		else if (PersonAccessor.PERSON_INFOS.contains(property)) {
            try {
				return Person.getUser(person).getAttributeValue(property);
			} catch (Exception ex) {
				Logger.error("Failed to get person attribute from user object: " + property, ex, PersonAccessor.class);
            }
        }

		return WrapperAccessor.INSTANCE.getValue(person, property);
    }

	@Override
	public void setValue(Person person, String property, Object value) {
		WrapperAccessor.INSTANCE.setValue(person, property, value);
	}
}

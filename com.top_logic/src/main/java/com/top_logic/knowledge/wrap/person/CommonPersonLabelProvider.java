/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.layout.LabelProvider;

/**
 * A {@link LabelProvider} for {@link Person}s
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CommonPersonLabelProvider implements LabelProvider {

	public static final CommonPersonLabelProvider INSTANCE = new CommonPersonLabelProvider();

	/**
	 * Creates a label of a {@link Person} using following format: <br/>
	 * <code> login(Surname,Forename) </code>
	 * 
	 * @param object
	 *        - the person
	 * @return label of the person
	 * 
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return "";
		}

		Person person = (Person) object;

		return Person.getUser(person).getUserName() + "(" +
			Person.getUser(person).getNameAs_LastTitleFirst(false) + ")";
	}

}

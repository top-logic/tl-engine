/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.util;

import java.util.Comparator;

import com.top_logic.base.user.UserFullNameComparator;
import com.top_logic.base.user.UserInterface;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Compare a Person by resolving its user.
 * 
 * @author    <a href="mailto:tgi@top-logic.com>Thomas Grill</a>
 */
public class PersonComparator implements Comparator<Person> {

    public static final PersonComparator INSTANCE = new PersonComparator();
    
    /** This one does the actual compare */
	private UserFullNameComparator usercomp;

	/**
	 * Creates a new ascending sorting {@link PersonComparator}.
	 */
	public PersonComparator() {
		this(UserFullNameComparator.ASCENDING);
	}

	public PersonComparator(boolean asc) {
		usercomp = new UserFullNameComparator(asc);
	}

	/**
	 * Resolve the users and than call usercomp.compare().
	 */
	@Override
	public int compare(Person p1, Person p2) {
		UserInterface u1 = null;
		UserInterface u2 = null;
		if (p1 != null) {
			u1 = p1.getUser();
		}
		if (p2 != null) {
			u2 = p2.getUser();
		}
		return usercomp.compare(u1, u2);
	}

}
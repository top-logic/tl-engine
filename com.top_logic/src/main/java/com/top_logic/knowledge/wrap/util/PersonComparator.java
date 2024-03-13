/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.util;

import java.util.Comparator;

import com.top_logic.base.user.UserInterface;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Factory for {@link Comparator}s for {@link Person}s.
 */
public class PersonComparator {

	/**
	 * Creates a locale specific instance.
	 */
	public static Comparator<? super Person> getInstance() {
		return Comparator.comparing(Person::getUser, UserInterface.comparator(TLContext.getLocale()));
	}

}
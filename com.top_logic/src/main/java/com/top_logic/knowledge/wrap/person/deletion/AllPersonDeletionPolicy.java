/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.deletion;

import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link PersonDeletionPolicy} that does not restrict the persons to delete.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllPersonDeletionPolicy extends PersonDeletionPolicy {

	/** Singleton {@link AllPersonDeletionPolicy} instance. */
	public static final AllPersonDeletionPolicy INSTANCE = new AllPersonDeletionPolicy();

	private AllPersonDeletionPolicy() {
		// singleton instance
	}

	@Override
	public Set<Person> restrictPersonsToDelete(PersonManager manager, Set<Person> personsToDelete) {
		return personsToDelete;
	}

}


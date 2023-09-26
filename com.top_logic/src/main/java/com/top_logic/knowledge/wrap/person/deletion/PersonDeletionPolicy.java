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
 * Plugin for the {@link PersonManager} to restrict the persons without user to delete.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PersonDeletionPolicy {

	/**
	 * Restricts the persons for which no user is found to delete.
	 * 
	 * <p>
	 * The given set of persons are the persons for which no user exists. If not all of them must be
	 * deleted, the persons not to delete may be removed. It is possible to modify the given set or
	 * to return a new one.
	 * </p>
	 * 
	 * @param manager
	 *        The {@link PersonManager} that wants to delete the given persons.
	 * @param personsWithoutUser
	 *        The {@link Person}s for which no user can be found.
	 * 
	 * @return A set of {@link Person} that must be deleted. The returned value may be modifiable or
	 *         unmodifiable. It may be the given set or a different.
	 */
	public abstract Set<Person> restrictPersonsToDelete(PersonManager manager, Set<Person> personsWithoutUser);

}


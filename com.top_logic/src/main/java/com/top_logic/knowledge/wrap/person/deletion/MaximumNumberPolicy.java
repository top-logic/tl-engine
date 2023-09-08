/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.deletion;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link ConfiguredDeletionPolicy} that allows a maximum number of persons to delete. If there are
 * more persons to delete, no person is deleted and an error is logged.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MaximumNumberPolicy extends ConfiguredDeletionPolicy {

	/**
	 * Configuration of a {@link MaximumNumberPolicy}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredDeletionPolicy.Config {

		/** Configuration name of the property {@link #getMaxNumberOfPersonsToDelete()}. */
		String MAX_NUMBER_OF_PERSONS_TO_DELETE_PROPERTY = "max-number-of-persons-to-delete";

		/**
		 * Defining the maximum numbers of persons without user to delete.
		 */
		@Mandatory
		@Name(MAX_NUMBER_OF_PERSONS_TO_DELETE_PROPERTY)
		int getMaxNumberOfPersonsToDelete();
	}

	/**
	 * Creates a new {@link MaximumNumberPolicy}.
	 */
	public MaximumNumberPolicy(InstantiationContext context, Config config) {
		super(context, config);
		if (config.getMaxNumberOfPersonsToDelete() < 0) {
			StringBuilder negativeMsg = new StringBuilder();
			negativeMsg.append("Maximum number of persons to delete must not be negative: ");
			negativeMsg.append(config.getMaxNumberOfPersonsToDelete());
			context.error(negativeMsg.toString());
		}
	}

	@Override
	public Set<Person> restrictPersonsToDelete(PersonManager manager, Set<Person> personsToDelete) {
		int maxNumberOfPersonsToDelete = ((Config) getConfig()).getMaxNumberOfPersonsToDelete();
		boolean deleteAllowed = personsToDelete.size() <= maxNumberOfPersonsToDelete;
		if (deleteAllowed) {
			return personsToDelete;
		} else {
			StringBuilder toMuchUsersToDelete = new StringBuilder();
			toMuchUsersToDelete.append("There are ");
			toMuchUsersToDelete.append(personsToDelete.size());
			toMuchUsersToDelete.append(" users to be deleted, allowed are only ");
			toMuchUsersToDelete.append(maxNumberOfPersonsToDelete);
			Logger.error(toMuchUsersToDelete.toString(), MaximumNumberPolicy.class);
			return Collections.emptySet();
		}
	}

}

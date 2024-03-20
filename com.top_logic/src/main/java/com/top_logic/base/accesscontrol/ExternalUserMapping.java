/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Mapping of an external user name, i.e. the name in the authentication system identifying the
 * {@link Person}, to the {@link Person} to login.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExternalUserMapping {

	/**
	 * Retrieves the logged-in {@link Person} using the name that the account has in the
	 * authentication system.
	 * 
	 * @param externalUserName
	 *        Name of the account in the authentication system.
	 * 
	 * @throws LoginDeniedException
	 *         When it is not possible to retrieve a valid {@link Person}.
	 */
	Person findAccountForExternalName(String externalUserName) throws LoginDeniedException;

	/**
	 * Finds the {@link Person} with the given {@link Person#getName() user name}.
	 * 
	 * @param loginName
	 *        <code>TopLogic</code> name of the {@link Person} to login.
	 * @return Valid non <code>null</code> {@link Person} with the given name.
	 * 
	 * @throws LoginDeniedException
	 *         If no {@link Person} with the given name was found.
	 */
	static Person findAccount(String loginName) throws LoginDeniedException {
		if (StringServices.isEmpty(loginName)) {
			throw new LoginDeniedException("No login name was given.");
		}
		Person person = Person.byName(loginName);
		if (person == null) {
			String message = "No account with login name '" + loginName + "' found!";
			throw new LoginDeniedException(message);
		}
		if (!person.isAlive()) {
			String message = "Account with login name '" + loginName + "' is not alive!";
			throw new LoginDeniedException(message);
		}
		return person;

	}

}


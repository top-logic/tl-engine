/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.util.Arrays;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * The {@link LoginCredentials} are:
 * <ul>
 * <li>The obligatory username (login name of the user)</li>
 * <li>Maybe an password (Is not used in most cases of Single Sign On.)</li>
 * <li>Maybe a domainName</li>
 * <li>The person corresponding to the given username.</li>
 * </ul>
 * Depending on the method used to login (Normal / various version of Single Sign On) a different
 * combination of these credentials is used. To prevent having to create various methods for every
 * used combination, the {@link LoginCredentials} are combined in this class.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoginCredentials implements AutoCloseable {

	private final String _username;

	private final char[] _password;

	private final String _domainName;

	private final Person _person;

	/**
	 * @param username
	 *        Must not be <code>null</code> or the empty string.
	 * @throws LoginDeniedException
	 *         If the userName is <code>null</code>, or resolving a person with that name throws
	 *         a {@link LoginDeniedException}.
	 */
	public static LoginCredentials fromUsername(String username) {
		return new LoginCredentials(username, null, null);
	}

	/**
	 * @param username
	 *        Must not be <code>null</code> or the empty string.
	 * @throws LoginDeniedException
	 *         If the userName is <code>null</code>, or resolving a person with that name throws a
	 *         {@link LoginDeniedException}.
	 */
	public static LoginCredentials fromUsernameAndPassword(String username, char[] password) {
		return new LoginCredentials(username, password, null);
	}

	/**
	 * @param user
	 *        Must be an {@link Person#isAlive() alive} not <code>null</code> person.
	 * 
	 * @throws LoginDeniedException
	 *         If the user is <code>null</code> or not alive.
	 */
	public static LoginCredentials fromUserAndPassword(Person user, char[] password) {
		return new LoginCredentials(user, password);
	}

	/**
	 * @param username
	 *        Must not be <code>null</code> or the empty string.
	 * @throws LoginDeniedException
	 *         If the userName is <code>null</code>, or resolving a person with that name throws
	 *         a {@link LoginDeniedException}.
	 */
	public static LoginCredentials fromUsernameAndDomain(String username, String domain) {
		return new LoginCredentials(username, null, domain);
	}

	/**
	 * @param user
	 *        Must be an {@link Person#isAlive() alive} not <code>null</code> person.
	 * 
	 * @param password
	 *        Is allowed to be <code>null</code>.
	 * @throws LoginDeniedException
	 *         If the user is <code>null</code> or not alive.
	 */
	protected LoginCredentials(Person user, char[] password) {
		_person = checkPerson(user);
		_username = _person.getName();
		_password = password;
		_domainName = null;
	}

	/**
	 * @param username
	 *        Must not be <code>null</code> or the empty string.
	 * @param password
	 *        Is allowed to be <code>null</code>.
	 * @param domainName
	 *        Is allowed to be <code>null</code>.
	 * @throws LoginDeniedException
	 *         If the userName is <code>null</code>, or resolving a person with that name throws
	 *         a {@link LoginDeniedException}.
	 */
	protected LoginCredentials(String username, char[] password, String domainName) {
		_person = retrievePersonChecked(username);
		_username = username;
		_password = password;
		_domainName = domainName;
	}

	/** Never <code>null</code>. */
	public Person getPerson() {
		return _person;
	}

	/** Never <code>null</code>. */
	public String getUsername() {
		return _username;
	}

	/** Can be <code>null</code> */
	public char[] getPassword() {
		return _password;
	}

	/** Can be <code>null</code> */
	public String getDomainName() {
		return _domainName;
	}

	/**
	 * Never <code>null</code>
	 * @throws LoginDeniedException
	 *         If something is wrong with the given loginName or the corresponding person. For
	 *         example if the loginName is <code>null</code> or empty. If no person with that
	 *         loginName is found, ...
	 */
	private static Person retrievePersonChecked(String loginName) {
		if (StringServices.isEmpty(loginName)) {
			throw new LoginDeniedException("No loginName was given.");
		}
		Person person = PersonManager.getManager().getPersonByName(loginName);
		return checkPerson(person, loginName);
	}

	private static Person checkPerson(Person person) {
		if (person == null) {
			String message = "Person must not be null!";
			throw new LoginDeniedException(message);
		}
		if (!person.isAlive()) {
			String message = "Person '" + person + "' is not alive!";
			throw new LoginDeniedException(message);
		}
		if (Person.getUser(person) == null) {
			String message = "No User for the person with login name '" + person.getName() + "' found!";
			throw new LoginDeniedException(message);
		}
		return person;
	}

	private static Person checkPerson(Person person, String loginName) {
		if (person == null) {
			String message = "No Person with login name '" + loginName + "' found!";
			throw new LoginDeniedException(message);
		}
		if (!person.isAlive()) {
			String message = "Person with login name '" + loginName + "' is not alive!";
			throw new LoginDeniedException(message);
		}
		if (Person.getUser(person) == null) {
			String message = "No User for the person with login name '" + loginName + "' found!";
			throw new LoginDeniedException(message);
		}
		return person;
	}

	@Override
	public void close() {
		// clear password
		if (_password != null) {
			Arrays.fill(_password, '0');
		}
	}

}
/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Mapping of a user authenticated by an external authentication system to the {@link Person} to
 * login.
 *
 * <p>
 * A login that knows only a name (the header login of {@link J2eeRemoteUserAuthenticationServlet})
 * calls {@link #findAccountForExternalName(String)}. A login that knows the whole verified identity
 * (the pac4j login, e.g. OIDC) calls {@link #findAccountForIdentity(ExternalIdentity)},
 * which resolves the {@link ExternalIdentity#getName() name} of the identity unless a mapping
 * overrides it.
 * </p>
 *
 * <h2>Context of the call</h2>
 *
 * <p>
 * Both methods are called in the request of the external authentication servlet
 * ({@link ExternalAuthenticationServlet}) after the authentication system has verified the user
 * and before the session is logged in. The thread runs in the session and window context of that
 * request, which is not yet the context of the account to find, and no transaction is open. A
 * mapping that creates or links an account opens and commits a transaction of its own.
 * </p>
 *
 * <p>
 * The same call resolves the account when a running session has its user confirm their identity
 * once more ({@link ExternalAuthenticationServlet#VERIFICATION_PARAM}). The mapping must therefore
 * not assume that a call is the first login of the user, and must find the same account for the
 * same identity.
 * </p>
 *
 * <p>
 * An {@link UnknownAccountException} sends the user to the page explaining that the application
 * has no account for them. Any other {@link LoginDeniedException} lets the login fail.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExternalUserMapping {

	/**
	 * Retrieves the logged-in {@link Person} using the name that the account has in the
	 * authentication system.
	 *
	 * <p>
	 * See {@link ExternalUserMapping} for the context of the call.
	 * </p>
	 *
	 * @param externalUserName
	 *        Name of the account in the authentication system.
	 *
	 * @throws UnknownAccountException
	 *         If the application has no account for the given name.
	 * @throws LoginDeniedException
	 *         When it is not possible to retrieve a valid {@link Person}.
	 */
	Person findAccountForExternalName(String externalUserName) throws LoginDeniedException;

	/**
	 * Retrieves the logged-in {@link Person} for the whole identity that the authentication system
	 * has verified.
	 *
	 * <p>
	 * By default, the account is resolved from the {@link ExternalIdentity#getName() name} of the
	 * identity through {@link #findAccountForExternalName(String)}. A mapping overrides this method
	 * to find an account by something the name does not express, e.g. by the stable
	 * {@link ExternalIdentity#getSubject() subject} of the identity provider. Example of a mapping
	 * that remembers the subject at the account and links an invited account on the first login by
	 * the verified e-mail address:
	 * </p>
	 *
	 * <pre>
	 * public Person findAccountForIdentity(ExternalIdentity identity) {
	 * 	Person account = findBySubject(identity.getClientName(), identity.getSubject());
	 * 	if (account != null) {
	 * 		return account;
	 * 	}
	 * 	if (Boolean.TRUE.equals(identity.getClaim(EMAIL_VERIFIED_CLAIM))) {
	 * 		Invitation invitation = findInvitation((String) identity.getClaim(EMAIL_CLAIM));
	 * 		if (invitation != null) {
	 * 			try (Transaction tx = kb.beginTransaction(...)) {
	 * 				account = invitation.linkAccount(identity.getClientName(), identity.getSubject());
	 * 				tx.commit();
	 * 			}
	 * 			return account;
	 * 		}
	 * 	}
	 * 	throw new UnknownAccountException(identity.getName(), "No account for " + identity + ".");
	 * }
	 * </pre>
	 *
	 * <p>
	 * See {@link ExternalUserMapping} for the context of the call.
	 * </p>
	 *
	 * @param identity
	 *        The identity the authentication system has verified.
	 *
	 * @throws UnknownAccountException
	 *         If the application has no account for the given identity.
	 * @throws LoginDeniedException
	 *         When it is not possible to retrieve a valid {@link Person}.
	 */
	default Person findAccountForIdentity(ExternalIdentity identity) throws LoginDeniedException {
		return findAccountForExternalName(identity.getName());
	}

	/**
	 * Finds the {@link Person} with the given {@link Person#getName() user name}.
	 * 
	 * @param loginName
	 *        <code>TopLogic</code> name of the {@link Person} to login.
	 * @return Valid non <code>null</code> {@link Person} with the given name.
	 * 
	 * @throws UnknownAccountException
	 *         If the authentication system has authenticated the given name, but this application
	 *         has no account of that name, or the account is not alive.
	 * @throws LoginDeniedException
	 *         If no name was given at all.
	 */
	static Person findAccount(String loginName) throws LoginDeniedException {
		if (StringServices.isEmpty(loginName)) {
			throw new LoginDeniedException("No login name was given.");
		}
		Person person = Person.byName(loginName);
		if (person == null) {
			String message = "No account with login name '" + loginName + "' found!";
			throw new UnknownAccountException(loginName, message);
		}
		if (!person.isAlive()) {
			String message = "Account with login name '" + loginName + "' is not alive!";
			throw new UnknownAccountException(loginName, message);
		}
		return person;

	}

}


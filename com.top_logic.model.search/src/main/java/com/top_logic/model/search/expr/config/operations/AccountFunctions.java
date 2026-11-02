/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * TL-Script functions about accounts.
 */
@ScriptPrefix("account")
public class AccountFunctions extends TLScriptFunctions {

	/**
	 * Whether the given account is the anonymous one, which stands for a session nobody has
	 * logged in to.
	 *
	 * <p>
	 * The current account of a session is an account like any other, so this is how a view tells
	 * an anonymous session from one with a user: <code>currentUser().accountIsAnonymous()</code>.
	 * </p>
	 *
	 * @param account
	 *        The account to test.
	 * @return Whether the account is the anonymous account.
	 */
	@Label("Account is anonymous")
	@SideEffectFree
	public static boolean isAnonymous(@Mandatory Person account) {
		return PersonManager.getManager().isAnonymous(account);
	}

	/**
	 * Whether a new password can be set for the given account.
	 *
	 * <p>
	 * An account is authenticated by a device, and a device that keeps the password elsewhere - a
	 * directory service, an identity provider - refuses to change it. This is how a view offers a
	 * password change only where it can be applied:
	 * <code>currentUser().accountPasswordChangeAllowed()</code>.
	 * </p>
	 *
	 * @param account
	 *        The account to test.
	 * @return Whether the account's password can be changed.
	 *
	 * @implNote Asks {@link Person#getAuthenticationDevice()} for
	 *           {@link AuthenticationDevice#allowPwdChange()}. An account without a device has no
	 *           password to change.
	 */
	@Label("Password change allowed")
	@SideEffectFree
	public static boolean passwordChangeAllowed(@Mandatory Person account) {
		AuthenticationDevice device = account.getAuthenticationDevice();
		return device != null && device.allowPwdChange();
	}

	/**
	 * The time of the last login of every account that has logged in.
	 *
	 * <p>
	 * The logins of all accounts are read with a single database query, so a table of accounts
	 * computes the map once and looks each row up in it:
	 * <code>$lastLogins[$row]</code>. An account that has never logged in has no entry.
	 * </p>
	 *
	 * <p>
	 * The logins of others are personal data, so the function is reserved for an administrator
	 * when called interactively.
	 * </p>
	 *
	 * @return The time of the latest login indexed by the account.
	 *
	 * @implNote Reads the recorded sessions with {@link UserSession#getLastLogins(KnowledgeBase)}.
	 */
	@Label("Last logins of all accounts")
	@AdminOnly
	public static Map<Person, Date> lastLogins() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Map<String, Date> loginByName;
		try {
			loginByName = UserSession.getLastLogins(kb);
		} catch (SQLException ex) {
			throw new RuntimeException("Unable to read the last logins of the accounts.", ex);
		}
		Map<Person, Date> result = new HashMap<>();
		for (Person account : Person.all(kb)) {
			Date login = loginByName.get(account.getName());
			if (login != null) {
				result.put(account, login);
			}
		}
		return result;
	}
}

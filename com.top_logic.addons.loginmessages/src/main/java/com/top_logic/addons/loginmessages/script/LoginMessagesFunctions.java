/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.addons.loginmessages.model.LoginMessagesUtil;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions showing the {@link LoginMessage}s to an account at its login and recording
 * their confirmation.
 *
 * <p>
 * The functions name the account explicitly, so that they serve a login that has not completed
 * yet, e.g. a dialog step of the React login: the window then still belongs to the anonymous
 * user, whose personal configuration is not the account's.
 * </p>
 *
 * <p>
 * The account is taken as given, without checking that it is the current user's: a script may ask
 * which messages any account has not confirmed yet, and confirm the messages displayed to it. This
 * affects nothing but which login messages the account is shown.
 * </p>
 *
 * @implNote The confirmations are kept in the persistent personal configuration of the account
 *           ({@link PersonalConfigurationWrapper}), under the names of
 *           {@link LoginMessagesUtil#getConfirmName(LoginMessage)}, where the classic login hook
 *           keeps them too. A confirmation is written there directly, before the session of the
 *           account starts: the transient personal configuration of a session is created from
 *           the persistent one when the user of a window is set
 *           ({@link com.top_logic.util.TLContext#setPerson(Person)} resets it), so the session
 *           started afterwards carries the confirmation and writes it back at its logout.
 */
@ScriptPrefix(LoginMessagesFunctions.PREFIX)
public class LoginMessagesFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "loginMessages";

	/**
	 * The login messages to show to the given account at its login.
	 *
	 * <p>
	 * A message is shown when it is active, today lies between its start and its end date, and the
	 * account has not confirmed it yet, or its confirmation has expired. A view shows them with
	 * <code>loginMessagesDisplay($account)</code> and records their confirmation with
	 * <code>loginMessagesConfirm($display)</code>.
	 * </p>
	 *
	 * <p>
	 * The messages are model objects: a script running for a user who may not read them - the
	 * anonymous user of a login that has not completed - does not receive them, see
	 * {@link #display(Person)}.
	 * </p>
	 *
	 * @param account
	 *        The account logging in.
	 * @return The messages to show, sorted by name in descending order.
	 */
	@Label("Due login messages")
	@SideEffectFree
	public static List<LoginMessage> due(@Mandatory Person account) {
		PersonalConfiguration configuration = PersonalConfigurationWrapper.getPersonalConfiguration(account);
		List<LoginMessage> result = new ArrayList<>();
		for (LoginMessage message : LoginMessagesUtil.getLoginMessagesSortedByNameDescending()) {
			Date confirmDate =
				configuration == null ? null : LoginMessagesUtil.getConfirmDate(configuration, message);
			if (LoginMessagesUtil.isDue(confirmDate, message)) {
				result.add(message);
			}
		}
		return result;
	}

	/**
	 * The login messages due for the given account, as the login shows them.
	 *
	 * <p>
	 * The result is displayed as HTML, e.g. with <code>&lt;html input="..."/&gt;</code>: the text of
	 * each due message in the current language with its images (as <code>data:</code> URIs), or its
	 * name where it has no text, one below the other. The result also says which messages were
	 * shown: <code>loginMessagesConfirm($display)</code> confirms exactly these, for the given
	 * account.
	 * </p>
	 *
	 * <p>
	 * The result is no model object, so a script of a login that has not completed - running for the
	 * anonymous user, who may not read the login messages - receives it as well.
	 * </p>
	 *
	 * @param account
	 *        The account logging in.
	 * @return The display of the messages {@link #due(Person) due} for the account; empty when none
	 *         is due.
	 */
	@Label("Display of the due login messages")
	@SideEffectFree
	public static LoginMessagesDisplay display(@Mandatory Person account) {
		return new LoginMessagesDisplay(account, due(account));
	}

	/**
	 * Records that the account of the given display has confirmed the messages shown in it now.
	 *
	 * <p>
	 * A confirmed message is not shown again until its confirmation expires.
	 * </p>
	 *
	 * @param display
	 *        The messages shown to the account, the result of
	 *        <code>loginMessagesDisplay($account)</code>.
	 */
	@Label("Confirm login messages")
	public static void confirm(@Mandatory LoginMessagesDisplay display) {
		if (display.isEmpty()) {
			return;
		}
		Person account = display.getAccount();
		try (Transaction tx = account.getKnowledgeBase()
			.beginTransaction(I18NConstants.CONFIRMED_LOGIN_MESSAGES__USER.fill(account.getName()))) {
			PersonalConfigurationWrapper configuration =
				PersonalConfigurationWrapper.createPersonalConfiguration(account);
			if (configuration == null) {
				throw new IllegalStateException(
					"No personal configuration could be created for '" + account.getName() + "'.");
			}
			Date now = new Date();
			for (LoginMessage message : display.getMessages()) {
				LoginMessagesUtil.setConfirmDate(configuration, message, now);
			}
			tx.commit();
		}
	}

}

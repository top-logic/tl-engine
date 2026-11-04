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
	 * <code>loginMessagesDue($account)</code> and records their confirmation with
	 * <code>loginMessagesConfirm($account, $messages)</code>.
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
	 * Records that the given account has confirmed the given login messages now.
	 *
	 * <p>
	 * A confirmed message is not shown again until its confirmation expires.
	 * </p>
	 *
	 * @param account
	 *        The account that has confirmed the messages.
	 * @param messages
	 *        The confirmed messages, e.g. the result of <code>loginMessagesDue($account)</code>.
	 */
	@Label("Confirm login messages")
	public static void confirm(@Mandatory Person account, List<LoginMessage> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}
		try (Transaction tx = account.getKnowledgeBase()
			.beginTransaction(I18NConstants.CONFIRMED_LOGIN_MESSAGES__USER.fill(account.getName()))) {
			PersonalConfigurationWrapper configuration =
				PersonalConfigurationWrapper.createPersonalConfiguration(account);
			if (configuration == null) {
				throw new IllegalStateException(
					"No personal configuration could be created for '" + account.getName() + "'.");
			}
			Date now = new Date();
			for (LoginMessage message : messages) {
				LoginMessagesUtil.setConfirmDate(configuration, message, now);
			}
			tx.commit();
		}
	}

}

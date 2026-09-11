/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
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
}

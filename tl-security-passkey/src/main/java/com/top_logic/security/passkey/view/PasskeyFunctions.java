/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.security.passkey.PasskeyService;

/**
 * TL-Script functions about the passkeys of accounts.
 */
@ScriptPrefix(PasskeyFunctions.PREFIX)
public class PasskeyFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "passkey";

	/**
	 * Whether the given account may create passkeys.
	 *
	 * <p>
	 * This is how a view offers the creation of a passkey only to an account that may have one:
	 * <code>passkeyMayUse(currentUser())</code>. Neither the anonymous account nor an account whose
	 * second factor is switched off for good may create one.
	 * </p>
	 *
	 * @param account
	 *        The account to test.
	 * @return Whether the account may create passkeys.
	 *
	 * @implNote Asks {@link PasskeyService#mayUsePasskeys(Person)}.
	 */
	@Label("Account may use passkeys")
	@SideEffectFree
	public static boolean mayUse(@Mandatory Person account) {
		return PasskeyService.getInstance().mayUsePasskeys(account);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;

/**
 * TL-Script functions about the second factors of the login, see {@link SecondFactor}.
 */
@ScriptPrefix(SecondFactorFunctions.PREFIX)
public class SecondFactorFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "secondFactor";

	/**
	 * The names of the second factor methods the given account has set up.
	 *
	 * <p>
	 * Asks every method the application offers for its login, so a view states whether an account
	 * has a second factor without knowing the methods: <code>secondFactorEnrolled($account)</code>
	 * is empty for an account logging in with its password alone.
	 * </p>
	 *
	 * @param account
	 *        The account to ask for.
	 * @return The names of the methods the account has set up, in the order the application lists
	 *         the methods, in the language of the current user.
	 *
	 * @implNote The methods are the {@link SecondFactorStep#configured() configured} ones, asked
	 *           with {@link SecondFactor#isEnrolled(Person)}.
	 */
	@Label("Second factors set up")
	@SideEffectFree
	public static List<String> enrolled(@Mandatory Person account) {
		Resources resources = Resources.getInstance();
		List<String> result = new ArrayList<>();
		for (SecondFactor method : SecondFactorStep.enrolled(account, SecondFactorStep.configured())) {
			result.add(resources.getString(method.getLabel()));
		}
		return result;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.util.Collection;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.access.model.AccessToken;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions behind the views that create and revoke personal access tokens of the agent
 * endpoints.
 *
 * <p>
 * Both act for the account that runs the script and check it here rather than in the view: a
 * command's input is whatever a channel holds, and these functions are reachable from the script
 * console and a scheduler task as well.
 * </p>
 */
@ScriptPrefix("ai")
public class AccessTokenFunctions extends TLScriptFunctions {

	/**
	 * Creates a personal access token for the account running the script, and returns the token.
	 *
	 * <p>
	 * The returned token is the only copy: what is stored is its hash, so show it to its owner once
	 * and do not keep it anywhere.
	 * </p>
	 *
	 * <p>
	 * Example: <code>aiAccessTokenCreate("Claude Code laptop", 90)</code>
	 * </p>
	 *
	 * @param name
	 *        What the owner calls the token, typically the client it is meant for.
	 * @param validForDays
	 *        How many days from now the token is accepted. Empty for a token that does not expire.
	 * @return The token, starting with <code>tlpat_</code>.
	 */
	public static String accessTokenCreate(String name, Integer validForDays) {
		Person owner = TLContext.currentUser();
		if (owner == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_ACCOUNT);
		}
		return AccessTokens.create(owner, name, validForDays).plain();
	}

	/**
	 * Revokes a personal access token: the next request presenting it is refused.
	 *
	 * <p>
	 * The owner revokes their own tokens, an administrator any token.
	 * </p>
	 *
	 * @param token
	 *        The <code>tl.ai.access:AccessToken</code> to revoke.
	 * @return Always <code>true</code>; a refusal is an error, not a <code>false</code>.
	 */
	public static boolean accessTokenRevoke(Object token) {
		AccessTokens.revoke(revocable(token));
		return true;
	}

	private static AccessToken revocable(Object token) {
		AccessToken resolved = token instanceof AccessToken found ? found : null;
		if (resolved == null && token instanceof Collection<?> many && many.size() == 1) {
			resolved = many.iterator().next() instanceof AccessToken found ? found : null;
		}
		if (resolved == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TOKEN);
		}
		Person actor = TLContext.currentUser();
		if (actor == null || (!actor.equals(resolved.getOwner()) && !actor.isAdmin())) {
			throw new TopLogicException(I18NConstants.ERROR_TOKEN_NOT_OWNED);
		}
		return resolved;
	}

}

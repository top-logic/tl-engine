/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.security.auth.pac4j.login.Pac4jLoginMethod;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions about the external logins connected with the account of the session, see
 * {@link OidcIdentity}.
 *
 * <p>
 * Every function acts on the account of the current session only: no script can list or remove
 * the identities of another account through them.
 * </p>
 */
@ScriptPrefix(OidcIdentityFunctions.PREFIX)
public class OidcIdentityFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "oidc";

	/**
	 * The names of the external login clients that can be connected with an account.
	 *
	 * <p>
	 * The clients of the available pac4j login methods that can
	 * {@link LoginMethod#canIdentify() identify} the user of a running session. Empty in an
	 * application without such a client: <code>oidcClients().isEmpty()</code> decides whether a
	 * view offers to connect logins at all.
	 * </p>
	 *
	 * @return The client names, which are the {@link LoginMethod#getId() IDs} of their login
	 *         methods, in the order the application lists its login methods.
	 */
	@Label("Connectable login clients")
	@SideEffectFree
	public static List<String> clients() {
		List<String> result = new ArrayList<>();
		for (LoginMethod method : LoginMethods.available(LoginMethods.all())) {
			if (method instanceof Pac4jLoginMethod && method.canIdentify()) {
				result.add(method.getId());
			}
		}
		return result;
	}

	/**
	 * The external logins connected with the account of the current session.
	 *
	 * @return The identities; empty for the anonymous session.
	 */
	@Label("Connected logins")
	@SideEffectFree
	public static List<OidcIdentity> identities() {
		Person account = sessionAccount();
		return account == null ? List.of() : OidcIdentities.getIdentities(account);
	}

	/**
	 * Why the given login cannot be removed from the account of the current session, or
	 * <code>null</code> if it can.
	 *
	 * <p>
	 * A login of another account, and every login in the anonymous session, cannot be removed; the
	 * last way to sign in to the account cannot either, see
	 * {@link OidcIdentities#canRemove(Person, OidcIdentity)}. Suited as reason of a disabled
	 * command: <code>&lt;disabled-if expr="oidcRemoveRefusal($identity)"/&gt;</code>.
	 * </p>
	 *
	 * @param identity
	 *        The login to remove.
	 */
	@Label("Reason not to remove a login")
	@SideEffectFree
	public static ResKey removeRefusal(@Mandatory OidcIdentity identity) {
		Person account = sessionAccount();
		if (account == null || !account.equals(OidcIdentities.getOwner(identity))) {
			return I18NConstants.ERROR_NOT_CONNECTED;
		}
		if (!OidcIdentities.canRemove(account, identity)) {
			return I18NConstants.ERROR_LAST_LOGIN;
		}
		return null;
	}

	/**
	 * Removes the given login from the account of the current session, in a transaction of its
	 * own.
	 *
	 * <p>
	 * Refused with the reason of {@link #removeRefusal(OidcIdentity)}.
	 * </p>
	 *
	 * @param identity
	 *        The login to remove.
	 * @throws TopLogicException
	 *         If the login cannot be removed.
	 */
	@Label("Remove a login")
	public static void remove(@Mandatory OidcIdentity identity) throws TopLogicException {
		Person account = sessionAccount();
		if (account == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_CONNECTED);
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.REMOVE_IDENTITY)) {
			OidcIdentities.removeIdentity(account, identity);
			tx.commit();
		}
	}

	/**
	 * The account of the current session, or <code>null</code> for the anonymous session.
	 */
	private static Person sessionAccount() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		return TLContext.currentUser();
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.headless.AgentAccess;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} withdrawing the {@link AccessTokens access token} passed as its input: the
 * token is deleted, and an agent presenting it is no longer admitted.
 *
 * <p>
 * An account revokes its own tokens; revoking another account's token is an administrative
 * operation and needs the permission the administrative view is guarded with.
 * </p>
 */
public class RevokeAccessTokenAction implements ViewAction {

	/**
	 * Configuration for {@link RevokeAccessTokenAction}.
	 */
	@TagName("revoke-access-token")
	public interface Config extends PolymorphicConfiguration<RevokeAccessTokenAction> {

		@Override
		@ClassDefault(RevokeAccessTokenAction.class)
		Class<? extends RevokeAccessTokenAction> getImplementationClass();
	}

	/**
	 * Creates a {@link RevokeAccessTokenAction} from configuration.
	 */
	@CalledByReflection
	public RevokeAccessTokenAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject token) || !AccessTokens.type().equals(token.tType())) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_TOKEN_NO_TOKEN);
		}

		String sessionKey = (String) token.tValue(AccessTokens.part(AccessTokens.SESSION_KEY));
		try (Transaction tx = token.tKnowledgeBase().beginTransaction()) {
			AccessTokens.withdraw(token);
			tx.commit();
		}

		if (sessionKey != null && !stillAdmitted(sessionKey)) {
			// Another token admitting an agent to the same session keeps its own registration; this
			// one's is dropped only when no token refers to the session any more.
			AgentAccess.getInstance().unbindSession(sessionKey);
		}
		return null;
	}

	private static boolean stillAdmitted(String sessionKey) {
		for (TLObject token : AccessTokens.all()) {
			if (sessionKey.equals(token.tValue(AccessTokens.part(AccessTokens.SESSION_KEY)))
				&& AccessTokens.isValid(token)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The account whose tokens the current user may withdraw without an administrative permission.
	 */
	static Person currentAccount() {
		return TLContext.currentUser();
	}

}

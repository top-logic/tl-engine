/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Admits an agent that presents an {@link AccessTokens access token} its owner issued.
 *
 * <p>
 * The presented secret is matched against the stored hash; a revoked or expired token admits
 * nobody. Every admission is recorded on the token, so its owner sees that an agent is using it.
 * </p>
 */
public class AccessTokenAuthenticator implements AgentAuthenticator {

	/**
	 * Configuration of {@link AccessTokenAuthenticator}.
	 */
	@TagName("access-token")
	public interface Config extends AgentAuthenticator.Config {

		@Override
		@ClassDefault(AccessTokenAuthenticator.class)
		Class<? extends AgentAuthenticator> getImplementationClass();
	}

	/**
	 * Creates an {@link AccessTokenAuthenticator} from configuration.
	 */
	@CalledByReflection
	public AccessTokenAuthenticator(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public AgentCredential authenticate(String credential) {
		return TLContext.inSystemContext(AccessTokenAuthenticator.class, () -> {
			TLObject token = AccessTokens.resolve(credential);
			if (token == null || !AccessTokens.isValid(token)) {
				return null;
			}
			Person owner = (Person) token.tValue(AccessTokens.part(AccessTokens.OWNER));
			if (owner == null || !owner.tValid()) {
				return null;
			}

			try (Transaction tx = token.tKnowledgeBase().beginTransaction()) {
				AccessTokens.markUsed(token);
				tx.commit();
			} catch (RuntimeException ex) {
				// Recording the use is bookkeeping for the owner; a token that is valid stays valid
				// even when the note cannot be written.
				Logger.warn("Cannot record the use of an access token.", ex, AccessTokenAuthenticator.class);
			}

			boolean mayAct = Boolean.TRUE.equals(token.tValue(AccessTokens.part(AccessTokens.MAY_ACT)));
			String sessionKey = (String) token.tValue(AccessTokens.part(AccessTokens.SESSION_KEY));
			return new AgentCredential(owner, mayAct, sessionKey);
		});
	}

}

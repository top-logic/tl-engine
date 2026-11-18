/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.time.Instant;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Admits an agent that presents an {@link AccessToken access token} with a permission for the user
 * interface.
 *
 * <p>
 * A token with {@value #UI_OBSERVE_PERMISSION} lets the agent read the session the token was issued
 * from, a token with {@value #UI_ACT_PERMISSION} (which includes the former) lets it act in that
 * session as well. Both permissions bind to that session. A token without one of them, an
 * unknown, revoked or expired token, and a token of a deleted account admit nobody. Every admission
 * is recorded on the token, so its owner sees that an agent is using it.
 * </p>
 *
 * @see AccessToken#getSessionKey()
 */
public class AccessTokenAuthenticator extends AbstractConfiguredInstance<AccessTokenAuthenticator.Config>
		implements AgentAuthenticator {

	/**
	 * Name of the {@link com.top_logic.element.accesstoken.AccessTokenPermissions access token
	 * permission} to read the user interface of the session the token was issued from.
	 */
	public static final String UI_OBSERVE_PERMISSION = "ui-observe";

	/**
	 * Name of the {@link com.top_logic.element.accesstoken.AccessTokenPermissions access token
	 * permission} to act in the session the token was issued from. Implies
	 * {@link #UI_OBSERVE_PERMISSION}.
	 */
	public static final String UI_ACT_PERMISSION = "ui-act";

	/**
	 * Configuration of {@link AccessTokenAuthenticator}.
	 */
	@TagName("access-token")
	public interface Config extends AgentAuthenticator.Config<AccessTokenAuthenticator> {
		// No configuration options.
	}

	/**
	 * Creates an {@link AccessTokenAuthenticator} from configuration.
	 */
	@CalledByReflection
	public AccessTokenAuthenticator(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public AgentCredential authenticate(String credential) {
		if (!AccessTokens.hasFormat(credential)) {
			return null;
		}
		return TLContext.inSystemContext(AccessTokenAuthenticator.class, () -> {
			AccessToken token = AccessTokens.find(credential);
			if (token == null || !AccessTokens.hasPermission(token, UI_OBSERVE_PERMISSION)) {
				return null;
			}
			Instant now = Instant.now();
			if (AccessTokens.isExpired(token, now)) {
				return null;
			}
			Person owner = token.getOwner();
			if (owner == null || !owner.tValid()) {
				return null;
			}

			try {
				AccessTokens.touch(token, now);
			} catch (RuntimeException ex) {
				// Recording the use is bookkeeping for the owner; a token that is valid stays valid
				// even when the note cannot be written.
				Logger.warn("Cannot record the use of an access token.", ex, AccessTokenAuthenticator.class);
			}

			boolean mayAct = AccessTokens.hasPermission(token, UI_ACT_PERMISSION);
			return new AgentCredential(owner, mayAct, token.getSessionKey());
		});
	}

}

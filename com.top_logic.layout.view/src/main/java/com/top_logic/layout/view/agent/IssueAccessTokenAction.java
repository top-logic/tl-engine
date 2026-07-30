/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.io.IOError;
import java.io.IOException;
import java.util.UUID;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.headless.AgentAccess;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} issuing an {@link AccessTokens access token} for the current account from the
 * form input passed as its input.
 *
 * <p>
 * The input is a {@code tl.agent:NewAccessToken} carrying the token's label, its validity in hours
 * and whether the agent may act. The action returns a transient carrier of the invitation — the
 * application's address together with the issued token — which the surrounding chain hands to the
 * dialog displaying it, the one and only time the token is readable. The token admits the agent to
 * the session it is issued from, which is registered with {@link AgentAccess} here.
 * </p>
 */
public class IssueAccessTokenAction implements ViewAction {

	/**
	 * Path of the agent endpoint below the application, named in the invitation so that an agent
	 * knows where to talk to rather than having to guess it.
	 */
	private static final String AGENT_API_PATH = "/agent-api";

	/**
	 * Configuration for {@link IssueAccessTokenAction}.
	 */
	@TagName("issue-access-token")
	public interface Config extends PolymorphicConfiguration<IssueAccessTokenAction> {

		@Override
		@ClassDefault(IssueAccessTokenAction.class)
		Class<? extends IssueAccessTokenAction> getImplementationClass();
	}

	/**
	 * Creates an {@link IssueAccessTokenAction} from configuration.
	 */
	@CalledByReflection
	public IssueAccessTokenAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject request)) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_TOKEN_NO_INPUT);
		}
		Person account = TLContext.currentUser();
		if (account == null) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_TOKEN_NO_ACCOUNT);
		}

		String label = (String) request.tValue(request.tType().getPart(AccessTokens.LABEL));
		if (StringServices.isEmpty(label)) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_TOKEN_NO_LABEL);
		}
		Number validHours = (Number) request.tValue(request.tType().getPart(AccessTokens.VALID_HOURS));
		if (validHours == null || validHours.intValue() <= 0) {
			throw new TopLogicException(I18NConstants.ERROR_ACCESS_TOKEN_NO_VALIDITY);
		}
		boolean mayAct = Boolean.TRUE.equals(request.tValue(request.tType().getPart(AccessTokens.MAY_ACT)));

		String sessionKey = UUID.randomUUID().toString();
		String secret;
		try (Transaction tx = account.tKnowledgeBase().beginTransaction()) {
			secret = AccessTokens.issue(account, label, validHours.intValue(), mayAct, sessionKey);
			tx.commit();
		}
		AgentAccess.getInstance()
			.bindSession(sessionKey, DefaultDisplayContext.getDisplayContext().asRequest().getSession());

		// The invitation travels on as the action's result, so that the surrounding chain opens the
		// dialog displaying it. This is the one and only time the token in it is readable.
		TLObject issued = TransientObjectFactory.INSTANCE.createObject(AccessTokens.issuedType());
		issued.tUpdate(AccessTokens.issuedType().getPart(AccessTokens.INVITATION), invitation(secret));
		return issued;
	}

	/**
	 * The invitation the owner hands to an agent: where the application is, and the token admitting
	 * the agent to this session.
	 *
	 * <p>
	 * An agent needs both, so both are copied in one go. The application's address is the publicly
	 * reachable one — the {@link AliasManager#HOST} alias when the application configures it,
	 * because the address seen from inside a request can be a load balancer's or a container's,
	 * which no agent could reach.
	 * </p>
	 */
	private static String invitation(String secret) {
		StringBuilder invitation = new StringBuilder();
		try {
			LayoutUtils.appendHostURL(DefaultDisplayContext.getDisplayContext(), invitation);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		invitation.append(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
		invitation.append(AGENT_API_PATH);
		invitation.append(' ');
		invitation.append(secret);
		return invitation.toString();
	}

}

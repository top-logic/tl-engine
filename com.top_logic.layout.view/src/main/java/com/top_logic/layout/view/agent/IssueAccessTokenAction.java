/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.util.UUID;

import com.top_logic.basic.CalledByReflection;
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
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} issuing an {@link AccessTokens access token} for the current account from the
 * form input passed as its input.
 *
 * <p>
 * The input is a {@code tl.agent:NewAccessToken} carrying the token's label, its validity in hours
 * and whether the agent may act. The action writes the plain secret back into that same object, so
 * the form displays it — the one and only time it is readable. The token admits the agent to the
 * session it is issued from, which is registered with {@link AgentAccess} here.
 * </p>
 */
public class IssueAccessTokenAction implements ViewAction {

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

		request.tUpdate(request.tType().getPart(AccessTokens.SECRET), secret);
		return input;
	}

}

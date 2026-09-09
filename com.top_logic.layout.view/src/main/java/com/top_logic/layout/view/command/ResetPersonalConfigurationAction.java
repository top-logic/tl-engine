/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.util.TLContext;

/**
 * Discards everything the current user has adjusted about the presentation of the application, so
 * it looks to them the way it looks to somebody who has never used it.
 *
 * <p>
 * That is the chosen theme, the column widths of a table, the state of a collapsed panel - whatever
 * has accumulated in the personal configuration. What the account carries as data of its own - the
 * language, the country, the timezone - is not part of it and stays.
 * </p>
 *
 * <p>
 * The stored configuration and the copy the running session holds are dropped together; keeping
 * either would restore the other. The page is then reloaded, because what was just discarded is
 * what the page it renders was built from.
 * </p>
 */
@InApp
public class ResetPersonalConfigurationAction implements ViewAction {

	/**
	 * Configuration for {@link ResetPersonalConfigurationAction}.
	 */
	@TagName("reset-personal-configuration")
	public interface Config extends PolymorphicConfiguration<ResetPersonalConfigurationAction> {

		@Override
		@ClassDefault(ResetPersonalConfigurationAction.class)
		Class<? extends ResetPersonalConfigurationAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ResetPersonalConfigurationAction} from configuration.
	 */
	@CalledByReflection
	public ResetPersonalConfigurationAction(InstantiationContext context, Config config) {
		// No configuration needed: the session's own account is the one reset.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Person account = TLContext.currentUser();
		if (account == null) {
			return input;
		}

		try (Transaction tx = account.tKnowledgeBase()
			.beginTransaction(I18NConstants.RESET_PERSONAL_CONFIGURATION__USER.fill(account.getName()))) {
			TLContext session = TLContext.getContext();
			if (session != null && account == session.getPerson()) {
				session.resetPersonalConfiguration();
			}
			PersonalConfigurationWrapper stored =
				PersonalConfigurationWrapper.getPersonalConfiguration(account);
			if (stored != null) {
				stored.tDelete();
			}
			tx.commit();
		}

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		}
		return input;
	}

}

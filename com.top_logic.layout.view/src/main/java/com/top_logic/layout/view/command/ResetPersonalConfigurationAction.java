/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
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
import com.top_logic.layout.react.window.ReactWindowRegistry;
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
 * The stored configuration and the copies the session holds are dropped together - one per browser
 * tab, each of them a subsession of its own - because keeping any of them would restore the others
 * as soon as the tab it belongs to writes it out.
 * </p>
 *
 * <p>
 * Every window of the session is then rebuilt, because what was just discarded is what the page it
 * displays was built from. The transient display state of those pages goes with it: a table's
 * selection, its scroll position, the input of a form.
 * </p>
 *
 * @implNote The windows are rebuilt through {@link ReactWindowRegistry#rebuildWindows()}, which
 *           leaves the trees in place and has the reload of each window replace them - the command
 *           pipeline runs to its end on the tree that triggered it.
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
			resetSubSessions(account);
			PersonalConfigurationWrapper stored =
				PersonalConfigurationWrapper.getPersonalConfiguration(account);
			if (stored != null) {
				stored.tDelete();
			}
			tx.commit();
		}

		context.getWindowRegistry().rebuildWindows();
		return input;
	}

	/**
	 * Drops the transient personal configuration of every subsession that operates on behalf of the
	 * given account.
	 *
	 * <p>
	 * Each browser tab of the session has a subsession of its own with a copy of the configuration,
	 * and a tab may be logged in as a different account, which keeps what it adjusted.
	 * </p>
	 *
	 * @param account
	 *        The account whose configuration is reset.
	 */
	private void resetSubSessions(Person account) {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return;
		}
		TLSessionContext session = context.getSessionContext();
		if (session == null) {
			return;
		}
		for (TLSubSessionContext subSession : session.getSubSessions().values()) {
			if (account == subSession.getPerson()) {
				subSession.resetPersonalConfiguration();
			}
		}
	}

}

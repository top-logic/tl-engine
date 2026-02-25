/*
 * Copyright (c) 2026 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.knowledge.monitor;

import java.util.Date;
import java.util.Set;

import com.top_logic.base.accesscontrol.SessionService.UserEventListener;
import com.top_logic.base.bus.UserEvent;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link UserEventListener} storing the {@link UserEvent} in the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StoreUserEventListener extends AbstractConfiguredInstance<StoreUserEventListener.Config>
		implements UserEventListener {

	/** Config attribute for the user names to exclude from creating login and logout events. */
	public static final String CONF_EXCLUDE_UIDS = "excludeUIDs";

	/**
	 * Typed configuration interface definition for {@link StoreUserEventListener}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<StoreUserEventListener> {
		/**
		 * The user names to exclude from beeing logged.
		 */
		@Name(CONF_EXCLUDE_UIDS)
		@Label("Exclude user IDs")
		@Format(CommaSeparatedStringSet.class)
		Set<String> getExcludeUIDs();

	}

	/**
	 * Create a {@link StoreUserEventListener}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StoreUserEventListener(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Notifies this {@link UserMonitor} about the given {@link UserEvent}.
	 */
	@Override
	public void notifyUserEvent(UserEvent evt) {
		switch (evt.type()) {
			case LOGGED_IN:
				this.login(evt);
				return;
			case LOGGED_OUT:
				this.logout(evt);
				return;
		}
		throw new UnreachableAssertion("No such enum " + evt.type());
	}

	/**
	 * Handle the login of a user.
	 * 
	 * @param anEvent
	 *        The event holding the information about the login.
	 */
	protected void login(UserEvent anEvent) {
		Person theUser = anEvent.passiveUser();
		Date theDate = anEvent.date();

		String userName = theUser.getName();
		if (isExcluded(userName)) {
			return;
		}
		KnowledgeBase kb = theUser.tKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.LOGGED_IN_USER__USER.fill(userName))) {
			UserSession.startSession(kb, userName, anEvent.sessionID(), anEvent.machine(), theDate);
			// This should happen in the (TL-/DB-)context of the user logging in.
			tx.commit();
		}
	}

	/**
	 * Handle the logout of a user.
	 * 
	 * @param anEvent
	 *        The event holding the information about the logout.
	 * @return <code>true</code>, if ending the session succeeds.
	 */
	protected boolean logout(UserEvent anEvent) {
		Person theUser = anEvent.passiveUser();
		Date theDate = anEvent.date();

		String userName = theUser.getName();
		if (isExcluded(userName)) {
			return false;
		}

		KnowledgeBase kb = theUser.tKnowledgeBase();
		UserSession theSession = UserSession.findUserSession(kb, userName, anEvent.sessionID());
		boolean theResult;
		try (Transaction tx = kb.beginTransaction(I18NConstants.LOGGED_OUT_USER__USER.fill(userName))) {
			theResult = theSession.endSession(theDate);

			if (theResult) {
				tx.commit();
			}
		}

		return (theResult);
	}

	private boolean isExcluded(String userName) {
		return getConfig().getExcludeUIDs().contains(userName);
	}

}


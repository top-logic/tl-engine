/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Date;
import java.util.List;

import javax.management.MBeanAttributeInfo;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.util.AbstractStartStopListener;

/**
 * MBean to monitor data of the users.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class UserMBean extends AbstractDynamicMBean {

	/** {@link ConfigurationItem} for the {@link UserMBean}. */
	public interface Config extends AbstractDynamicMBean.Config {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=UserMBean")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link UserMBean}. */
	public UserMBean(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[1];

		attributes[0] = new MBeanAttributeInfo(
			"AmountLoggedInUsers", // name
			"java.lang.Integer", // type
			"The number of actual logged in users.", // description
			true, // readable
			false, // writable
			false); // isIs

		return attributes;
	}

	/**
	 * Calculates the number of actual logged in users with respect to the last system start.
	 * 
	 * @return Number of users which are logged at this moment.
	 */
	@CalledByReflection
	public int getAmountLoggedInUsers() {
		return ThreadContextManager.inSystemInteraction(UserMBean.class, this::calcLoggedInUsers);
	}

	private int calcLoggedInUsers() {
		KnowledgeBase base = PersistencyLayer.getKnowledgeBase();
		RevisionQuery<UserSession> queryResolved = createQuery();
		List<UserSession> result = base.search(queryResolved);

		return result.size();
	}

	private RevisionQuery<UserSession> createQuery() {
		Expression isLoggedIn = isLoggedIn();
		Expression isLoggedInAfterStartup = isLoggedInAfterStartup();
		
		RevisionQuery<UserSession> queryResolved =
			queryResolved(filter(allOf(UserSession.OBJECT_NAME), and(isLoggedIn, isLoggedInAfterStartup)),
				UserSession.class);

		return queryResolved;
	}

	private Expression isLoggedIn() {
		Expression logoutAttr = attribute(UserSession.OBJECT_NAME, UserSession.LOGOUT);
		Expression isLoggedIn = isNull(logoutAttr);
		return isLoggedIn;
	}

	private Expression isLoggedInAfterStartup() {
		// The logout column might be null too if the user has not been logged out correctly, when
		// the system crashed.
		Date startUpDate = AbstractStartStopListener.startUpDate();
		Expression loginAttr = attribute(UserSession.OBJECT_NAME, UserSession.LOGIN);
		Expression isLoggedInAfterStartup = ge(loginAttr, literal(startUpDate));
		return isLoggedInAfterStartup;
	}

}

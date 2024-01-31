/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Date;
import java.util.List;

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
public class MonitorUser implements MonitorUserMBean, NamedMonitor {

	@Override
	public String getName() {
		return "com.test.top_logic.monitor.data:name=MonitorUser";
	}

	@Override
	public int getAmountLoggedInUsers() {
		return ThreadContextManager.inSystemInteraction(MonitorUser.class, this::calcLoggedInUsers);
	}

	private int calcLoggedInUsers() {
		KnowledgeBase aBase = PersistencyLayer.getKnowledgeBase();
		RevisionQuery<UserSession> queryResolved = createQuery();
		List<UserSession> theResult = aBase.search(queryResolved);

		return theResult.size();
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

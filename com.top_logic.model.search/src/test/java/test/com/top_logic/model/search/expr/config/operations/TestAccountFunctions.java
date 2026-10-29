/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.AccountFunctions;

/**
 * Test of {@link AccountFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestAccountFunctions extends AbstractSearchExpressionTest {

	/** A point in time with whole seconds, which every database stores exactly. */
	private static final long BASE = 1_700_000_000_000L;

	private static final long HOUR = 60 * 60 * 1000L;

	private Person _loggedIn;

	private Person _neverLoggedIn;

	private final List<UserSession> _sessions = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_loggedIn = TestPerson.createPerson("lastLoginUser");
			_neverLoggedIn = TestPerson.createPerson("lastLoginNever");
			_sessions.add(UserSession.startSession(kb(), _loggedIn.getName(), "s1", "127.0.0.1", new Date(BASE)));
			_sessions.add(
				UserSession.startSession(kb(), _loggedIn.getName(), "s2", "127.0.0.1", new Date(BASE + 2 * HOUR)));
			_sessions.add(
				UserSession.startSession(kb(), _loggedIn.getName(), "s3", "127.0.0.1", new Date(BASE + HOUR)));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			for (UserSession session : _sessions) {
				session.tDelete();
			}
			_loggedIn.tDelete();
			_neverLoggedIn.tDelete();
			tx.commit();
		}
		_sessions.clear();
		_loggedIn = null;
		_neverLoggedIn = null;
		super.tearDown();
	}

	public void testLastLogins() {
		Map<Person, Date> lastLogins = AccountFunctions.lastLogins();

		assertEquals(new Date(BASE + 2 * HOUR), lastLogins.get(_loggedIn));
		assertFalse(lastLogins.containsKey(_neverLoggedIn));
	}

	public void testLookupInScript() throws Exception {
		assertEquals(new Date(BASE + 2 * HOUR), eval("x -> accountLastLogins()[$x]", _loggedIn));
		assertNull(eval("x -> accountLastLogins()[$x]", _neverLoggedIn));
	}

	public static Test suite() {
		return suite(TestAccountFunctions.class);
	}

}

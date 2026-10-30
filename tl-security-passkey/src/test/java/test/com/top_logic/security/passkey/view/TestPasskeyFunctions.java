/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey.view;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.view.PasskeyFunctions;
import com.top_logic.util.model.ModelService;

/**
 * Tests for the TL-Script functions of {@link PasskeyFunctions}.
 */
public class TestPasskeyFunctions extends BasicTestCase {

	private final List<Person> _accounts = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		super.tearDown();
	}

	/**
	 * <code>passkeyMayUse()</code> is true for an account with a second factor, false for an
	 * account whose second factor is disabled and for the anonymous account.
	 */
	public void testMayUse() throws Exception {
		QueryExecutor mayUse = compile("a -> passkeyMayUse($a)");

		Person account = TestPerson.createPerson("functionsMayUse");
		_accounts.add(account);
		assertEquals(Boolean.TRUE, mayUse.execute(account));

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Disable MFA"))) {
			account.setMFARequirement(MfaRequirement.DISABLED);
			tx.commit();
		}
		assertEquals(Boolean.FALSE, mayUse.execute(account));

		assertEquals(Boolean.FALSE, mayUse.execute(PersonManager.getManager().getAnonymous()));
	}

	private static QueryExecutor compile(String script) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
	}

	/**
	 * The suite, with a knowledge base holding the passkey model and the TL-Script functions.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyFunctions.class),
				TypeIndex.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}

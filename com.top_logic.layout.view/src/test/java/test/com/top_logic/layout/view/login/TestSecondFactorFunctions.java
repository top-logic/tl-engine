/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.SecondFactorFunctions;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.layout.view.login.TotpSecondFactor;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;

/**
 * Tests the TL-Script functions of {@link SecondFactorFunctions} with the second factor methods the
 * module configures.
 */
public class TestSecondFactorFunctions extends BasicTestCase {

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
	 * <code>secondFactorEnrolled()</code> names the configured methods an account has set up: none
	 * for an account without a TOTP secret, the authenticator app for one with a secret.
	 */
	public void testEnrolled() throws Exception {
		List<SecondFactor> configured = SecondFactorStep.configured();
		SecondFactor totp = configured.stream().filter(TotpSecondFactor.class::isInstance).findFirst().orElse(null);
		assertNotNull("The module configures the authenticator app.", totp);

		QueryExecutor enrolled = compile("a -> secondFactorEnrolled($a)");

		Person account = TestPerson.createPerson("functionsEnrolled");
		_accounts.add(account);
		assertEquals(List.of(), enrolled.execute(account));

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			account.setMFASecret(new Password("not-decrypted"));
			tx.commit();
		}
		assertEquals(List.of(Resources.getInstance().getString(totp.getLabel())), enrolled.execute(account));
	}

	private static QueryExecutor compile(String script) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
	}

	/**
	 * The suite, with a knowledge base holding the accounts and the TL-Script functions.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSecondFactorFunctions.class),
				TypeIndex.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}

}

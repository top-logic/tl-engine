/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * End-to-end test of the security scaffold in {@code WEB-INF/conf/secdemo.config.xml}: the engine's
 * model access rights are evaluated for the acting account when TL-Script runs with security - the
 * same way the agent's TL-Script tool runs it.
 *
 * <p>
 * The scaffold is an opt-in aspect that the shipped demo does not enable by default. The test
 * application configuration ({@code src/test/webapp/WEB-INF/conf/metaConf.txt}) lists the very same
 * aspect file, so the test and the shipped configuration cannot drift apart.
 * </p>
 *
 * @see com.top_logic.ai.demo.secdemo.SecDemoSetupService
 */
public class SecDemoModelSecurityTest extends BasicTestCase {

	private Person _previousUser;

	@Override
	protected void tearDown() throws Exception {
		if (_previousUser != null) {
			TLContext.getContext().setCurrentPerson(_previousUser);
		}
		super.tearDown();
	}

	/**
	 * A reader may read the attributes of a {@code Note}.
	 */
	public void testReaderReadsNotes() {
		actAs("secreader1");

		Object titles = eval("all(`secdemo:Note`).get(`secdemo:Note#title`)");

		assertFalse("The reader must see the note titles.", asCollection(titles).isEmpty());
	}

	/**
	 * A reader has no CREATE grant on {@code Note} and must be rejected.
	 */
	public void testReaderCannotCreateNote() {
		actAs("secreader1");

		assertDenied("new(`secdemo:Note`)");
	}

	/**
	 * {@code Secret} has no grant at all, so even its attribute values stay empty for the reader.
	 */
	public void testReaderCannotReadSecretValues() {
		actAs("secreader1");

		Object values = eval("all(`secdemo:Secret`).get(`secdemo:Secret#value`)");

		assertTrue("The reader must not see any secret value, but got: " + values,
			asCollection(values).isEmpty());
	}

	/**
	 * {@code filterSecurity(...)} drops the objects the reader may not see.
	 */
	public void testSecretsAreFilteredForReader() {
		actAs("secreader1");

		assertFalse("Precondition: the demo data must contain secrets.",
			asCollection(eval("all(`secdemo:Secret`)")).isEmpty());
		assertTrue("Secrets must be filtered away for the reader.",
			asCollection(eval("filterSecurity(all(`secdemo:Secret`))")).isEmpty());
	}

	/**
	 * An editor has a CREATE grant on {@code Note}.
	 */
	public void testEditorCreatesNote() {
		actAs("seceditor1");

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			Object created = eval("new(`secdemo:Note`)");

			assertNotNull("The editor must be allowed to create a note.", created);
			tx.rollback();
		}
	}

	/**
	 * No demo role may create a {@code Secret}.
	 */
	public void testEditorCannotCreateSecret() {
		actAs("seceditor1");

		assertDenied("new(`secdemo:Secret`)");
	}

	private void actAs(String login) {
		TLContext context = TLContext.getContext();
		if (_previousUser == null) {
			_previousUser = context.getPerson();
		}
		Person account = Person.byName(login);
		assertNotNull("The demo account '" + login + "' must have been seeded by "
			+ SecDemoSetupService.class.getSimpleName() + ".", account);
		assertFalse("The demo account '" + login + "' must not be an administrator, "
			+ "otherwise it would bypass the checks under test.", account.isAdmin());
		context.setCurrentPerson(account);
	}

	private void assertDenied(String script) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			try {
				eval(script);
				fail("Expected '" + script + "' to be denied for account "
					+ TLContext.currentUser().getName() + ".");
			} catch (TopLogicException expected) {
				// Expected: the engine reports the missing right as an application error.
			} finally {
				tx.rollback();
			}
		}
	}

	/**
	 * Evaluates the given TL-Script the way {@code TLScriptExecutor} does: compiled with the
	 * security checks enabled.
	 */
	private static Object eval(String script) {
		QueryExecutor executor = QueryExecutor.compile(parse(script));
		return executor.executeWith(executor.context(true, null, null), Args.none());
	}

	private static Expr parse(String script) {
		try {
			return ExprFormat.INSTANCE.getValue("script", script);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to parse TL-Script expression: " + script, ex);
		}
	}

	private static Collection<?> asCollection(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return List.of(value);
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		List<BasicRuntimeModule<?>> modules = new ArrayList<>();
		modules.add(SearchBuilder.Module.INSTANCE);
		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		modules.add(PersonManager.Module.INSTANCE);
		modules.add(SecDemoSetupService.Module.INSTANCE);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(SecDemoModelSecurityTest.class),
				modules.toArray(new BasicRuntimeModule<?>[modules.size()])));
	}

}

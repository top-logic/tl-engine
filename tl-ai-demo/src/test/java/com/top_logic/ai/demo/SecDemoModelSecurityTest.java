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
 * End-to-end test of the model layer (L2) of the test bed in {@code WEB-INF/conf/secdemo.config.xml}:
 * the engine's model access rights are evaluated for the acting persona when TL-Script runs with
 * security - the same way the agent's TL-Script tool runs it.
 *
 * <p>
 * The counterpart of {@link SecDemoToolAccessTest}, which covers the tool layer (L1). The two are
 * independent, and the fixture makes that visible in both directions: {@code secauthor1} holds
 * privileged tool grants and no data rights at all
 * ({@link #testAuthorHasNoDataRightsDespiteItsToolGrants()}), while {@code secviewer1} may read a note
 * here and may not call the tool that would list one.
 * </p>
 *
 * <p>
 * The fixture is an opt-in aspect that the shipped demo does not enable by default. The test
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
	 * No persona role may create a {@code Secret}.
	 */
	public void testEditorCannotCreateSecret() {
		actAs("seceditor1");

		assertDenied("new(`secdemo:Secret`)");
	}

	/**
	 * The writer persona has the same data rights as the editor: it may create a note.
	 *
	 * <p>
	 * What distinguishes the two is the <em>tool</em> layer - the writer holds {@code data.write} and
	 * {@code workspace} where the editor holds {@code script} and {@code agent.meta} - so giving them
	 * the same data rights keeps that difference attributable to the policy under test.
	 * </p>
	 */
	public void testWriterCreatesNote() {
		actAs("secwriter1");

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			assertNotNull("The writer must be allowed to create a note.", eval("new(`secdemo:Note`)"));
			tx.rollback();
		}
	}

	/**
	 * The viewer persona may read a note and may not create one.
	 *
	 * <p>
	 * Its interest is the contrast with the tool layer: {@code secviewer1} holds no {@code data.read}
	 * capability, so the agent cannot call {@code listInstances} for it - and yet the data rights that
	 * would let a permitted tool return a note are there. Two independent authorizations, and both
	 * have to allow.
	 * </p>
	 */
	public void testViewerReadsNotesButCannotCreateOne() {
		actAs("secviewer1");

		assertFalse("The viewer must see the note titles.",
			asCollection(eval("all(`secdemo:Note`).get(`secdemo:Note#title`)")).isEmpty());
		assertDenied("new(`secdemo:Note`)");
	}

	/**
	 * The agent author holds privileged tool grants and no data rights whatsoever.
	 *
	 * <p>
	 * Its role appears nowhere in the {@code <security-config>}, so deny-by-default applies: it may
	 * neither read nor create a note. That is not a misconfiguration of the fixture, it is the sharpest
	 * statement the fixture makes about the two layers being independent - a tool grant is not a data
	 * grant.
	 * </p>
	 */
	public void testAuthorHasNoDataRightsDespiteItsToolGrants() {
		actAs("secauthor1");

		assertTrue("The author holds no Read grant on Note, so attribute reads come back empty.",
			asCollection(eval("all(`secdemo:Note`).get(`secdemo:Note#title`)")).isEmpty());
		assertDenied("new(`secdemo:Note`)");
	}

	/**
	 * No persona reads a secret value, whatever its tool grants are.
	 */
	public void testNoPersonaReadsASecretValue() {
		for (String login : SecDemoSetupService.personaLogins()) {
			actAs(login);

			Object values = eval("all(`secdemo:Secret`).get(`secdemo:Secret#value`)");
			assertTrue(login + " must not see any secret value, but got: " + values,
				asCollection(values).isEmpty());
		}
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

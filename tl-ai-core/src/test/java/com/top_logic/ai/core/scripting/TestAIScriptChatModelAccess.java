/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.scripting;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedProvider;
import com.top_logic.ai.core.provider.ScriptedProviderOverride;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * That a TL-Script call of {@code aiChat} runs only on a model the acting account may use.
 *
 * <p>
 * The section {@code <model-access>} of the tool access policy decides which models an account may
 * choose. A call of {@code aiChat} has no agent, so the default model of the installation takes the
 * place of the model of the agent: it is permitted, and another model needs a grant.
 * </p>
 *
 * <p>
 * The policy is the shipped one, which names no role and therefore grants no account a model. The
 * models are served by a {@link ScriptedProvider}, so that no request leaves the test, and the script
 * is evaluated as TL-Script, as an account that is no administrator. A role that grants a model is
 * asserted on the security test bed of {@code tl-ai-demo}.
 * </p>
 */
public class TestAIScriptChatModelAccess extends BasicTestCase {

	/** The default model of the installation in this test. */
	private static final String DEFAULT_MODEL = "test-default-model";

	/** A second configured model, which the policy grants nobody. */
	private static final String OTHER_MODEL = "test-other-model";

	private static final String ANSWER = "The scripted answer.";

	/** An account with no role: the policy grants it no model. */
	private static Person clerk;

	private ScriptedProviderOverride _providers;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (clerk == null) {
			clerk = TestPerson.createPerson("ai-chat-model-access-clerk");
		}
		_providers = ScriptedProviderOverride.install(DEFAULT_MODEL, List.of(DEFAULT_MODEL, OTHER_MODEL));
		ScriptedProvider.install((request, turn) -> Reply.text(ANSWER));
	}

	@Override
	protected void tearDown() throws Exception {
		if (_providers != null) {
			_providers.restore();
			_providers = null;
		}
		super.tearDown();
	}

	/** Precondition: the services this test reasons about are the ones it configured. */
	public void testFixture() {
		assertTrue("Without the policy service nothing is checked.", AiToolAccessService.Module.INSTANCE.isActive());
		assertEquals(List.of(DEFAULT_MODEL, OTHER_MODEL),
			LLMProviderService.getInstance().getConfiguredModelNames());
		assertEquals(DEFAULT_MODEL, AgentRuntimeService.getInstance().defaultTarget().model());
		assertFalse("The account must not be an administrator, who is never restricted.", clerk.isAdmin());
	}

	/**
	 * A model the policy does not grant the account is not used: the call is refused with the
	 * sentence for a call without an agent, and no request reaches the model.
	 */
	public void testModelWithoutGrantIsRefused() {
		try {
			String answer = asClerk("aiChat(['Hello'], '" + OTHER_MODEL + "')");
			fail("aiChat ran on '" + OTHER_MODEL + "', which the policy does not grant this account, and"
				+ " answered '" + answer + "'. Requests that reached the provider: " + calledModels());
		} catch (TopLogicException expected) {
			assertEquals(I18NConstants.ERROR_MODEL_NOT_THE_DEFAULT__MODEL.fill(OTHER_MODEL),
				expected.getErrorKey());
		}
		assertEquals("No request may reach a model the account may not use.", List.of(), calledModels());
	}

	/** A call that names no model runs on the default model of the installation. */
	public void testDefaultModelIsPermitted() {
		assertEquals(ANSWER, asClerk("aiChat(['Hello'])"));
		assertEquals(List.of(DEFAULT_MODEL), calledModels());
	}

	/** Naming the default model is no choice of another model, and is permitted too. */
	public void testDefaultModelByNameIsPermitted() {
		assertEquals(ANSWER, asClerk("aiChat(['Hello'], '" + DEFAULT_MODEL + "')"));
		assertEquals(List.of(DEFAULT_MODEL), calledModels());
	}

	/** An administrator is never restricted by the policy. */
	public void testAdministratorMayChooseAnyModel() {
		Person root = PersonManager.getManager().getRoot();
		assertTrue(root.isAdmin());

		assertEquals(ANSWER, as(root, "aiChat(['Hello'], '" + OTHER_MODEL + "')"));
		assertEquals(List.of(OTHER_MODEL), calledModels());
	}

	/**
	 * A call without an account is refused before anything is built: a run that names no principal
	 * cannot be authorized. This is the case of a system context and of a scheduled script task.
	 */
	public void testACallWithoutAnAccountIsRefused() {
		try {
			Object answer = as(null, "aiChat(['Hello'])");
			fail("aiChat ran without an account and answered '" + answer + "'.");
		} catch (TopLogicException expected) {
			// The evaluation wraps the refusal of the security context, which is no TopLogicException.
			Throwable cause = expected.getCause();
			while (cause != null && !(cause instanceof IllegalStateException)) {
				cause = cause.getCause();
			}
			assertNotNull("The refusal of a run without a principal must be the cause: " + expected, cause);
		}
		assertEquals(List.of(), calledModels());
	}

	private static String asClerk(String script) {
		return as(clerk, script);
	}

	/** Evaluates the script as the given account; <code>null</code> evaluates it without one. */
	private static String as(Person account, String script) {
		return TLContextManager.inPersonContext(account, () -> (String) eval(script));
	}

	/**
	 * Evaluates the script as the plain "execute" of the script console does: with the interactive
	 * context, the security checks of the acting account, and no modification of the knowledge base.
	 */
	private static Object eval(String script) {
		QueryExecutor executor;
		try {
			executor = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
		} catch (ConfigurationException ex) {
			throw new AssertionError("The test script does not parse: " + script, ex);
		}
		return PersistencyLayer.getKnowledgeBase().withoutModifications(
			() -> executor.executeWith(executor.context(true, null, null), Args.none()));
	}

	/** The models of the requests that reached the scripted provider since {@link #setUp()}. */
	private static List<String> calledModels() {
		return ScriptedProvider.requests().stream().map(ProviderRequest::model).toList();
	}

	/**
	 * The policy service, the providers and the runtime with its default model, and the accounts.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestAIScriptChatModelAccess.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE));
	}
}

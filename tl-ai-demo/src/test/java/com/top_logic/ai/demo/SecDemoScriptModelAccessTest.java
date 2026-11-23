/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedProvider;
import com.top_logic.ai.core.provider.ScriptedProviderOverride;
import com.top_logic.ai.core.security.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Scenario M1 of the developer article {@code ai/security/test-bed}: {@code aiChat} in the script
 * console follows the {@code <model-access>} of the fixture.
 *
 * <p>
 * Ben holds {@code ticketReader}, which the fixture grants the Haiku model. A call without an agent
 * runs on the default model, which needs no grant, and any other model needs one. The models are
 * served by a {@link ScriptedProvider} under the names the fixture configures, so that no request
 * leaves the test.
 * </p>
 */
public class SecDemoScriptModelAccessTest extends SecDemoPersonaTest {

	/** The default model of the application in this test, as the demo has it with a key. */
	private static final String DEFAULT_MODEL = "claude-sonnet-5-5";

	/** The model the fixture grants {@code ticketReader}. */
	private static final String GRANTED_MODEL = "claude-haiku-5-5";

	/** A configured model that nothing grants {@code ticketReader}. */
	private static final String OTHER_MODEL = "claude-opus-5-5";

	private static final String ANSWER = "The scripted answer.";

	private ScriptedProviderOverride _providers;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_providers =
			ScriptedProviderOverride.install(DEFAULT_MODEL, List.of(DEFAULT_MODEL, OTHER_MODEL, GRANTED_MODEL));
		ScriptedProvider.install((request, turn) -> Reply.text(ANSWER));
	}

	@Override
	protected void tearDown() throws Exception {
		_providers.restore();
		_providers = null;
		super.tearDown();
	}

	/** A model his role does not grant is refused with the reason of the policy, and is not called. */
	public void testBenIsRefusedAnotherModel() {
		actAs(BEN);
		try {
			Object answer = eval("aiChat(['Hello'], '" + OTHER_MODEL + "')");
			fail("ben ran aiChat on '" + OTHER_MODEL + "', which ticketReader is not granted: " + answer);
		} catch (TopLogicException expected) {
			assertEquals(I18NConstants.MODEL_NOT_GRANTED__MODEL.fill(OTHER_MODEL), expected.getErrorKey());
		}
		assertEquals(List.of(), calledModels());
	}

	/** A call without a model name runs on the default model, which needs no grant. */
	public void testBenRunsOnTheDefaultModel() {
		actAs(BEN);
		assertEquals(ANSWER, eval("aiChat(['Hello'])"));
		assertEquals(List.of(DEFAULT_MODEL), calledModels());
	}

	/** The model his role grants is his to choose. */
	public void testBenRunsOnTheGrantedModel() {
		actAs(BEN);
		assertEquals(ANSWER, eval("aiChat(['Hello'], '" + GRANTED_MODEL + "')"));
		assertEquals(List.of(GRANTED_MODEL), calledModels());
	}

	/**
	 * Anna holds no role the section names, so the default of the section decides: no model but the
	 * default one, and the refusal says so for a call without an agent.
	 */
	public void testAnnaIsRefusedTheModelBenIsGranted() {
		actAs(ANNA);
		try {
			Object answer = eval("aiChat(['Hello'], '" + GRANTED_MODEL + "')");
			fail("anna ran aiChat on '" + GRANTED_MODEL + "', which only ticketReader is granted: " + answer);
		} catch (TopLogicException expected) {
			assertEquals(com.top_logic.ai.core.scripting.I18NConstants.ERROR_MODEL_NOT_THE_DEFAULT__MODEL
				.fill(GRANTED_MODEL), expected.getErrorKey());
		}
		assertEquals(List.of(), calledModels());
	}

	/** Evaluates the script as the script console does, with the security checks of the account. */
	private static Object eval(String script) {
		QueryExecutor executor;
		try {
			executor = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
		} catch (ConfigurationException ex) {
			throw new AssertionError("The test script does not parse: " + script, ex);
		}
		return executor.execute();
	}

	/** The models of the requests that reached the scripted provider since {@link #setUp()}. */
	private static List<String> calledModels() {
		return ScriptedProvider.requests().stream().map(ProviderRequest::model).toList();
	}

	/**
	 * Creates the test suite, with the agent runtime that resolves the default model.
	 */
	public static Test suite() {
		return suite(SecDemoScriptModelAccessTest.class, AgentRuntimeService.Module.INSTANCE);
	}
}

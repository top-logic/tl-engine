/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.I18NConstants;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.LLMProviderServiceFixture;
import com.top_logic.tl.ai.core.security.DefaultSecurityContextFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that {@link AgentRuntimeService} holds which agent runs how - read from the XML an application
 * writes - and checks every model it names against the running {@link LLMProviderService} (Gitea
 * #691).
 */
public class TestAgentRuntimeService extends TestCase {

	/** Instantiating a configured class resolves it through the {@code TypeIndex}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestAgentRuntimeService.class));
	}

	private static final String PROVIDERS = ServiceConfigFixture.file(LLMProviderService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
						<providers>
							<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
								api-key="unencrypted:test"
								catalog-provider-id=""
								models="app-model, other-model"
								name="anthropic"
							/>
						</providers>
					</instance>""");

	private static final String RUNTIME = ServiceConfigFixture.file(AgentRuntimeService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService"
						default-model="app-model"
						chat-entry-point="App_Agent"
						max-turns="12"
					>
						<agent-overrides>
							<agent name="App_Agent" model="other-model" max-turns="7"/>
						</agent-overrides>
						<capacity chat-workers="4" chat-queue="-1" concurrent-runs-per-account="3"/>
					</instance>""");

	/** The variable holding the key of the provider {@code first}, set by a test. */
	private static final String FIRST_KEY = "test_agent_runtime_first_key";

	/** The variable holding the key of the provider {@code second}, set by a test. */
	private static final String SECOND_KEY = "test_agent_runtime_second_key";

	/** Two providers whose keys are taken from variables, none of which is set initially. */
	private static final String ENV_PROVIDERS = ServiceConfigFixture.file(LLMProviderService.class.getName(), """
					<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
						<providers>
							<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
								catalog-provider-id=""
								models="first-model, first-other"
								name="first"
							>
								<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource"
									name="test_agent_runtime_first_key"/>
							</provider>
							<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig"
								catalog-provider-id=""
								models="second-model"
								name="second"
							>
								<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource"
									name="test_agent_runtime_second_key"/>
							</provider>
						</providers>
					</instance>""");

	private LLMProviderService _providers;

	private AgentRuntimeService _runtime;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.clearProperty(FIRST_KEY);
		System.clearProperty(SECOND_KEY);
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(FIRST_KEY);
		System.clearProperty(SECOND_KEY);
		if (_runtime != null) {
			AgentRuntimeFixture.stop(_runtime);
			_runtime = null;
		}
		if (_providers != null) {
			LLMProviderServiceFixture.stop(_providers);
			_providers = null;
		}
		super.tearDown();
	}

	/** Every setting of the section reaches the running service. */
	public void testTheConfigurationApplies() throws Exception {
		start(RUNTIME);

		assertEquals("App_Agent", _runtime.getChatEntryPoint());
		assertEquals("app-model", _runtime.getDefaultModel());
		AgentModelAssignment own = _runtime.resolveModelAssignment("App_Agent");
		assertEquals("other-model", own.model());
		assertEquals(Integer.valueOf(7), own.maxTurns());
		assertEquals("app-model", _runtime.resolveModel("Other_Agent"));
		assertEquals("The default turn limit is configured here.", 12, _runtime.getMaxTurns());
		assertEquals(4, _runtime.getCapacity().getChatWorkers());
		assertEquals(-1, _runtime.getCapacity().getChatQueue());
		assertEquals(3, _runtime.getCapacity().getConcurrentRunsPerAccount());
		assertSame(DefaultSecurityContextFactory.class, _runtime.getSecurityContextFactory().getClass());
	}

	/** Nothing configured: the shipped defaults, and an agent without a model runs on the first provider's. */
	public void testDefaults() throws Exception {
		start(ServiceConfigFixture.file(AgentRuntimeService.class.getName(),
			"<instance class=\"com.top_logic.tl.ai.core.agent.AgentRuntimeService\"/>"));

		assertEquals(AgentRuntimeService.DEFAULT_CHAT_ENTRY_POINT, _runtime.getChatEntryPoint());
		assertEquals(AgentParams.DEFAULT_MAX_TURNS, _runtime.getMaxTurns());
		assertNull(_runtime.getDefaultModel());
		assertNull(_runtime.getCapacity());
		AgentModelAssignment assignment = _runtime.resolveModelAssignment("App_Agent");
		assertEquals("Without a default model, a run uses the first model of the first available provider.",
			"app-model", assignment.model());
		assertEquals("anthropic", assignment.provider());
	}

	/**
	 * Without a default model, each run takes the first model of the first provider whose key is
	 * provided, in configuration order - decided anew for every run, so a key provided later serves
	 * the next run without a restart.
	 */
	public void testTheDefaultModelFollowsTheProvidedKeys() throws Exception {
		startOn(ENV_PROVIDERS, ServiceConfigFixture.file(AgentRuntimeService.class.getName(),
			"<instance class=\"com.top_logic.tl.ai.core.agent.AgentRuntimeService\"/>"));

		System.setProperty(SECOND_KEY, "sk-second");
		AgentModelAssignment second = _runtime.resolveModelAssignment("App_Agent");
		assertEquals("The first provider has no key, so the second one's first model is used.",
			"second-model", second.model());
		assertEquals("second", second.provider());

		System.setProperty(FIRST_KEY, "sk-first");
		AgentModelAssignment first = _runtime.resolveModelAssignment("App_Agent");
		assertEquals("first-model", first.model());
		assertEquals("first", first.provider());
	}

	/** No provider has its key: the run is refused with a message naming the secrets looked for. */
	public void testWithoutAnyKeyTheRunNamesTheSecrets() throws Exception {
		startOn(ENV_PROVIDERS, ServiceConfigFixture.file(AgentRuntimeService.class.getName(),
			"<instance class=\"com.top_logic.tl.ai.core.agent.AgentRuntimeService\"/>"));

		try {
			_runtime.resolveModelAssignment("App_Agent");
			fail("A run without any available provider must be refused.");
		} catch (TopLogicException ex) {
			ResKey error = ex.getErrorKey();
			assertEquals(I18NConstants.ERROR_NO_PROVIDER_AVAILABLE__SECRETS, error.plain());
			String secrets = String.valueOf(error.arguments()[0]);
			assertTrue(secrets, secrets.contains(FIRST_KEY));
			assertTrue(secrets, secrets.contains(SECOND_KEY));
		}
	}

	/** A model the providers do not have refuses the start, naming who named it. */
	public void testAModelTheProvidersDoNotHaveRefusesTheStart() throws Exception {
		try {
			start(RUNTIME.replace("model=\"other-model\"", "model=\"gone-model\""));
			fail("Expected the unknown model to refuse the start.");
		} catch (ConfigurationError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Agent 'App_Agent'"));
			assertTrue(ex.getMessage(), ex.getMessage().contains("gone-model"));
		}
		assertFalse(AgentRuntimeService.isRunning());
	}

	/** While the service is down, the accessors answer what a minimal setup always got. */
	public void testAccessorsWithoutTheService() {
		assertNull(AgentRuntimeService.chatEntryPoint());
		assertNull(AgentRuntimeService.agentOverride("App_Agent"));
		assertEquals(AgentParams.DEFAULT_MAX_TURNS, AgentRuntimeService.maxTurns());
	}

	private void start(String runtime) throws ConfigurationException {
		startOn(PROVIDERS, runtime);
	}

	private void startOn(String providers, String runtime) throws ConfigurationException {
		_providers = LLMProviderServiceFixture.start(context(),
			ServiceConfigFixture.read(LLMProviderService.class, providers));
		_runtime = AgentRuntimeFixture.start(context(),
			ServiceConfigFixture.read(AgentRuntimeService.class, runtime));
	}

	private static DefaultInstantiationContext context() {
		return new DefaultInstantiationContext(TestAgentRuntimeService.class);
	}
}

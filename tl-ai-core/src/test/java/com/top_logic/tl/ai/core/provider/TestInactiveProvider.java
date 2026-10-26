/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderException.Kind;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.health.FailoverConfig;
import com.top_logic.tl.ai.core.provider.health.ModelTarget;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a provider whose credentials are not available is inactive: it is never called, a
 * run on its model fails with a message naming the secret to provide and leaves no health mark,
 * a failover chain passes it over, and a key provided later activates it without building
 * anything anew.
 *
 * <p>
 * Every case starts a real {@link LLMProviderService} whose providers are scripted and whose keys
 * are taken from system properties through an {@link EnvironmentSecretSource}, and runs a real
 * {@link AgentLoop} against it, built the way an agent run builds it.
 * </p>
 */
public class TestInactiveProvider extends TestCase {

	private static final String PRIMARY = "primary-model";

	private static final String SECOND = "second-model";

	private static final String THIRD = "third-model";

	/** Prefix of the variables holding the keys; the provider name is appended. */
	private static final String KEY_PREFIX = "test_inactive_provider_key_";

	/** Keyed by provider name, so the configuration and the test see the same scripted provider. */
	private static final Map<String, ScriptedProvider> PROVIDERS = new LinkedHashMap<>();

	private LLMProviderService _service;

	/** The agent loop executes inside a TopLogic interaction, as in {@code TestModelFailover}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestInactiveProvider.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PROVIDERS.clear();
		clearKeys();
	}

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		PROVIDERS.clear();
		clearKeys();
		super.tearDown();
	}

	/** A run on a model whose provider has no key never reaches the provider and names the secret. */
	public void testAnInactiveProviderIsNeverCalled() {
		start(noFailover(), keyed(PRIMARY));

		AgentLoop loop = loop(PRIMARY);
		TopLogicException error = runRefused(loop);

		assertEquals(I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET, error.getErrorKey().plain());
		Object[] arguments = error.getErrorKey().arguments();
		assertEquals(PRIMARY, arguments[0]);
		assertEquals(PRIMARY, arguments[1]);
		assertEquals(ResKey.text(key(PRIMARY)), arguments[2]);
		assertEquals(List.of(), script(PRIMARY).calledModels());
		assertNull("A provider that was not called says nothing about its model.",
			_service.modelHealth().mark(PRIMARY));
	}

	/** The same loop runs as soon as the key is provided: availability is asked for every run. */
	public void testAKeyProvidedLaterActivatesTheProvider() {
		start(noFailover(), keyed(PRIMARY));
		AgentLoop loop = loop(PRIMARY);
		runRefused(loop);

		provideKey(PRIMARY);
		Recording run = run(loop);

		assertEquals(StopReason.COMPLETE, run.result().stopReason());
		assertEquals(List.of(PRIMARY), script(PRIMARY).calledModels());
	}

	/** A caller building a provider for immediate use is refused while the key is missing. */
	public void testBuildingAnInactiveProviderIsRefused() {
		start(noFailover(), keyed(PRIMARY));

		try {
			_service.buildProvider(PRIMARY);
			fail("An inactive provider must not be handed out for use.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET,
				ex.getErrorKey().plain());
		}
		assertNotNull(_service.unavailableReason(PRIMARY, null));

		provideKey(PRIMARY);
		assertNotNull(_service.buildProvider(PRIMARY));
		assertNull(_service.unavailableReason(PRIMARY, null));
	}

	/** A configured model whose provider is inactive hands the run to the next model of its chain. */
	public void testFailoverPassesAnInactivePrimaryOver() {
		start(failover(chain(PRIMARY, fallback(SECOND))), keyed(PRIMARY), keyed(SECOND));
		provideKey(SECOND);
		script(SECOND).answer("from the fallback");

		Recording run = run(loop(PRIMARY));

		assertEquals(StopReason.COMPLETE, run.result().stopReason());
		assertEquals(List.of(), script(PRIMARY).calledModels());
		assertEquals(List.of(SECOND), script(SECOND).calledModels());
		assertEquals(1, run.switches().size());
		assertContains(run.switches().get(0).reason(), "credentials");
		assertNull(_service.modelHealth().mark(PRIMARY));
	}

	/** An inactive fallback is passed over like a marked one, without being called or marked. */
	public void testFailoverPassesAnInactiveFallbackOver() {
		start(failover(chain(PRIMARY, fallback(SECOND), fallback(THIRD))),
			keyed(PRIMARY), keyed(SECOND), keyed(THIRD));
		provideKey(PRIMARY);
		provideKey(THIRD);
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(THIRD).answer("from the third");

		Recording run = run(loop(PRIMARY));

		assertEquals(StopReason.COMPLETE, run.result().stopReason());
		assertEquals(List.of(), script(SECOND).calledModels());
		assertEquals(List.of(THIRD), script(THIRD).calledModels());
		assertNull(_service.modelHealth().mark(SECOND));
	}

	/** A chain none of whose providers has a key fails with the message, not with a provider error. */
	public void testAChainWithoutAnyActiveProviderNamesTheSecret() {
		start(failover(chain(PRIMARY, fallback(SECOND))), keyed(PRIMARY), keyed(SECOND));

		TopLogicException error = runRefused(loop(PRIMARY));

		assertEquals(I18NConstants.ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET, error.getErrorKey().plain());
		assertEquals(PRIMARY, error.getErrorKey().arguments()[0]);
		assertNull(_service.modelHealth().mark(PRIMARY));
		assertNull(_service.modelHealth().mark(SECOND));
	}

	/** The model of a run assigned none is the first one of the first active provider. */
	public void testTheDefaultTargetIsTheFirstActiveProvider() {
		start(noFailover(), keyed(PRIMARY), keyed(SECOND));

		assertNull(_service.defaultTarget());
		ResKey none = _service.noProviderAvailable();
		assertEquals(I18NConstants.ERROR_NO_PROVIDER_AVAILABLE__SECRETS, none.plain());
		assertContains(String.valueOf(none.arguments()[0]), key(PRIMARY));
		assertContains(String.valueOf(none.arguments()[0]), key(SECOND));

		provideKey(SECOND);
		assertEquals(new ModelTarget(SECOND, SECOND), _service.defaultTarget());

		provideKey(PRIMARY);
		assertEquals("The configuration order decides, not the order in which keys were provided.",
			new ModelTarget(PRIMARY, PRIMARY), _service.defaultTarget());
	}

	/** Without any provider there is nothing to name. */
	public void testWithoutProvidersNothingIsConfigured() {
		start(noFailover());

		assertNull(_service.defaultTarget());
		assertEquals(I18NConstants.ERROR_NO_PROVIDER_CONFIGURED, _service.noProviderAvailable());
	}

	/**
	 * A shipped provider follows its key the same way, through the provider configuration and
	 * through the provider it builds - no request is sent.
	 */
	public void testAShippedProviderFollowsItsKey() {
		AnthropicProviderConfig.Config anthropic = TypedConfiguration.newConfigItem(AnthropicProviderConfig.Config.class);
		set(anthropic, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, AnthropicProviderConfig.class);
		anthropic.setName("anthropic");
		set(anthropic, LLMProviderConfig.Config.MODELS, List.of(PRIMARY));
		set(anthropic, LLMProviderConfig.Config.CATALOG_PROVIDER_ID, "");
		set(anthropic, AnthropicProviderConfig.Config.API_KEY, envSecret(key("anthropic")));
		start(noFailover(), anthropic);

		LLMProviderConfig config = _service.getProvider("anthropic");
		LLMProvider provider = _service.routeProvider(PRIMARY, null);
		assertFalse(config.isAvailable());
		assertFalse(provider.isAvailable());
		assertEquals(ResKey.text(key("anthropic")), config.credentialsReference());

		provideKey("anthropic");
		assertTrue(config.isAvailable());
		assertTrue("The provider built before the key was provided uses it.", provider.isAvailable());
	}

	// ---- Fixture ----

	/** What one run produced: its result and the switches it reported. */
	private record Recording(AgentResult result, List<AgentEvent.ModelSwitched> switches) {
		// A pair, nothing more.
	}

	private static TopLogicException runRefused(AgentLoop loop) {
		try {
			run(loop);
		} catch (TopLogicException ex) {
			return ex;
		}
		fail("A run without an active provider must be refused.");
		return null;
	}

	private static Recording run(AgentLoop loop) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("do something"))));
		List<AgentEvent.ModelSwitched> switches = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.ModelSwitched e) {
				switches.add(e);
			}
		};
		AgentResult result = loop.run(conversation, ToolContext.unrestricted(), listener);
		return new Recording(result, switches);
	}

	/** A loop built the way an agent run builds it: routed, but not checked for credentials. */
	private AgentLoop loop(String model) {
		// No retries: what is under test starts where those retries stop.
		AgentParams config = new AgentParams(model, "You are a test assistant.", 4,
			1024, Map.of(), 200_000, 0, 30, null, msg -> true);
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return new AgentLoop(config, _service.routeProvider(model, null), catalog);
	}

	private static String key(String providerName) {
		return KEY_PREFIX + providerName;
	}

	private static void provideKey(String providerName) {
		System.setProperty(key(providerName), "sk-" + providerName);
	}

	private static void clearKeys() {
		for (String name : List.of(PRIMARY, SECOND, THIRD, "anthropic")) {
			System.clearProperty(key(name));
		}
	}

	private static ScriptedProvider script(String providerName) {
		return PROVIDERS.computeIfAbsent(providerName, name -> new ScriptedProvider());
	}

	private void start(FailoverConfig failover, LLMProviderConfig.Config... providers) {
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);
		for (LLMProviderConfig.Config provider : providers) {
			config.getProviders().put(provider.getName(), provider);
		}
		set(config, LLMProviderService.Config.FAILOVER, failover);
		// No model catalog: these cases are about routing.
		set(config, LLMProviderService.Config.MODEL_CATALOG, null);

		LLMProviderService service =
			new LLMProviderService(new DefaultInstantiationContext(TestInactiveProvider.class), config);
		service.startUp();
		_service = service;
	}

	private static FailoverConfig noFailover() {
		return TypedConfiguration.newConfigItem(FailoverConfig.class);
	}

	private static FailoverConfig failover(FailoverConfig.Chain... chains) {
		FailoverConfig result = noFailover();
		for (FailoverConfig.Chain chain : chains) {
			result.getChains().put(chain.getName(), chain);
		}
		return result;
	}

	private static FailoverConfig.Chain chain(String model, FailoverConfig.Target... fallbacks) {
		FailoverConfig.Chain result = TypedConfiguration.newConfigItem(FailoverConfig.Chain.class);
		result.setName(model);
		result.getFallbacks().addAll(List.of(fallbacks));
		return result;
	}

	private static FailoverConfig.Target fallback(String model) {
		FailoverConfig.Target result = TypedConfiguration.newConfigItem(FailoverConfig.Target.class);
		set(result, FailoverConfig.Target.MODEL, model);
		return result;
	}

	private static EnvironmentSecretSource.Config envSecret(String variable) {
		EnvironmentSecretSource.Config result = TypedConfiguration.newConfigItem(EnvironmentSecretSource.Config.class);
		result.setName(variable);
		return result;
	}

	/**
	 * A provider entry whose instance is the test's scripted one, with its key taken from the
	 * variable {@link #key(String)} of its name. Named after the model it serves.
	 */
	private static LLMProviderConfig.Config keyed(String model) {
		ScriptedProviderConfig.Config result =
			TypedConfiguration.newConfigItem(ScriptedProviderConfig.Config.class);
		set(result, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, ScriptedProviderConfig.class);
		result.setName(model);
		set(result, LLMProviderConfig.Config.MODELS, List.of(model));
		set(result, LLMProviderConfig.Config.CATALOG_PROVIDER_ID, "scripted");
		result.setCredentials(HeaderCredentials.config("x-api-key", null, envSecret(key(model))));
		script(model);
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	private static void assertContains(String message, String expected) {
		assertTrue("Expected '" + expected + "' in: " + message, message != null && message.contains(expected));
	}

	/** A provider configuration that hands out the test's scripted provider with its credentials. */
	public static final class ScriptedProviderConfig implements LLMProviderConfig {

		/** Configuration of a {@link ScriptedProviderConfig}. */
		public interface Config extends LLMProviderConfig.Config {
			// Nothing beyond the shared properties.
		}

		private final Config _config;

		private final CredentialProvider _credentials;

		/** Creates a {@link ScriptedProviderConfig}. */
		@CalledByReflection
		public ScriptedProviderConfig(InstantiationContext context, Config config) {
			_config = config;
			_credentials = LLMProviderConfig.createCredentials(context, config);
		}

		@Override
		public CredentialProvider getCredentials() {
			return _credentials;
		}

		@Override
		public LLMProvider createProvider() {
			ScriptedProvider result = script(_config.getName());
			result.setCredentials(_credentials);
			return result;
		}

		@Override
		public List<String> discoverModels() {
			return getConfiguredModels();
		}

		@Override
		public List<String> getConfiguredModels() {
			List<String> models = _config.getModels();
			return models != null ? models : List.of();
		}

		@Override
		public String getCatalogProviderId() {
			return _config.getCatalogProviderId();
		}
	}

	/**
	 * A provider that answers or fails on command, records which model it was asked for, and is
	 * available while its credentials are.
	 */
	private static final class ScriptedProvider implements LLMProvider {

		private final List<String> _calledModels = new ArrayList<>();

		private CredentialProvider _credentials;

		private Kind _failWith;

		private String _failMessage;

		private String _answer = "ok";

		void setCredentials(CredentialProvider credentials) {
			_credentials = credentials;
		}

		void fail(Kind kind, String message) {
			_failWith = kind;
			_failMessage = message;
		}

		void answer(String text) {
			_failWith = null;
			_answer = text;
		}

		List<String> calledModels() {
			return List.copyOf(_calledModels);
		}

		@Override
		public boolean isAvailable() {
			return _credentials == null || _credentials.isAvailable();
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
			_calledModels.add(request.model());
			if (_failWith != null) {
				throw new ProviderException(_failWith, 0, _failMessage);
			}
			List<ContentPart> content = List.of(new TextContent(_answer));
			handler.onEvent(new StreamEvent.TextDelta(0, _answer));
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg", request.model(), "end_turn", TokenUsage.EMPTY, content));
		}
	}
}

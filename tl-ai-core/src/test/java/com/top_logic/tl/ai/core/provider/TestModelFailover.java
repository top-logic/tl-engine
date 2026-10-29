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
import com.top_logic.tl.ai.core.provider.health.FailoverConfig;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * Tests that a run which loses its model continues on the next healthy one of its chain, and that a
 * run with no chain fails exactly as it did before (Gitea #344).
 *
 * <p>
 * Every case starts a real {@link LLMProviderService} on a hand-built configuration whose providers are
 * scripted - each one answers or fails on command - and then runs a real {@link AgentLoop} against
 * it. The routing decision sits between the two, so neither can be faked out of the test.
 * </p>
 */
public class TestModelFailover extends TestCase {

	private static final String PRIMARY = "primary-model";

	private static final String SECOND = "second-model";

	private static final String THIRD = "third-model";

	/** Keyed by provider name, so the configuration and the test see the same scripted provider. */
	private static final Map<String, ScriptedProvider> PROVIDERS = new LinkedHashMap<>();

	private LLMProviderService _service;

	/** The agent loop executes inside a TopLogic interaction, as in {@code TestAgentLoop}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestModelFailover.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PROVIDERS.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		PROVIDERS.clear();
		super.tearDown();
	}

	/**
	 * The default, and the one every installation is in until it writes a {@code <failover>}
	 * section: the run lives and dies with its model.
	 */
	public void testWithoutAChainNothingChanges() {
		start(noFailover(), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).answer("from the fallback");

		Recording run = run(PRIMARY);

		assertEquals(StopReason.ERROR, run.result().stopReason());
		assertEquals("OVERLOADED", run.result().errorKind());
		assertEquals(List.of(PRIMARY), script(PRIMARY).calledModels());
		assertEquals("Nothing may reach a model the run was not configured with.",
			List.of(), script(SECOND).calledModels());
		assertEquals(List.of(), run.switches());
	}

	/** The point of the ticket: an overloaded model hands the run to the next one of its chain. */
	public void testCapacityFailureMovesTheRunOn() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).answer("from the fallback");

		Recording run = run(PRIMARY);

		assertEquals(StopReason.COMPLETE, run.result().stopReason());
		assertEquals("The answer must come from the model the run moved to.",
			List.of(SECOND), script(SECOND).calledModels());
		assertEquals(1, run.switches().size());

		AgentEvent.ModelSwitched event = run.switches().get(0);
		assertEquals(PRIMARY, event.fromModel());
		assertEquals(SECOND, event.toModel());
		assertContains(event.reason(), "OVERLOADED");
		assertContains(event.reason(), "upstream 529");
	}

	/** The request the fallback receives is built for the fallback, not for the model that failed. */
	public void testTheFallbackIsAskedForItsOwnModel() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.RATE_LIMITED, "slow down");
		script(SECOND).answer("ok");

		run(PRIMARY);

		assertEquals(List.of(SECOND), script(SECOND).calledModels());
	}

	/**
	 * What the run publishes about itself follows the switch, as the plain identifiers a tool and
	 * the execution record read - not as the label a human reads.
	 */
	public void testTheRunPublishesTheModelItMovedTo() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).answer("ok");

		ToolContext context = ToolContext.unrestricted();
		run(loop(PRIMARY), context);

		RunInfo info = context.executionHandle().runInfo();
		assertEquals(SECOND, info.model());
		assertEquals("scripted", info.provider());
	}

	/** Once a run has moved, it stays moved: the rest of its turns are the fallback's. */
	public void testTheSwitchLastsForTheRestOfTheRun() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).answer("ok");

		AgentLoop loop = loop(PRIMARY);
		Recording first = run(loop);
		Recording second = run(loop);

		assertEquals(StopReason.COMPLETE, first.result().stopReason());
		assertEquals(StopReason.COMPLETE, second.result().stopReason());
		assertEquals("The primary is called once - by the first run. The second is routed past it"
			+ " by the mark the first one left, without paying for another failed call.",
			List.of(PRIMARY), script(PRIMARY).calledModels());
		assertEquals(List.of(SECOND, SECOND), script(SECOND).calledModels());
		assertEquals("The second run reports its own switch, with the mark as the reason.",
			1, second.switches().size());
		assertContains(second.switches().get(0).reason(), "capacity");
	}

	/** Every model the run passed over is named, with the reason it was passed over for. */
	public void testSkippedModelsAreRecordedWithTheirReasons() {
		start(failover(chain(PRIMARY, fallback(SECOND), fallback(THIRD))),
			scripted(PRIMARY), scripted(SECOND), scripted(THIRD));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).fail(Kind.BILLING, "no credit");
		script(THIRD).answer("ok");

		Recording run = run(PRIMARY);

		assertEquals(StopReason.COMPLETE, run.result().stopReason());
		assertEquals(2, run.switches().size());
		assertContains(run.switches().get(0).reason(), "OVERLOADED");
		assertEquals(SECOND, run.switches().get(0).toModel());
		assertContains(run.switches().get(1).reason(), "BILLING");
		assertEquals(THIRD, run.switches().get(1).toModel());
	}

	/** A chain with nothing healthy left fails the run, naming every model it went through. */
	public void testAnExhaustedChainNamesEveryModel() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.OVERLOADED, "upstream 529");
		script(SECOND).fail(Kind.BILLING, "no credit");

		Recording run = run(PRIMARY);

		assertEquals(StopReason.ERROR, run.result().stopReason());
		assertEquals("The kind of the last model that was actually called.",
			"BILLING", run.result().errorKind());
		assertContains(run.result().errorMessage(), PRIMARY);
		assertContains(run.result().errorMessage(), SECOND);
		assertContains(run.result().errorMessage(), "OVERLOADED");
	}

	/**
	 * A fault that travels with the request is not a statement about the model: failing over would
	 * only repeat it on the next one and hide where it came from.
	 */
	public void testARequestFaultDoesNotFailOver() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.INVALID_REQUEST, "unknown field 'tools'");
		script(SECOND).answer("ok");

		Recording run = run(PRIMARY);

		assertEquals(StopReason.ERROR, run.result().stopReason());
		assertEquals("INVALID_REQUEST", run.result().errorKind());
		assertEquals(List.of(), script(SECOND).calledModels());
		assertEquals(List.of(), run.switches());
	}

	/** A sticky mark outlives the run that earned it: the next one does not call that model at all. */
	public void testAQuotaMarkRoutesTheNextRunStraightPast() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.AUTHENTICATION, "invalid api key");
		script(SECOND).answer("ok");

		run(PRIMARY);
		Recording second = run(PRIMARY);

		assertEquals(StopReason.COMPLETE, second.result().stopReason());
		assertEquals("Called once, by the first run.", List.of(PRIMARY), script(PRIMARY).calledModels());
		assertContains(second.switches().get(0).reason(), "quota");
	}

	/** And the administrator's reset puts it back. */
	public void testResettingTheMarkPutsTheModelBack() {
		start(failover(chain(PRIMARY, fallback(SECOND))), scripted(PRIMARY), scripted(SECOND));
		script(PRIMARY).fail(Kind.AUTHENTICATION, "invalid api key");
		script(SECOND).answer("ok");
		run(PRIMARY);

		assertTrue(_service.modelHealth().reset(PRIMARY));
		script(PRIMARY).answer("fixed");
		Recording second = run(PRIMARY);

		assertEquals(StopReason.COMPLETE, second.result().stopReason());
		assertEquals(List.of(PRIMARY, PRIMARY), script(PRIMARY).calledModels());
		assertEquals(List.of(), second.switches());
	}

	// ---- Fixture ----

	/** What one run produced: its result and the switches it reported. */
	private record Recording(AgentResult result, List<AgentEvent.ModelSwitched> switches) {
		// A pair, nothing more.
	}

	private Recording run(String model) {
		return run(loop(model));
	}

	private static Recording run(AgentLoop loop) {
		return run(loop, ToolContext.unrestricted());
	}

	private static Recording run(AgentLoop loop, ToolContext context) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("do something"))));
		List<AgentEvent.ModelSwitched> switches = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.ModelSwitched e) {
				switches.add(e);
			}
		};
		AgentResult result = loop.run(conversation, context, listener);
		return new Recording(result, switches);
	}

	private AgentLoop loop(String model) {
		// No retries: the loop's own backoff for a transient error would make every case here wait
		// out a real sleep, and what is under test starts where those retries stop.
		AgentParams config = new AgentParams(model, "You are a test assistant.", 4,
			1024, Map.of(), 200_000, 0, 30, null, msg -> true);
		return new AgentLoop(config, _service.buildProvider(model), emptyRegistry());
	}

	private static ToolCatalog emptyRegistry() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return catalog;
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
		// No model catalog: these cases are about routing, and a catalog would send the start
		// looking for model metadata none of these invented models has.
		set(config, LLMProviderService.Config.MODEL_CATALOG, null);

		LLMProviderService service =
			new LLMProviderService(new DefaultInstantiationContext(TestModelFailover.class), config);
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

	/**
	 * A provider entry whose instance is the test's scripted one. Named after the model it serves,
	 * so that a chain naming models is a chain naming these entries.
	 */
	private static LLMProviderConfig.Config scripted(String model) {
		ScriptedProviderConfig.Config result =
			TypedConfiguration.newConfigItem(ScriptedProviderConfig.Config.class);
		set(result, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, ScriptedProviderConfig.class);
		result.setName(model);
		set(result, LLMProviderConfig.Config.MODELS, List.of(model));
		set(result, LLMProviderConfig.Config.CATALOG_PROVIDER_ID, "scripted");
		script(model);
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	private static void assertContains(String message, String expected) {
		assertTrue("Expected '" + expected + "' in: " + message, message != null && message.contains(expected));
	}

	/** A provider configuration that hands out the test's scripted provider. */
	public static final class ScriptedProviderConfig implements LLMProviderConfig {

		/** Configuration of a {@link ScriptedProviderConfig}. */
		public interface Config extends LLMProviderConfig.Config {
			// Nothing beyond the shared properties.
		}

		private final Config _config;

		/** Creates a {@link ScriptedProviderConfig}. */
		@CalledByReflection
		public ScriptedProviderConfig(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public LLMProvider createProvider() {
			return script(_config.getName());
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

	/** A provider that answers or fails on command, and records which model it was asked for. */
	private static final class ScriptedProvider implements LLMProvider {

		private final List<String> _calledModels = new ArrayList<>();

		private Kind _failWith;

		private String _failMessage;

		private String _answer = "ok";

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

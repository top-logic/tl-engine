/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.tl.ai.core.provider.LLMService.AgentExecutionConfig;
import com.top_logic.tl.ai.core.provider.LLMService.AgentOverride;
import com.top_logic.tl.ai.core.provider.LLMService.ModelCatalogConfig;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;

/**
 * Tests that a model exposed by two providers stays routable (issue #105).
 *
 * <p>
 * Every case here starts the real {@link LLMService} on a hand-built configuration and asks it the
 * two questions the {@code AgentFactory} asks -
 * {@link LLMService#resolveModelAssignment(String)} followed by
 * {@link LLMService#buildProvider(String, String)} - because the defect this fixes was invisible in
 * the resolution result and showed only in which provider instance came back. The two providers
 * therefore differ in their endpoint, which is the one thing an
 * {@link AbstractStreamingProvider} makes visible.
 * </p>
 */
public class TestProviderRouting extends TestCase {

	private static final String MODEL = "claude-sonnet-4-20250514";

	private static final String OTHER_MODEL = "claude-haiku-4-5-20251001";

	private static final String A1_URL = "https://a1.example.com";

	private static final String A2_URL = "https://a2.example.com";

	/**
	 * A catalog URL no {@code HttpClient} will follow, so that a start that does want a catalog
	 * falls back to the bundled snapshot instead of reaching models.dev.
	 */
	private static final String NO_CATALOG_URL = "urn:test:no-catalog";

	private LLMService _service;

	/**
	 * Instantiating a provider configuration resolves its implementation class through the
	 * {@code TypeIndex}, which needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestProviderRouting.class));
	}

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		super.tearDown();
	}

	/**
	 * The defect itself: the second provider exposing the model is no longer dropped.
	 */
	public void testBothProvidersSurviveStartUp() {
		start(MODEL, "a1", overrides(), provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertEquals(List.of("a1", "a2"), _service.getProvidersOf(MODEL));
	}

	/**
	 * An agent's provider decides which of the two instances serves it - the point of the ticket.
	 */
	public void testAgentOverrideSelectsTheProviderInstance() {
		start(MODEL, "a1", overrides(override("analyst", MODEL, "a2"), override("writer", MODEL, "a1")),
			provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertEquals("a2", _service.resolveModelAssignment("analyst").provider());
		assertEquals(A2_URL, endpointFor("analyst"));
		assertEquals(A1_URL, endpointFor("writer"));
	}

	/**
	 * An agent that names no model of its own runs the default model through the default provider.
	 */
	public void testDefaultProviderServesInheritingAgents() {
		start(MODEL, "a2", overrides(override("analyst", null, null)),
			provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertEquals(A2_URL, endpointFor("analyst"));
		assertEquals("An agent without an override inherits the default pair as well.",
			A2_URL, endpointFor("unlisted"));
	}

	/**
	 * The overwhelmingly common case: one provider, and no provider named anywhere.
	 */
	public void testSingleProviderNeedsNoQualification() {
		start(MODEL, null, overrides(override("analyst", MODEL, null)), provider("a1", A1_URL, MODEL));

		assertEquals(List.of("a1"), _service.getProvidersOf(MODEL));
		assertNull(_service.getDefaultProvider());
		assertNull(_service.resolveModelAssignment("analyst").provider());
		assertEquals(MODEL, _service.resolveModel("analyst"));
		assertEquals(A1_URL, endpointFor("analyst"));
		assertEquals(A1_URL, endpointFor("unlisted"));
		assertEquals("anthropic", _service.getProviderId(MODEL));
	}

	/**
	 * A blank provider attribute means "unqualified", not "a provider named nothing".
	 */
	public void testBlankProviderIsTreatedAsUnqualified() {
		start(MODEL, "", overrides(override("analyst", MODEL, "  ")), provider("a1", A1_URL, MODEL));

		assertNull(_service.getDefaultProvider());
		assertNull(_service.resolveModelAssignment("analyst").provider());
		assertEquals(A1_URL, endpointFor("analyst"));
	}

	/**
	 * An unqualified reference to a model two providers expose refuses the start: there is no
	 * answer to give, and answering by configuration order is the defect this fixes.
	 */
	public void testAmbiguousDefaultModelRefusesStartUp() {
		String message = refuseStart(MODEL, null, overrides(override("analyst", MODEL, "a1")),
			provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertContains(message, "The default model");
		assertContains(message, MODEL);
		assertContains(message, "a1, a2");
		assertFalse("The agent that does name a provider is not a problem: " + message,
			message.contains("analyst"));
	}

	/** The same for an agent override rather than for the default model. */
	public void testAmbiguousAgentOverrideRefusesStartUp() {
		String message = refuseStart(MODEL, "a1", overrides(override("analyst", MODEL, null)),
			provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertContains(message, "analyst");
		assertContains(message, MODEL);
		assertContains(message, "a1, a2");
	}

	/** A provider name nobody configured refuses the start, naming agent, model and provider. */
	public void testUnknownProviderRefusesStartUp() {
		String message = refuseStart(MODEL, null, overrides(override("analyst", MODEL, "a3")),
			provider("a1", A1_URL, MODEL));

		assertContains(message, "Agent 'analyst'");
		assertContains(message, MODEL);
		assertContains(message, "a3");
		assertContains(message, "not configured");
	}

	/** A provider that exists but does not expose the model refuses the start as well. */
	public void testProviderWithoutTheModelRefusesStartUp() {
		String message = refuseStart(MODEL, null, overrides(override("analyst", MODEL, "a2")),
			provider("a1", A1_URL, MODEL), provider("a2", A2_URL, OTHER_MODEL));

		assertContains(message, "Agent 'analyst'");
		assertContains(message, "does not expose");
		assertContains(message, OTHER_MODEL);
	}

	/**
	 * Two instances of one vendor still answer the catalog question: they agree on their catalog
	 * provider ID even where the instance is ambiguous, so model metadata and the
	 * provider-specific fields of the editor keep working.
	 */
	public void testCatalogProviderIdSurvivesTwoInstancesOfOneVendor() {
		start(MODEL, "a1", overrides(), provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		assertEquals("anthropic", _service.getProviderId(MODEL));
		assertEquals("anthropic", _service.getProviderId(MODEL, "a2"));
		assertNull("A provider that does not expose the model has no ID to give for it.",
			_service.getProviderId(MODEL, "nobody"));
	}

	/** An unqualified lookup at runtime says what is ambiguous rather than guessing. */
	public void testUnqualifiedRuntimeLookupOfAnAmbiguousModelFails() {
		start(MODEL, "a1", overrides(), provider("a1", A1_URL, MODEL), provider("a2", A2_URL, MODEL));

		try {
			_service.buildProvider(MODEL);
			fail("Expected the ambiguity to be reported.");
		} catch (IllegalStateException ex) {
			assertContains(ex.getMessage(), "a1, a2");
		}
	}

	/**
	 * An agent with neither an override nor a default model is named in the failure.
	 */
	public void testAgentWithoutAnyModelIsReportedByName() {
		start(null, null, overrides(), provider("a1", A1_URL, MODEL));

		try {
			_service.resolveModelAssignment("analyst");
			fail("Expected the missing model to be reported.");
		} catch (IllegalStateException ex) {
			assertContains(ex.getMessage(), "analyst");
		}
	}

	/** A model no provider exposes stays a runtime error, not a boot failure. */
	public void testUnknownModelIsARuntimeError() {
		start(MODEL, null, overrides(), provider("a1", A1_URL, MODEL));

		try {
			_service.buildProvider("no-such-model");
			fail("Expected the unknown model to be reported.");
		} catch (IllegalStateException ex) {
			assertContains(ex.getMessage(), "no-such-model");
		}
	}

	/**
	 * The model metadata of a model two providers expose no longer depends on which of them the
	 * index happened to keep.
	 *
	 * <p>
	 * The gateway is listed first and has no remote catalog of its own, so under the dropped
	 * mapping the model was requested under no catalog provider at all and the editor showed a
	 * model without limits or capabilities.
	 * </p>
	 */
	public void testCatalogMetadataReachesAModelTwoProvidersExpose() {
		LLMService.Config<?> config =
			serviceConfig(MODEL, "gateway", overrides(), gateway("gateway", A1_URL, MODEL),
				provider("anthropic", A2_URL, MODEL));
		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		set(catalog, ModelCatalogConfig.CATALOG_URL, NO_CATALOG_URL);
		set(config, LLMService.Config.MODEL_CATALOG, catalog);
		start(config);

		assertNotNull("The model of the second provider must still reach the catalog.",
			_service.getModel(MODEL));
	}

	/**
	 * The provider-specific fields of the editor follow the provider the override names.
	 *
	 * <p>
	 * The Anthropic-only fields must stay visible for the Anthropic entry and disappear for the
	 * gateway that has no catalog provider of its own - which is only possible now that the mode
	 * function is told the provider along with the model.
	 * </p>
	 */
	public void testProviderSpecificFieldsFollowTheNamedProvider() {
		start(MODEL, "anthropic", overrides(), gateway("gateway", A1_URL, MODEL),
			provider("anthropic", A2_URL, MODEL));

		ProviderFieldMode.AnthropicFieldMode mode = new ProviderFieldMode.AnthropicFieldMode();
		assertEquals(FieldMode.ACTIVE, mode.invoke(MODEL, "anthropic"));
		assertEquals(FieldMode.INVISIBLE, mode.invoke(MODEL, "gateway"));
		assertEquals("Providers that disagree on the vendor leave the question unanswered.",
			FieldMode.INVISIBLE, mode.invoke(MODEL, null));
	}

	/**
	 * Every dynamic-mode annotation of an agent override passes as many arguments as its function
	 * takes, and every argument names a property that exists.
	 *
	 * <p>
	 * The wiring the editor resolves only when the administration page opens, checked here where a
	 * mismatch is a test failure rather than a page that cannot be displayed.
	 * </p>
	 */
	public void testDynamicModeArgumentsMatchTheirFunctions() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(AgentOverride.class);
		int checked = 0;
		for (PropertyDescriptor property : descriptor.getProperties()) {
			DynamicMode annotation = property.getAnnotation(DynamicMode.class);
			if (annotation == null) {
				continue;
			}
			checked++;
			GenericFunction<?> function = annotation.fun().getDeclaredConstructor().newInstance();
			assertEquals(property.getPropertyName() + " passes the wrong number of arguments.",
				annotation.args().length, function.getArgumentCount());
			for (Ref ref : annotation.args()) {
				for (String step : ref.value()) {
					assertNotNull(property.getPropertyName() + " refers to the unknown property '" + step + "'.",
						descriptor.getProperty(step));
				}
			}
		}
		assertEquals("All six provider-specific fields must be covered.", 6, checked);
	}

	// ---- Fixture ----

	/**
	 * The endpoint of the provider instance the given agent's assignment routes to - the two lines
	 * {@code AgentFactory} runs before every agent loop.
	 */
	private String endpointFor(String agentName) {
		AgentModelAssignment assignment = _service.resolveModelAssignment(agentName);
		LLMProvider provider = _service.buildProvider(assignment.model(), assignment.provider());
		return ((AbstractStreamingProvider) provider)._baseUrl;
	}

	/**
	 * Starts the service without a model catalog: routing is what these cases are about, and a
	 * catalog would send the start looking for model metadata.
	 */
	private void start(String defaultModel, String defaultProvider, List<AgentOverride> overrides,
			LLMProviderConfig.Config... providers) {
		start(serviceConfig(defaultModel, defaultProvider, overrides, providers));
	}

	private void start(LLMService.Config<?> config) {
		LLMService service = new LLMService(context(), config);
		service.startUp();
		_service = service;
	}

	private String refuseStart(String defaultModel, String defaultProvider, List<AgentOverride> overrides,
			LLMProviderConfig.Config... providers) {
		try {
			start(defaultModel, defaultProvider, overrides, providers);
			fail("Expected the unroutable configuration to refuse the start.");
			return null;
		} catch (ConfigurationError ex) {
			return ex.getMessage();
		}
	}

	private static LLMService.Config<?> serviceConfig(String defaultModel, String defaultProvider,
			List<AgentOverride> overrides, LLMProviderConfig.Config... providers) {
		@SuppressWarnings("unchecked")
		LLMService.Config<LLMService> result =
			(LLMService.Config<LLMService>) TypedConfiguration.newConfigItem(LLMService.Config.class);
		for (LLMProviderConfig.Config provider : providers) {
			result.getProviders().put(provider.getName(), provider);
		}
		AgentExecutionConfig execution = TypedConfiguration.newConfigItem(AgentExecutionConfig.class);
		set(result, LLMService.Config.AGENT_EXECUTION, execution);
		set(execution, AgentExecutionConfig.DEFAULT_MODEL, defaultModel);
		set(execution, AgentExecutionConfig.DEFAULT_PROVIDER, defaultProvider);
		for (AgentOverride override : overrides) {
			execution.getAgentOverrides().put(override.getName(), override);
		}
		return result;
	}

	private static List<AgentOverride> overrides(AgentOverride... entries) {
		return List.of(entries);
	}

	private static AgentOverride override(String agentName, String model, String provider) {
		AgentOverride result = TypedConfiguration.newConfigItem(AgentOverride.class);
		result.setName(agentName);
		set(result, AgentOverride.MODEL, model);
		set(result, AgentOverride.PROVIDER, provider);
		return result;
	}

	/**
	 * A provider entry with its own endpoint, so that the instance a lookup returns is
	 * recognizable.
	 */
	private static LLMProviderConfig.Config provider(String name, String baseUrl, String... models) {
		AnthropicProviderConfig.Config result =
			TypedConfiguration.newConfigItem(AnthropicProviderConfig.Config.class);
		set(result, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, AnthropicProviderConfig.class);
		result.setName(name);
		set(result, LLMProviderConfig.Config.BASE_URL, baseUrl);
		set(result, LLMProviderConfig.Config.MODELS, List.of(models));
		set(result, AnthropicProviderConfig.Config.API_KEY, "unencrypted:test");
		return result;
	}

	/**
	 * A provider entry with no remote catalog of its own - an endpoint models.dev does not list.
	 */
	private static LLMProviderConfig.Config gateway(String name, String baseUrl, String... models) {
		LLMProviderConfig.Config result = provider(name, baseUrl, models);
		set(result, LLMProviderConfig.Config.CATALOG_PROVIDER_ID, "");
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	private static DefaultInstantiationContext context() {
		return new DefaultInstantiationContext(TestProviderRouting.class);
	}

	private static void assertContains(String message, String expected) {
		assertTrue("Expected '" + expected + "' in: " + message, message.contains(expected));
	}
}

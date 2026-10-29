/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.catalog.CatalogStatus;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;
import com.top_logic.tl.ai.core.provider.catalog.ResolvedModel;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests that the remote model catalog never holds the start of {@link LLMProviderService} (issue
 * #898), and that the catalog the service resolves is service state - with the origin of every
 * entry and the state of the remote load - rather than part of its configuration (ticket #29954).
 */
public class TestModelCatalogLoad extends TestCase {

	/** A model the bundled catalog knows. */
	public static final String MODEL = "claude-sonnet-4-20250514";

	/** The context window the test catalog gives {@link #MODEL}, which the bundled one does not. */
	private static final int REMOTE_CONTEXT = 4242;

	/** A configured model that neither catalog lists. */
	public static final String LOCAL_MODEL = "local-test-model";

	/** The context window the model overrides of a test give a model. */
	public static final int OVERRIDE_CONTEXT = 777;

	/** The path of the test server that answers every request with a server error. */
	private static final String BROKEN_PATH = "/broken.json";

	private HttpServer _server;

	private ExecutorService _serverThreads;

	private final AtomicInteger _requests = new AtomicInteger();

	/** Counted down when the client of the slow catalog has gone away. */
	private final CountDownLatch _slowClientGone = new CountDownLatch(1);

	private LLMProviderService _service;

	/**
	 * Instantiating a provider configuration needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestModelCatalogLoad.class));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/catalog.json", this::answerCatalog);
		_server.createContext("/slow.json", this::answerSlowly);
		_server.createContext(BROKEN_PATH, this::answerError);
		_serverThreads = Executors.newCachedThreadPool();
		_server.setExecutor(_serverThreads);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		if (_service != null) {
			_service.shutDown();
			_service = null;
		}
		_server.stop(0);
		_serverThreads.shutdownNow();
		super.tearDown();
	}

	/**
	 * A server that sends its headers at once and then one byte a second neither holds the start
	 * nor the stop of the service; the bundled catalog answers meanwhile.
	 */
	public void testSlowCatalogDoesNotHoldTheStart() {
		long start = System.nanoTime();
		_service = start(url("/slow.json"), true);
		long startMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

		assertTrue("The start took " + startMillis + " ms.", startMillis < 1000);
		ModelInfo info = _service.getModel(MODEL);
		assertNotNull("The bundled catalog answers until the remote one arrives.", info);
		assertTrue(info.limits().contextWindow() != REMOTE_CONTEXT);
		assertEquals(ResolvedModel.Origin.BUNDLED, _service.getCatalogEntry(MODEL).origin());
		assertEquals(CatalogStatus.State.LOADING, _service.catalogStatus().state());
	}

	/**
	 * The whole fetch, body included, ends at its deadline, and the request is cancelled.
	 */
	public void testDeadlineCoversTheBody() throws Exception {
		long start = System.nanoTime();
		try {
			ModelsDevFetcher.fetch(url("/slow.json"), Map.of("anthropic", Set.of(MODEL)), Duration.ofSeconds(1));
			fail("A body that never ends must not be read to its end.");
		} catch (IOException expected) {
			// The deadline.
		}
		long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		assertTrue("The fetch took " + millis + " ms.", millis < 3000);
		assertTrue("The cancelled request must close its connection.", _slowClientGone.await(10, TimeUnit.SECONDS));
	}

	/**
	 * A catalog that arrives replaces the bundled one.
	 */
	public void testRemoteCatalogIsInstalled() throws InterruptedException {
		String url = url("/catalog.json");
		_service = start(url, true);
		CatalogStatus bundledStatus = _service.catalogStatus();

		awaitState(CatalogStatus.State.LOADED);
		assertEquals(1, _requests.get());

		ResolvedModel entry = _service.getCatalogEntry(MODEL);
		assertEquals(ResolvedModel.Origin.REMOTE, entry.origin());
		assertEquals(REMOTE_CONTEXT, entry.info().limits().contextWindow());
		assertSame(entry.info(), _service.getModel(MODEL));

		CatalogStatus status = _service.catalogStatus();
		assertEquals(url, status.catalogUrl());
		assertNull(status.failure());
		assertFalse("The remote catalog is installed after the bundled one.",
			status.installedAt().isBefore(bundledStatus.installedAt()));
	}

	/**
	 * Switched off, the service sends no request and works from the bundled catalog.
	 */
	public void testSwitchedOffSendsNoRequest() throws InterruptedException {
		_service = start(url("/catalog.json"), false);

		assertNotNull(_service.getModel(MODEL));
		Thread.sleep(1000);
		assertEquals(0, _requests.get());
	}

	/**
	 * Switched off, every model the bundled catalog lists is marked as bundled, and the status says
	 * that no remote catalog is loaded.
	 */
	public void testSwitchedOffUsesTheBundledCatalog() {
		ModelInfo snapshot =
			ModelsDevFetcher.loadSnapshot(Map.of("anthropic", Set.of(MODEL))).get(MODEL);
		assertNotNull("The bundled catalog lists the test model.", snapshot);

		_service = start(url("/catalog.json"), false);

		ResolvedModel entry = _service.getCatalogEntry(MODEL);
		assertEquals(ResolvedModel.Origin.BUNDLED, entry.origin());
		assertEquals(snapshot, entry.info());

		CatalogStatus status = _service.catalogStatus();
		assertEquals(CatalogStatus.State.DISABLED, status.state());
		assertNotNull(status.installedAt());
		assertNull(status.failure());
		assertSame(status, _service.catalog().status());
	}

	/**
	 * A model override takes precedence over the catalog and is marked as such; a configured model
	 * nothing describes is listed with its provider but has no catalog entry.
	 */
	public void testOverrideTakesPrecedence() {
		_service = start(url("/catalog.json"), false, List.of(MODEL, LOCAL_MODEL), MODEL);

		ResolvedModel entry = _service.getCatalogEntry(MODEL);
		assertEquals(ResolvedModel.Origin.OVERRIDE, entry.origin());
		assertEquals(OVERRIDE_CONTEXT, entry.info().limits().contextWindow());

		assertNull(_service.getCatalogEntry(LOCAL_MODEL));
		assertEquals(List.of(MODEL, LOCAL_MODEL), _service.getModelsOf("anthropic"));
		assertEquals(List.of(), _service.getModelsOf("no-such-provider"));
	}

	/**
	 * A remote catalog that cannot be loaded leaves the bundled catalog in use and says why.
	 */
	public void testFailedRemoteCatalog() throws InterruptedException {
		_service = start(url(BROKEN_PATH), true);

		awaitState(CatalogStatus.State.FAILED);

		CatalogStatus status = _service.catalogStatus();
		assertEquals(url(BROKEN_PATH), status.catalogUrl());
		assertTrue(status.failure(), status.failure().contains("500"));
		assertEquals(ResolvedModel.Origin.BUNDLED, _service.getCatalogEntry(MODEL).origin());
	}

	/**
	 * Saving the configuration of the running service writes no model metadata: the catalog is not
	 * part of the configuration.
	 */
	public void testSavedConfigurationHoldsNoCatalog() throws InterruptedException {
		_service = start(url("/catalog.json"), true);
		awaitState(CatalogStatus.State.LOADED);

		ApplicationConfig.Config application = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		application.getServices().put(LLMProviderService.class,
			InAppServiceConfigStore.newServiceEntry(LLMProviderService.class, _service.getConfig()));
		String written = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG, application,
			InAppServiceConfigStore.overrideTypes(false));

		assertTrue(written, written.contains(LLMProviderService.class.getName()));
		assertFalse(written, written.contains("<model "));
		assertFalse(written, written.contains(ModelCatalogEntry.OverrideConfig.CONTEXT_WINDOW));
		assertFalse(written, written.contains(ModelCatalogEntry.OverrideConfig.MAX_OUTPUT));
		assertFalse(written, written.contains(String.valueOf(REMOTE_CONTEXT)));
	}

	private void awaitState(CatalogStatus.State expected) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (_service.catalogStatus().state() != expected) {
			assertTrue("The catalog did not reach the state " + expected + ": " + _service.catalogStatus(),
				System.nanoTime() < deadline);
			Thread.sleep(20);
		}
	}

	private void answerCatalog(HttpExchange exchange) throws IOException {
		_requests.incrementAndGet();
		byte[] body = ("{\"anthropic\": {\"models\": {\"" + MODEL + "\": {\"id\": \"" + MODEL
			+ "\", \"limit\": {\"context\": " + REMOTE_CONTEXT + ", \"output\": 1000}}}}}")
				.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private void answerError(HttpExchange exchange) throws IOException {
		_requests.incrementAndGet();
		exchange.sendResponseHeaders(500, -1);
		exchange.close();
	}

	private void answerSlowly(HttpExchange exchange) throws IOException {
		_requests.incrementAndGet();
		exchange.sendResponseHeaders(200, 0);
		OutputStream out = exchange.getResponseBody();
		try {
			while (true) {
				out.write(' ');
				out.flush();
				Thread.sleep(1000);
			}
		} catch (IOException ex) {
			_slowClientGone.countDown();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			exchange.close();
		}
	}

	private String url(String path) {
		return "http://127.0.0.1:" + _server.getAddress().getPort() + path;
	}

	private static LLMProviderService start(String catalogUrl, boolean fetchRemote) {
		return start(catalogUrl, fetchRemote, List.of(MODEL), null);
	}

	/**
	 * Starts a service with one Anthropic provider named "anthropic" exposing the given models.
	 *
	 * <p>
	 * The caller shuts the service down.
	 * </p>
	 *
	 * @param catalogUrl
	 *        The URL of the remote catalog.
	 * @param fetchRemote
	 *        Whether the service loads the remote catalog.
	 * @param models
	 *        The models the provider is configured to serve.
	 * @param overridden
	 *        The model to give a model override with the context window {@link #OVERRIDE_CONTEXT},
	 *        or <code>null</code> for none.
	 */
	public static LLMProviderService start(String catalogUrl, boolean fetchRemote, List<String> models,
			String overridden) {
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);

		AnthropicProviderConfig.Config provider = TypedConfiguration.newConfigItem(AnthropicProviderConfig.Config.class);
		set(provider, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, AnthropicProviderConfig.class);
		provider.setName("anthropic");
		set(provider, LLMProviderConfig.Config.MODELS, models);
		set(provider, AnthropicProviderConfig.Config.API_KEY, FixedCredentials.secret("test"));
		config.getProviders().put(provider.getName(), provider);

		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		set(catalog, ModelCatalogConfig.CATALOG_URL, catalogUrl);
		set(catalog, ModelCatalogConfig.FETCH_REMOTE_CATALOG, fetchRemote);
		if (overridden != null) {
			ModelCatalogEntry.OverrideConfig override =
				TypedConfiguration.newConfigItem(ModelCatalogEntry.OverrideConfig.class);
			set(override, ModelCatalogEntry.OverrideConfig.NAME_ATTRIBUTE, overridden);
			set(override, ModelCatalogEntry.OverrideConfig.CONTEXT_WINDOW, OVERRIDE_CONTEXT);
			catalog.getModelOverrides().put(overridden, override);
		}
		set(config, LLMProviderService.Config.MODEL_CATALOG, catalog);

		LLMProviderService service =
			new LLMProviderService(new DefaultInstantiationContext(TestModelCatalogLoad.class), config);
		service.startUp();
		return service;
	}

	/**
	 * Stops a service {@link #start(String, boolean, List, String) started} by a test.
	 */
	public static void stop(LLMProviderService service) {
		service.shutDown();
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}

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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;

/**
 * Tests that the remote model catalog never holds the start of {@link LLMProviderService} (issue
 * #898).
 */
public class TestModelCatalogLoad extends TestCase {

	/** A model the bundled catalog knows. */
	private static final String MODEL = "claude-sonnet-4-20250514";

	/** The context window the test catalog gives {@link #MODEL}, which the bundled one does not. */
	private static final int REMOTE_CONTEXT = 4242;

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
		_service = start(url("/catalog.json"), true);

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (_service.getModel(MODEL).limits().contextWindow() != REMOTE_CONTEXT) {
			assertTrue("The remote catalog was not installed.", System.nanoTime() < deadline);
			Thread.sleep(20);
		}
		assertEquals(1, _requests.get());
		assertEquals(REMOTE_CONTEXT,
			_service.getConfig().getModelCatalog().getModelConfiguration().get(MODEL).getContextWindow().intValue());
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
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);

		AnthropicProviderConfig.Config provider = TypedConfiguration.newConfigItem(AnthropicProviderConfig.Config.class);
		set(provider, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME, AnthropicProviderConfig.class);
		provider.setName("anthropic");
		set(provider, LLMProviderConfig.Config.MODELS, List.of(MODEL));
		set(provider, AnthropicProviderConfig.Config.API_KEY, "unencrypted:test");
		config.getProviders().put(provider.getName(), provider);

		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		set(catalog, ModelCatalogConfig.CATALOG_URL, catalogUrl);
		set(catalog, ModelCatalogConfig.FETCH_REMOTE_CATALOG, fetchRemote);
		set(config, LLMProviderService.Config.MODEL_CATALOG, catalog);

		LLMProviderService service =
			new LLMProviderService(new DefaultInstantiationContext(TestModelCatalogLoad.class), config);
		service.startUp();
		return service;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}

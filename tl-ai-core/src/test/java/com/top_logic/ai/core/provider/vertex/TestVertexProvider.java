/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.vertex;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.ai.core.message.AssistantMessage;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.Message;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.ThinkingContent;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.message.UserMessage;
import com.top_logic.ai.core.provider.ApiFamilies;
import com.top_logic.ai.core.provider.I18NConstants;
import com.top_logic.ai.core.provider.LLMProviderConfig;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.ai.core.provider.LLMProviderServiceFixture;
import com.top_logic.ai.core.provider.MessageHistorySanitizer;
import com.top_logic.ai.core.provider.ProviderException;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.StreamEvent;
import com.top_logic.ai.core.provider.StreamEventContractAssertions;
import com.top_logic.ai.core.provider.ToolChoice;
import com.top_logic.ai.core.provider.catalog.ModelInfo;
import com.top_logic.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Tests the {@link VertexProviderConfig Google Vertex AI provider} against a local HTTP server
 * standing in for a gateway.
 */
public class TestVertexProvider extends TestCase {

	private static final String MODEL = "claude-sonnet-5-5";

	private static final String MODEL_PATH =
		"/v1/projects/my-project/locations/eu/publishers/anthropic/models/claude-sonnet-5-5:streamRawPredict";

	private static final String AUTHORIZATION = "authorization";

	private static final String STREAM = ""
		+ "event: message_start\n"
		+ "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\",\"type\":\"message\","
		+ "\"role\":\"assistant\",\"model\":\"claude-sonnet-5-5\","
		+ "\"usage\":{\"input_tokens\":10,\"output_tokens\":0}}}\n\n"
		+ "event: content_block_start\n"
		+ "data: {\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"thinking\"}}\n\n"
		+ "event: content_block_delta\n"
		+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
		+ "\"delta\":{\"type\":\"thinking_delta\",\"thinking\":\"Let me think.\"}}\n\n"
		+ "event: content_block_delta\n"
		+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
		+ "\"delta\":{\"type\":\"signature_delta\",\"signature\":\"sig-1\"}}\n\n"
		+ "event: content_block_stop\n"
		+ "data: {\"type\":\"content_block_stop\",\"index\":0}\n\n"
		+ "event: content_block_start\n"
		+ "data: {\"type\":\"content_block_start\",\"index\":1,\"content_block\":{\"type\":\"text\"}}\n\n"
		+ "event: content_block_delta\n"
		+ "data: {\"type\":\"content_block_delta\",\"index\":1,"
		+ "\"delta\":{\"type\":\"text_delta\",\"text\":\"Looking it up.\"}}\n\n"
		+ "event: content_block_stop\n"
		+ "data: {\"type\":\"content_block_stop\",\"index\":1}\n\n"
		+ "event: content_block_start\n"
		+ "data: {\"type\":\"content_block_start\",\"index\":2,"
		+ "\"content_block\":{\"type\":\"tool_use\",\"id\":\"toolu_1\",\"name\":\"search\"}}\n\n"
		+ "event: content_block_delta\n"
		+ "data: {\"type\":\"content_block_delta\",\"index\":2,"
		+ "\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"q\\\":\\\"x\\\"}\"}}\n\n"
		+ "event: content_block_stop\n"
		+ "data: {\"type\":\"content_block_stop\",\"index\":2}\n\n"
		+ "event: message_delta\n"
		+ "data: {\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"tool_use\"},"
		+ "\"usage\":{\"output_tokens\":20}}\n\n"
		+ "event: message_stop\n"
		+ "data: {\"type\":\"message_stop\"}\n\n";

	private HttpServer _server;

	private final List<Received> _requests = new ArrayList<>();

	private int _status = 200;

	private String _responseBody = STREAM;

	private LLMProviderService _service;

	/**
	 * A request the server received.
	 *
	 * @param method
	 *        The HTTP method.
	 * @param path
	 *        The request path.
	 * @param headers
	 *        The headers, names in lower case.
	 * @param body
	 *        The parsed JSON body.
	 */
	private record Received(String method, String path, Map<String, String> headers, Map<String, Object> body) {
		// Values only.
	}

	/**
	 * Parsing provider configurations resolves their nested configuration types through the
	 * <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestVertexProvider.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		if (_service != null) {
			LLMProviderServiceFixture.stop(_service);
			_service = null;
		}
		super.tearDown();
	}

	private void handle(HttpExchange exchange) throws IOException {
		Map<String, String> headers = new TreeMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		synchronized (_requests) {
			_requests.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), headers,
				map(JsonUtil.parseJsonValue(json))));
		}
		byte[] body = _responseBody.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type",
			_status == 200 ? "text/event-stream" : "application/json");
		exchange.sendResponseHeaders(_status, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	/**
	 * The request reaches the model's endpoint with the API key, without the model and with the
	 * API version in the body, and with Claude's options.
	 */
	public void testRequest() throws Exception {
		stream(provider(""), request(null, Map.of("thinking_effort", "high",
			"top_k", Integer.valueOf(40), "cacheTtl", "1h")));

		Received received = _requests.get(0);
		assertEquals("POST", received.method());
		assertEquals(MODEL_PATH, received.path());
		assertEquals("Bearer vertex-key", received.headers().get(AUTHORIZATION));
		assertNull(received.headers().get("anthropic-version"));
		assertNull(received.headers().get("x-api-key"));

		Map<String, Object> body = received.body();
		assertFalse("The model is named in the path only.", body.containsKey("model"));
		assertEquals(VertexProviderConfig.DEFAULT_ANTHROPIC_VERSION, body.get("anthropic_version"));
		assertEquals(Boolean.TRUE, body.get("stream"));
		assertEquals(Long.valueOf(40), body.get("top_k"));
		assertEquals("adaptive", map(body.get("thinking")).get("type"));
		assertEquals("high", map(body.get("output_config")).get("effort"));
		Map<String, Object> system = map(list(body.get("system")).get(0));
		assertEquals("1h", map(system.get("cache_control")).get("ttl"));
	}

	/**
	 * Text, thinking and tool calls arrive as stream events; the thinking is stamped with the
	 * catalog ID of the provider.
	 */
	public void testStreamEvents() throws Exception {
		List<StreamEvent> events = stream(provider(""), request(null, Map.of()));

		StreamEventContractAssertions.assertValidStream(events);
		assertTrue(events.stream().anyMatch(StreamEvent.TextDelta.class::isInstance));
		assertTrue(events.stream().anyMatch(StreamEvent.ThinkingDelta.class::isInstance));
		assertTrue(events.stream().anyMatch(StreamEvent.ToolCallStart.class::isInstance));

		List<ContentPart> content = complete(events).content();
		ThinkingContent thinking = (ThinkingContent) content.get(0);
		assertEquals("Let me think.", thinking.text());
		assertEquals("sig-1", thinking.continuityToken());
		assertEquals(VertexProviderConfig.DEFAULT_CATALOG_PROVIDER_ID, thinking.provider());
		assertEquals("Looking it up.", ((TextContent) content.get(1)).text());
		assertEquals("search", ((ToolCall) content.get(2)).name());
	}

	/**
	 * Thinking of a Vertex response is replayed to Vertex in the next round.
	 */
	public void testThinkingIsReplayed() throws Exception {
		VertexProviderConfig provider = provider("");
		List<ContentPart> content = complete(stream(provider, request(null, Map.of()))).content();
		AssistantMessage assistant = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(content.get(0), content.get(1)), new TokenUsage(1, 1, 0, 0, 0), "end_turn");
		List<Message> history = MessageHistorySanitizer.forProvider(
			List.of(user("Hello."), assistant, user("Go on.")), provider.getCatalogProviderId());

		stream(provider, new ProviderRequest(MODEL, null, history, List.of(), 1000));

		Map<String, Object> replayed = map(list(map(list(last().body().get("messages")).get(1)).get("content")).get(0));
		assertEquals("thinking", replayed.get("type"));
		assertEquals("Let me think.", replayed.get("thinking"));
		assertEquals("sig-1", replayed.get("signature"));
	}

	/**
	 * Errors in Anthropic's form and in Google's form are classified.
	 */
	public void testErrors() throws Exception {
		assertEquals(ProviderException.Kind.OVERLOADED, error(529,
			"{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\",\"message\":\"Overloaded\"}}"));
		assertEquals(ProviderException.Kind.INVALID_REQUEST, error(400,
			"{\"type\":\"error\",\"error\":{\"type\":\"invalid_request_error\",\"message\":\"Bad\"}}"));
		assertEquals(ProviderException.Kind.RATE_LIMITED, error(429,
			"{\"error\":{\"code\":429,\"message\":\"Quota exceeded\",\"status\":\"RESOURCE_EXHAUSTED\"}}"));
		assertEquals(ProviderException.Kind.AUTHENTICATION, error(403,
			"{\"error\":{\"code\":403,\"message\":\"Denied\",\"status\":\"PERMISSION_DENIED\"}}"));
		assertEquals(ProviderException.Kind.OVERLOADED, error(503,
			"{\"error\":{\"code\":503,\"message\":\"Unavailable\",\"status\":\"UNAVAILABLE\"}}"));
		assertEquals(ProviderException.Kind.INVALID_REQUEST, error(400,
			"{\"error\":{\"code\":400,\"message\":\"Invalid\",\"status\":\"INVALID_ARGUMENT\"}}"));
	}

	/**
	 * With only {@code auto} accepted, a forced tool call fails before the request is sent.
	 */
	public void testRequiredRefusedWithoutRequest() throws Exception {
		try {
			provider("tool-choice='auto'").createProvider().stream(request(ToolChoice.REQUIRED, Map.of()),
				event -> {
					// Not observed.
				});
			fail("A forced tool call must be refused.");
		} catch (ProviderException ex) {
			assertEquals(ProviderException.Kind.INVALID_REQUEST, ex.kind());
		}
		assertEquals(List.of(), _requests);
	}

	/**
	 * With only {@code auto} accepted, a request without tool calls is sent without a tool choice
	 * and keeps its tools.
	 */
	public void testNoneSentAsAuto() throws Exception {
		stream(provider("tool-choice='auto'"), request(ToolChoice.NONE, Map.of()));

		Map<String, Object> body = last().body();
		assertFalse(body.containsKey("tool_choice"));
		assertEquals(1, list(body.get("tools")).size());
	}

	/**
	 * By default every tool choice is passed on.
	 */
	public void testAllToolChoicesByDefault() throws Exception {
		stream(provider(""), request(ToolChoice.REQUIRED, Map.of()));

		assertEquals("any", map(last().body().get("tool_choice")).get("type"));
	}

	/**
	 * Without a base URL, the host is derived from the location.
	 */
	public void testHostOfLocation() {
		assertEquals("aiplatform.googleapis.com", VertexProviderConfig.host("global"));
		assertEquals("aiplatform.eu.rep.googleapis.com", VertexProviderConfig.host("eu"));
		assertEquals("europe-west4-aiplatform.googleapis.com", VertexProviderConfig.host("europe-west4"));
	}

	/**
	 * A base URL with or without trailing API version addresses the same endpoint.
	 */
	public void testBaseUrlWithAndWithoutVersion() throws Exception {
		stream(provider(""), request(null, Map.of()));
		stream(provider(baseUrl(), "", ""), request(null, Map.of()));
		stream(provider(baseUrl() + "/v1/", "", ""), request(null, Map.of()));

		assertEquals(3, _requests.size());
		for (Received received : _requests) {
			assertEquals(MODEL_PATH, received.path());
		}
	}

	/**
	 * The settings of a model decide its publisher.
	 */
	public void testPublisherSetting() throws Exception {
		VertexProviderConfig provider = provider(baseUrl(), "models='my-model'",
			"<model-settings><model name='my-model' publisher='anthropic'/></model-settings>");

		stream(provider, new ProviderRequest("my-model", null, List.of(user("Hello.")), List.of(), 1000));

		assertEquals("/v1/projects/my-project/locations/eu/publishers/anthropic/models/my-model:streamRawPredict",
			last().path());
		assertEquals(ApiFamilies.ANTHROPIC, provider.getApiFamily("my-model"));
	}

	/**
	 * A model without a known publisher prevents the start.
	 */
	public void testModelWithoutPublisher() throws Exception {
		try {
			provider(baseUrl(), "models='gemini-3-pro'", "");
			fail("A model without publisher must be refused.");
		} catch (RuntimeException ex) {
			assertRefused(ex, I18NConstants.ERROR_VERTEX_NO_PUBLISHER__PROVIDER_MODEL.fill("test", "gemini-3-pro"));
		}
	}

	/**
	 * A model of a publisher that is not supported prevents the start.
	 */
	public void testUnsupportedPublisher() throws Exception {
		try {
			provider(baseUrl(), "models='gemini-3-pro'",
				"<model-settings><model name='gemini-3-pro' publisher='google'/></model-settings>");
			fail("A model of an unsupported publisher must be refused.");
		} catch (RuntimeException ex) {
			assertRefused(ex, I18NConstants.ERROR_VERTEX_UNSUPPORTED_PUBLISHER__PROVIDER_MODEL_PUBLISHER_SUPPORTED
				.fill("test", "gemini-3-pro", "google", VertexProviderConfig.PUBLISHER_ANTHROPIC));
		}
	}

	/**
	 * Claude on Vertex has the catalog data of its {@code @default} entry.
	 */
	public void testCatalog() throws Exception {
		LLMProviderConfig.Config vertex = config(baseUrl(), "", "");
		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) TypedConfiguration.newConfigItem(
				LLMProviderService.Config.class);
		config.getProviders().put(vertex.getName(), vertex);
		ModelCatalogConfig catalog = TypedConfiguration.newConfigItem(ModelCatalogConfig.class);
		catalog.update(catalog.descriptor().getProperty(ModelCatalogConfig.FETCH_REMOTE_CATALOG), Boolean.FALSE);
		config.update(config.descriptor().getProperty(LLMProviderService.Config.MODEL_CATALOG), catalog);
		_service = LLMProviderServiceFixture.start(new DefaultInstantiationContext(TestVertexProvider.class), config);

		ModelInfo sonnet = _service.getModel(MODEL);
		assertNotNull(sonnet);
		assertEquals(1_000_000, sonnet.limits().contextWindow());
		assertEquals(ApiFamilies.ANTHROPIC, _service.getApiFamily(MODEL));
	}

	private ProviderException.Kind error(int status, String body) throws Exception {
		_status = status;
		_responseBody = body;
		try {
			provider("").createProvider().stream(request(null, Map.of()), event -> {
				// Not observed.
			});
			fail("An error response must fail the request.");
			return null;
		} catch (ProviderException ex) {
			return ex.kind();
		}
	}

	private List<StreamEvent> stream(VertexProviderConfig provider, ProviderRequest request) throws Exception {
		List<StreamEvent> events = new ArrayList<>();
		provider.createProvider().stream(request, events::add);
		return events;
	}

	private static StreamEvent.MessageComplete complete(List<StreamEvent> events) {
		return (StreamEvent.MessageComplete) events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance).findFirst().orElseThrow();
	}

	private Received last() {
		return _requests.get(_requests.size() - 1);
	}

	private static ProviderRequest request(ToolChoice toolChoice, Map<String, Object> options) {
		ToolSchema tool = new ToolSchema("search", "Searches.",
			"{\"type\":\"object\",\"properties\":{\"q\":{\"type\":\"string\"}}}");
		return new ProviderRequest(MODEL, "You are helpful.", List.of(user("Hello.")), List.of(tool), 4096,
			options, null, toolChoice);
	}

	private static UserMessage user(String text) {
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)));
	}

	private String baseUrl() {
		return "http://127.0.0.1:" + _server.getAddress().getPort();
	}

	/** A provider against the local server, configured as a gateway URL with API version. */
	private VertexProviderConfig provider(String attributes) throws ConfigurationException {
		return provider(baseUrl() + "/v1", attributes, "");
	}

	private VertexProviderConfig provider(String baseUrl, String attributes, String content)
			throws ConfigurationException {
		return (VertexProviderConfig) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config(baseUrl, attributes, content));
	}

	private static LLMProviderConfig.Config config(String baseUrl, String attributes, String content)
			throws ConfigurationException {
		String models = attributes.contains("models=") ? "" : " models='" + MODEL + "'";
		String xml = "<provider xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + VertexProviderConfig.Config.class.getName() + "'"
			+ " class='" + VertexProviderConfig.class.getName() + "' name='test'"
			+ " base-url='" + baseUrl + "' project='my-project' location='eu'"
			+ " api-key='unencrypted:vertex-key'" + models + " " + attributes + ">" + content + "</provider>";
		return (LLMProviderConfig.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	private static void assertRefused(Throwable ex, ResKey message) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConfigurationError error && message.equals(error.getErrorKey())) {
				return;
			}
		}
		throw new AssertionError("Not refused with " + message + ": " + ex, ex);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> list(Object value) {
		return (List<Object>) value;
	}
}

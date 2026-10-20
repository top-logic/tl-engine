/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import junit.framework.TestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.index.lucene.Vectors;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * The HTTP embedder against a fake OpenAI-compatible server on a local port: one request per batch,
 * prefixes, order, cut to the configured dimensions, retry and refusal.
 */
public class TestOpenAiCompatibleEmbedder extends TestCase {

	/** One request the fake server received. */
	private record Received(String authorization, String model, List<String> input, String path) {
		// A plain carrier.
	}

	private HttpServer _server;

	private final ConcurrentLinkedQueue<Received> _received = new ConcurrentLinkedQueue<>();

	/** Answers each request; the argument is the number of the request, from 1. */
	private Function<Integer, Answer> _answers;

	/** An answer of the fake server. */
	private record Answer(int status, boolean reversed, int dimensions) {
		// A plain carrier.
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
		_answers = n -> new Answer(200, false, 8);
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		super.tearDown();
	}

	/**
	 * Seven texts with batch size 3 go out as three requests of 3, 3 and 1 texts, each with the
	 * model, the key and the passage prefix, and come back as seven vectors in the order of the
	 * texts.
	 */
	public void testBatchesCarryManyTexts() throws EmbeddingException {
		Embedder embedder = embedder(4, 3, 0);
		List<String> texts = List.of("a", "b", "c", "d", "e", "f", "g");

		List<float[]> vectors = embedder.embedPassages(texts);

		assertEquals(7, vectors.size());
		List<Received> requests = new ArrayList<>(_received);
		assertEquals(3, requests.size());
		assertEquals(List.of("passage: a", "passage: b", "passage: c"), requests.get(0).input());
		assertEquals(List.of("passage: g"), requests.get(2).input());
		assertEquals("/v1/embeddings", requests.get(0).path());
		assertEquals("Bearer secret", requests.get(0).authorization());
		assertEquals("test-model", requests.get(0).model());
		for (int i = 0; i < texts.size(); i++) {
			assertVector(texts.get(i), "passage: ", vectors.get(i));
		}
	}

	/**
	 * Read from XML as an application writes it, the prefixes keep their trailing space: the models
	 * were trained with {@code "query: "}, not {@code "query:"}.
	 */
	public void testPrefixesFromXmlKeepTheirSpace() throws Exception {
		String xml = "<embedder class=\"" + OpenAiCompatibleEmbedder.class.getName() + "\" base-url=\"http://127.0.0.1:"
			+ _server.getAddress().getPort() + "\" model=\"m\" dimensions=\"4\" query-prefix=\"query: \""
			+ " passage-prefix=\"passage: \"/>";
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("embedder",
				TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class)));
		reader.setSource(CharacterContents.newContent(xml));
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) reader.read();
		Embedder embedder = (Embedder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		embedder.embedQuery("Grundplatte");
		embedder.embedPassages(List.of("Base plate"));
		List<Received> requests = new ArrayList<>(_received);
		assertEquals(List.of("query: Grundplatte"), requests.get(0).input());
		assertEquals(List.of("passage: Base plate"), requests.get(1).input());
	}

	/** An answer whose entries come in another order is put back into the order of the texts. */
	public void testOrderFollowsTheIndex() throws EmbeddingException {
		_answers = n -> new Answer(200, true, 8);
		List<float[]> vectors = embedder(4, 10, 0).embedPassages(List.of("x", "y", "z"));
		assertVector("x", "passage: ", vectors.get(0));
		assertVector("z", "passage: ", vectors.get(2));
	}

	/** A query gets the query prefix and comes back cut to the configured dimensions, normalized. */
	public void testQueryIsPrefixedAndCut() throws EmbeddingException {
		float[] vector = embedder(4, 10, 0).embedQuery("Grundplatte");

		assertEquals(4, vector.length);
		assertEquals(1.0, Vectors.dot(vector, vector), 1e-5);
		assertEquals(List.of("query: Grundplatte"), _received.peek().input());
	}

	/** A server error is retried, and the retry succeeds. */
	public void testServerErrorIsRetried() throws EmbeddingException {
		_answers = n -> n == 1 ? new Answer(503, false, 8) : new Answer(200, false, 8);
		List<float[]> vectors = embedder(4, 10, 2).embedPassages(List.of("a"));
		assertEquals(1, vectors.size());
		assertEquals(2, _received.size());
	}

	/** A refused request fails at once, without retries. */
	public void testRefusalIsNotRetried() {
		_answers = n -> new Answer(400, false, 8);
		try {
			embedder(4, 10, 3).embedPassages(List.of("a"));
			fail("A refused request must fail.");
		} catch (EmbeddingException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("HTTP 400"));
		}
		assertEquals(1, _received.size());
	}

	/** A server that keeps failing ends in an exception after the configured retries. */
	public void testRetriesEnd() {
		_answers = n -> new Answer(502, false, 8);
		try {
			embedder(4, 10, 2).embedPassages(List.of("a"));
			fail("A failing server must fail the call.");
		} catch (EmbeddingException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("failed 3 times"));
		}
		assertEquals(3, _received.size());
	}

	/** Vectors shorter than the configured dimensions are refused rather than padded. */
	public void testShortVectorsAreRefused() {
		_answers = n -> new Answer(200, false, 2);
		try {
			embedder(4, 10, 0).embedPassages(List.of("a"));
			fail("A vector shorter than configured must be refused.");
		} catch (EmbeddingException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("2 dimensions"));
		}
	}

	private OpenAiCompatibleEmbedder embedder(int dimensions, int batch, int retries) {
		@SuppressWarnings("unchecked")
		OpenAiCompatibleEmbedder.Config<OpenAiCompatibleEmbedder> config =
			TypedConfiguration.newConfigItem(OpenAiCompatibleEmbedder.Config.class);
		set(config, OpenAiCompatibleEmbedder.Config.BASE_URL,
			"http://127.0.0.1:" + _server.getAddress().getPort() + "/v1/");
		set(config, OpenAiCompatibleEmbedder.Config.MODEL, "test-model");
		set(config, OpenAiCompatibleEmbedder.Config.API_KEY, "secret");
		set(config, AbstractEmbedder.Config.DIMENSIONS, dimensions);
		set(config, AbstractEmbedder.Config.BATCH_SIZE, batch);
		set(config, AbstractEmbedder.Config.QUERY_PREFIX, "query: ");
		set(config, AbstractEmbedder.Config.PASSAGE_PREFIX, "passage: ");
		set(config, OpenAiCompatibleEmbedder.Config.RETRIES, retries);
		set(config, OpenAiCompatibleEmbedder.Config.RETRY_DELAY_MILLIS, 1L);
		return new OpenAiCompatibleEmbedder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private static void set(OpenAiCompatibleEmbedder.Config<?> config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/** The vector is the fake server's answer for the prefixed text, cut and normalized. */
	private static void assertVector(String text, String prefix, float[] actual) {
		float[] expected = Vectors.normalized(Arrays.copyOf(fake(prefix + text, 8), actual.length));
		assertEquals(1.0, Vectors.dot(expected, actual), 1e-5);
	}

	/** The fake server's vector for a text: distinct per text. */
	private static float[] fake(String text, int dimensions) {
		float[] result = new float[dimensions];
		int hash = text.hashCode();
		for (int i = 0; i < dimensions; i++) {
			result[i] = ((hash >> i) & 7) + 1;
		}
		return result;
	}

	private void handle(HttpExchange exchange) throws IOException {
		String body;
		try (InputStream in = exchange.getRequestBody()) {
			body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		String model = null;
		List<String> input = new ArrayList<>();
		try (JsonReader json = new JsonReader(new StringR(body))) {
			json.beginObject();
			while (json.hasNext()) {
				switch (json.nextName()) {
					case "model" -> model = json.nextString();
					case "input" -> {
						json.beginArray();
						while (json.hasNext()) {
							input.add(json.nextString());
						}
						json.endArray();
					}
					default -> json.skipValue();
				}
			}
			json.endObject();
		}
		_received.add(new Received(exchange.getRequestHeaders().getFirst("Authorization"), model, input,
			exchange.getRequestURI().getPath()));
		Answer answer = _answers.apply(_received.size());
		StringBuilder out = new StringBuilder();
		if (answer.status() == 200) {
			List<Integer> order = new ArrayList<>();
			for (int i = 0; i < input.size(); i++) {
				order.add(i);
			}
			if (answer.reversed()) {
				Collections.reverse(order);
			}
			out.append("{\"object\":\"list\",\"data\":[");
			for (int k = 0; k < order.size(); k++) {
				int i = order.get(k);
				if (k > 0) {
					out.append(',');
				}
				out.append("{\"object\":\"embedding\",\"index\":").append(i).append(",\"embedding\":[");
				float[] vector = fake(input.get(i), answer.dimensions());
				for (int d = 0; d < vector.length; d++) {
					out.append(d > 0 ? "," : "").append(vector[d]);
				}
				out.append("]}");
			}
			out.append("],\"model\":\"").append(model).append("\"}");
		} else {
			out.append("{\"error\":\"status ").append(answer.status()).append("\"}");
		}
		byte[] bytes = out.toString().getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(answer.status(), bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

}

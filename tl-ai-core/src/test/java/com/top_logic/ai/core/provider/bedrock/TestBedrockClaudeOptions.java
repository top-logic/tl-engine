/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.bedrock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.ai.core.message.AssistantMessage;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.Message;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.ThinkingContent;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.message.UserMessage;
import com.top_logic.ai.core.provider.FixedCredentials;
import com.top_logic.ai.core.provider.MessageHistorySanitizer;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.StreamEvent;
import com.top_logic.ai.core.provider.StreamEventContractAssertions;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Tests that Claude on AWS Bedrock gets the options and the thinking continuity it gets from the
 * Anthropic Messages API, and that other models on Bedrock get none of them.
 */
public class TestBedrockClaudeOptions extends TestCase {

	private static final String CLAUDE = "eu.anthropic.claude-opus-5-5";

	private static final String NOVA = "amazon.nova-pro-v1:0";

	private static final Map<String, Object> CLAUDE_OPTIONS =
		Map.of("thinking_effort", "high", "top_k", Integer.valueOf(40), "cacheTtl", "1h");

	/**
	 * Effort, {@code top_k} and the cache lifetime of a Claude model reach the Converse request.
	 */
	public void testClaudeOptionsInRequest() throws Exception {
		Map<String, Object> root = build(CLAUDE, CLAUDE_OPTIONS, List.of(user("Hello.")));

		Map<String, Object> additional = map(root.get("additionalModelRequestFields"));
		assertNotNull("Claude options must be sent as additionalModelRequestFields.", additional);
		assertEquals(Long.valueOf(40), additional.get("top_k"));
		Map<String, Object> thinking = map(additional.get("thinking"));
		assertNotNull("Thinking must be configured.", thinking);
		assertEquals("adaptive", thinking.get("type"));
		assertEquals("high", map(additional.get("output_config")).get("effort"));

		List<Object> system = list(root.get("system"));
		assertEquals("1h", map(map(system.get(system.size() - 1)).get("cachePoint")).get("ttl"));
		for (Object message : list(root.get("messages"))) {
			List<Object> content = list(map(message).get("content"));
			for (Object block : content) {
				Map<String, Object> cachePoint = map(map(block).get("cachePoint"));
				if (cachePoint != null) {
					assertEquals("1h", cachePoint.get("ttl"));
				}
			}
		}
	}

	/**
	 * A model that is not Claude gets none of Anthropic's options, even if they are in the request.
	 */
	public void testOtherModelGetsNoClaudeOptions() throws Exception {
		Map<String, Object> root = build(NOVA, CLAUDE_OPTIONS, List.of(user("Hello.")));

		assertNull(root.get("additionalModelRequestFields"));
		List<Object> system = list(root.get("system"));
		assertNull(map(map(system.get(system.size() - 1)).get("cachePoint")).get("ttl"));
	}

	/**
	 * Without a cache lifetime, the cache points carry none.
	 */
	public void testNoTtlWithoutCacheTtl() throws Exception {
		Map<String, Object> root = build(CLAUDE, Map.of("thinking_effort", "low"), List.of(user("Hello.")));

		List<Object> system = list(root.get("system"));
		assertEquals(Map.of("type", "default"), map(system.get(system.size() - 1)).get("cachePoint"));
	}

	/**
	 * Thinking streamed by Bedrock is replayed to Bedrock in the next round, with its signature.
	 */
	public void testThinkingIsReplayed() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame("messageStart", "{\"role\":\"assistant\"}"));
		buf.write(frame("contentBlockDelta",
			"{\"contentBlockIndex\":0,\"delta\":{\"reasoningContent\":{\"text\":\"Let me \"}}}"));
		buf.write(frame("contentBlockDelta",
			"{\"contentBlockIndex\":0,\"delta\":{\"reasoningContent\":{\"text\":\"think.\"}}}"));
		buf.write(frame("contentBlockDelta",
			"{\"contentBlockIndex\":0,\"delta\":{\"reasoningContent\":{\"signature\":\"sig-1\"}}}"));
		buf.write(frame("contentBlockStop", "{\"contentBlockIndex\":0}"));
		buf.write(frame("contentBlockDelta", "{\"contentBlockIndex\":1,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame("contentBlockStop", "{\"contentBlockIndex\":1}"));
		buf.write(frame("messageStop", "{\"stopReason\":\"end_turn\"}"));
		buf.write(frame("metadata", "{\"usage\":{\"inputTokens\":5,\"outputTokens\":2}}"));

		List<StreamEvent> events = new ArrayList<>();
		new TestableProvider().parse(new ByteArrayInputStream(buf.toByteArray()), events);
		StreamEventContractAssertions.assertValidStream(events);
		assertTrue("Thinking must be streamed.",
			events.stream().anyMatch(StreamEvent.ThinkingDelta.class::isInstance));

		StreamEvent.MessageComplete complete = (StreamEvent.MessageComplete) events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance).findFirst().orElseThrow();
		List<ContentPart> content = complete.content();
		ThinkingContent thinking = (ThinkingContent) content.get(0);
		assertEquals("Let me think.", thinking.text());
		assertEquals("sig-1", thinking.continuityToken());
		assertEquals(BedrockConverseProviderConfig.DEFAULT_CATALOG_PROVIDER_ID, thinking.provider());

		AssistantMessage assistant = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			content, new TokenUsage(5, 2, 0, 0, 0), "end_turn");
		List<Message> history = MessageHistorySanitizer.forProvider(
			List.of(user("Hello."), assistant, user("Go on.")),
			BedrockConverseProviderConfig.DEFAULT_CATALOG_PROVIDER_ID);

		Map<String, Object> root = build(CLAUDE, Map.of(), history);
		List<Object> replayed = list(map(list(root.get("messages")).get(1)).get("content"));
		Map<String, Object> reasoningText =
			map(map(map(replayed.get(0)).get("reasoningContent")).get("reasoningText"));
		assertNotNull("The thinking block must be replayed as reasoningContent.", reasoningText);
		assertEquals("Let me think.", reasoningText.get("text"));
		assertEquals("sig-1", reasoningText.get("signature"));
		assertEquals("Hi", map(replayed.get(1)).get("text"));
	}

	/**
	 * Thinking is stamped with the catalog ID the provider is configured with: it is replayed to a
	 * provider of that ID and dropped for another one.
	 */
	public void testThinkingStampedWithConfiguredCatalogId() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame("contentBlockDelta",
			"{\"contentBlockIndex\":0,\"delta\":{\"reasoningContent\":{\"text\":\"Hmm.\"}}}"));
		buf.write(frame("contentBlockDelta",
			"{\"contentBlockIndex\":0,\"delta\":{\"reasoningContent\":{\"signature\":\"sig-2\"}}}"));
		buf.write(frame("contentBlockStop", "{\"contentBlockIndex\":0}"));
		buf.write(frame("contentBlockDelta", "{\"contentBlockIndex\":1,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame("contentBlockStop", "{\"contentBlockIndex\":1}"));
		buf.write(frame("messageStop", "{\"stopReason\":\"end_turn\"}"));
		buf.write(frame("metadata", "{\"usage\":{\"inputTokens\":5,\"outputTokens\":2}}"));

		List<StreamEvent> events = new ArrayList<>();
		new TestableProvider("my-bedrock").parse(new ByteArrayInputStream(buf.toByteArray()), events);
		StreamEvent.MessageComplete complete = (StreamEvent.MessageComplete) events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance).findFirst().orElseThrow();
		ThinkingContent thinking = (ThinkingContent) complete.content().get(0);
		assertEquals("my-bedrock", thinking.provider());

		AssistantMessage assistant = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			complete.content(), new TokenUsage(5, 2, 0, 0, 0), "end_turn");
		List<Message> history = List.of(user("Hello."), assistant);
		assertTrue(thinkingOf(MessageHistorySanitizer.forProvider(history, "my-bedrock")));
		assertFalse(thinkingOf(MessageHistorySanitizer.forProvider(history,
			BedrockConverseProviderConfig.DEFAULT_CATALOG_PROVIDER_ID)));
	}

	private static boolean thinkingOf(List<Message> history) {
		return ((AssistantMessage) history.get(1)).content().stream().anyMatch(ThinkingContent.class::isInstance);
	}

	private static Map<String, Object> build(String model, Map<String, Object> options, List<Message> messages)
			throws Exception {
		ProviderRequest request = new ProviderRequest(model, "You are helpful.", messages, List.of(), 4096,
			options, null, null);
		return map(JsonUtil.parseJsonValue(BedrockConverseRequestBuilder.buildRequestJson(request)));
	}

	private static UserMessage user(String text) {
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)));
	}

	private static byte[] frame(String eventType, String payload) throws Exception {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put(":event-type", eventType);
		headers.put(":message-type", "event");
		return TestEventStreamReader.frame(headers, payload);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> list(Object value) {
		return (List<Object>) value;
	}

	/**
	 * Exposes the stream parsing of {@link BedrockConverseProvider} without an HTTP round trip.
	 */
	private static final class TestableProvider extends BedrockConverseProvider {

		TestableProvider() {
			super(FixedCredentials.NONE, "https://bedrock.example.com", HttpClient.newHttpClient());
		}

		TestableProvider(String catalogProviderId) {
			super(FixedCredentials.NONE, "https://bedrock.example.com", 10, 10, Map.of(), catalogProviderId);
		}

		void parse(java.io.InputStream in, List<StreamEvent> events) throws Exception {
			processStream(in, events::add);
		}
	}
}

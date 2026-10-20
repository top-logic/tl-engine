/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Tests how {@link AnthropicRequestBuilder} puts multimodal content and the thinking configuration
 * on the wire.
 *
 * <p>The Messages API permits {@code text}, {@code image} and {@code document} blocks inside a
 * {@code tool_result}, so an image a tool produced needs no follow-up user message. The same blocks
 * carry attachments a caller put on a user turn.</p>
 */
public class TestAnthropicRequestBuilder extends TestCase {

	private static final String CALL_ID = "toolu_01A09q90qw90lq917835lq9";

	private static final String IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUg==";

	private static final String PDF_BASE64 = "JVBERi0xLjcKZmFrZQ==";

	/** An image and a PDF a tool returned become native blocks inside {@code tool_result}. */
	public void testMultimodalToolResultIsNative() throws Exception {
		String json = buildJson(List.of(
			new TextContent("Image content of \"demo.png\" (image/png), shown to you with this result."),
			new ImageContent("data:image/png;base64," + IMAGE_BASE64, "image/png"),
			new PdfContent(PDF_BASE64, "application/pdf")));

		List<Object> messages = messages(json);
		assertEquals("The tool result must not be followed by an extra message.", 3, messages.size());

		Map<String, Object> toolResult = toolResultBlock(messages.get(2));
		Object content = toolResult.get("content");
		assertTrue("Expected a list of content blocks, got: " + content, content instanceof List);

		List<?> blocks = (List<?>) content;
		assertEquals(3, blocks.size());

		Map<String, Object> text = asMap(blocks.get(0));
		assertEquals("text", text.get("type"));
		assertTrue(((String) text.get("text")).contains("demo.png"));

		Map<String, Object> image = asMap(blocks.get(1));
		assertEquals("image", image.get("type"));
		Map<String, Object> imageSource = asMap(image.get("source"));
		assertEquals("base64", imageSource.get("type"));
		assertEquals("image/png", imageSource.get("media_type"));
		assertEquals("The data URI prefix must be stripped.", IMAGE_BASE64, imageSource.get("data"));

		Map<String, Object> document = asMap(blocks.get(2));
		assertEquals("document", document.get("type"));
		Map<String, Object> pdfSource = asMap(document.get("source"));
		assertEquals("base64", pdfSource.get("type"));
		assertEquals("application/pdf", pdfSource.get("media_type"));
		assertEquals(PDF_BASE64, pdfSource.get("data"));
	}

	/**
	 * A PDF attached to a user turn - what a caller hands to {@code aiAgentChat} - becomes a native
	 * {@code document} block, so the model reads the document instead of base64 text.
	 */
	public void testPdfInUserMessageIsADocumentBlock() throws Exception {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Extract the price."), new PdfContent(PDF_BASE64, "application/pdf")));

		String json = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
			"claude-sonnet-4-20250514", "You are helpful.", List.<Message> of(user), List.of(), 256));

		Map<String, Object> turn = asMap(messages(json).get(0));
		assertEquals("user", turn.get("role"));

		List<?> blocks = (List<?>) turn.get("content");
		assertEquals(2, blocks.size());
		assertEquals("text", asMap(blocks.get(0)).get("type"));

		Map<String, Object> document = asMap(blocks.get(1));
		assertEquals("document", document.get("type"));
		Map<String, Object> source = asMap(document.get("source"));
		assertEquals("base64", source.get("type"));
		assertEquals("application/pdf", source.get("media_type"));
		assertEquals(PDF_BASE64, source.get("data"));
	}

	/**
	 * Every accepted image type reaches Anthropic with its media type unchanged. The builder states
	 * {@link ImageContent#mediaType()} verbatim, which is why the set of types that may be built is
	 * exactly the set the provider accepts.
	 */
	public void testAcceptedImageTypesReachTheProviderUnchanged() throws Exception {
		for (String mediaType : ImageContent.ACCEPTED_MEDIA_TYPES) {
			UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new ImageContent("data:" + mediaType + ";base64," + IMAGE_BASE64, mediaType)));

			String json = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
				"claude-sonnet-4-20250514", "You are helpful.", List.<Message> of(user), List.of(), 256));

			Map<String, Object> image = asMap(((List<?>) asMap(messages(json).get(0)).get("content")).get(0));
			assertEquals("image", image.get("type"));
			Map<String, Object> source = asMap(image.get("source"));
			assertEquals("base64", source.get("type"));
			assertEquals(mediaType, source.get("media_type"));
			assertEquals(IMAGE_BASE64, source.get("data"));
		}
	}

	/** A text-only tool result still serializes as a plain string, exactly as before. */
	public void testTextOnlyToolResultStaysAString() throws Exception {
		String json = buildJson(List.of());

		Map<String, Object> toolResult = toolResultBlock(messages(json).get(2));
		assertEquals("15 degrees", toolResult.get("content"));
	}

	/** The cache breakpoint stays on the tool_result block, not on its nested content. */
	public void testCacheControlStaysOnTheToolResultBlock() throws Exception {
		String json = buildJson(List.of(
			new TextContent("caption"),
			new ImageContent("data:image/png;base64," + IMAGE_BASE64, "image/png")));

		Map<String, Object> toolResult = toolResultBlock(messages(json).get(2));
		assertNotNull("The tool result is one of the cached turns.", toolResult.get("cache_control"));
		for (Object block : (List<?>) toolResult.get("content")) {
			assertNull("A nested block must not carry its own breakpoint.",
				asMap(block).get("cache_control"));
		}
	}

	/**
	 * Adaptive thinking asks for the summarized display, so a model whose API default is
	 * {@code omitted} (Opus 4.7 and later) streams readable thinking instead of empty blocks.
	 */
	public void testAdaptiveThinkingAsksForTheSummary() throws Exception {
		Map<String, Object> request = thinkingRequest("claude-opus-5-5", Map.of("thinking_effort", "high"));

		Map<String, Object> thinking = asMap(request.get("thinking"));
		assertEquals("adaptive", thinking.get("type"));
		assertEquals("summarized", thinking.get("display"));
		assertEquals("high", asMap(request.get("output_config")).get("effort"));
	}

	/** Budget thinking (Haiku) is left as it was: its display defaults to the summary already. */
	public void testBudgetThinkingIsUnchanged() throws Exception {
		Map<String, Object> request =
			thinkingRequest("claude-haiku-4-5-20251001", Map.of("thinking_effort", "medium"));

		Map<String, Object> thinking = asMap(request.get("thinking"));
		assertEquals("enabled", thinking.get("type"));
		assertNull(thinking.get("display"));
		assertEquals(Long.valueOf(2048), Long.valueOf(((Number) thinking.get("budget_tokens")).longValue()));
	}

	/**
	 * Without an effort, a model that thinks anyway still asks for the summary - the case of an agent
	 * with no configured effort, in which Opus 5.5 thought for minutes with nothing on screen. The
	 * effort is left to the API default.
	 */
	public void testNoEffortStillShowsThinkingOfAModelThatThinksUnasked() throws Exception {
		Map<String, Object> request = thinkingRequest("claude-opus-5-5", Map.of());

		Map<String, Object> thinking = asMap(request.get("thinking"));
		assertEquals("adaptive", thinking.get("type"));
		assertEquals("summarized", thinking.get("display"));
		assertNull(request.get("output_config"));
	}

	/** Without an effort, a model that only thinks when asked is not switched to thinking. */
	public void testNoEffortLeavesOtherModelsWithoutThinking() throws Exception {
		Map<String, Object> request = thinkingRequest("claude-sonnet-4-6", Map.of());

		assertNull(request.get("thinking"));
		assertNull(request.get("output_config"));
	}

	/** Which model names count as thinking without being asked. */
	public void testThinksUnasked() {
		for (String model : List.of("claude-opus-5-5", "claude-opus-5", "claude-sonnet-5-5", "claude-sonnet-5",
			"claude-fable-5", "claude-fable-5-1", "claude-mythos-5-1", "anthropic.claude-opus-5-5")) {
			assertTrue(model, AnthropicRequestBuilder.thinksUnasked(model));
		}
		for (String model : List.of("claude-opus-4-8", "claude-opus-4-7", "claude-opus-4-6", "claude-sonnet-4-6",
			"claude-opus-4-5-20251101", "claude-sonnet-4-5-20250929", "claude-haiku-4-5-20251001")) {
			assertFalse(model, AnthropicRequestBuilder.thinksUnasked(model));
		}
	}

	private static Map<String, Object> thinkingRequest(String model, Map<String, Object> options)
			throws Exception {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Think about it.")));
		String json = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
			model, "You are helpful.", List.<Message> of(user), List.of(), 4096, options));
		return asMap(JsonUtil.parseJsonValue(json));
	}

	private static String buildJson(List<ContentPart> toolResultContent) throws Exception {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Show me the screenshot.")));

		AssistantMessage assistant = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new ToolCall(CALL_ID, "viewDocument", "{\"tid\":\"Screenshot:42\"}")),
			new TokenUsage(10, 20, 0, 0, 0), "tool_use");

		ToolResultMessage result = new ToolResultMessage(UUID.randomUUID().toString(), Instant.now(),
			CALL_ID, "viewDocument",
			new ToolState.Completed("15 degrees", Instant.now(), Instant.now(), false, null, -1,
				toolResultContent));

		List<Message> messages = List.of(user, assistant, result);
		return AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
			"claude-sonnet-4-20250514", "You are helpful.", messages, List.of(), 256));
	}

	private static List<Object> messages(String json) throws Exception {
		@SuppressWarnings("unchecked")
		List<Object> messages = (List<Object>) asMap(JsonUtil.parseJsonValue(json)).get("messages");
		assertNotNull(messages);
		return messages;
	}

	private static Map<String, Object> toolResultBlock(Object message) {
		Map<String, Object> entry = asMap(message);
		assertEquals("Tool results ride in a user turn.", "user", entry.get("role"));
		Map<String, Object> block = asMap(((List<?>) entry.get("content")).get(0));
		assertEquals("tool_result", block.get("type"));
		assertEquals(CALL_ID, block.get("tool_use_id"));
		return block;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		assertTrue("Expected a JSON object, got: " + value, value instanceof Map);
		return (Map<String, Object>) value;
	}
}

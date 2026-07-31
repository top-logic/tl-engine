/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
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
 * Tests how {@link OpenAIResponsesRequestBuilder} puts a multimodal tool result on the wire.
 *
 * <p>The {@code output} of a {@code function_call_output} accepts either a string or an array of
 * {@code input_text}/{@code input_image}/{@code input_file} parts, which is the same vocabulary a
 * user message uses.</p>
 */
public class TestOpenAIResponsesRequestBuilder extends TestCase {

	private static final String CALL_ID = "call_abc123";

	private static final String IMAGE_URL = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==";

	private static final String PDF_BASE64 = "JVBERi0xLjcKZmFrZQ==";

	/** An image and a PDF a tool returned become an output array, not a stringified placeholder. */
	public void testMultimodalToolResultIsNative() throws Exception {
		List<Object> input = input(buildJson(List.of(
			new TextContent("Image content of \"demo.png\" (image/png)."),
			new ImageContent(IMAGE_URL, "image/png"),
			new PdfContent(PDF_BASE64, "application/pdf"))));

		assertEquals("The tool result must not be followed by an extra item.", 3, input.size());

		Map<String, Object> item = asMap(input.get(2));
		assertEquals("function_call_output", item.get("type"));
		assertEquals(CALL_ID, item.get("call_id"));

		Object output = item.get("output");
		assertTrue("Expected an array of content parts, got: " + output, output instanceof List);
		List<?> parts = (List<?>) output;
		assertEquals(3, parts.size());

		Map<String, Object> text = asMap(parts.get(0));
		assertEquals("input_text", text.get("type"));
		assertTrue(((String) text.get("text")).contains("demo.png"));

		Map<String, Object> image = asMap(parts.get(1));
		assertEquals("input_image", image.get("type"));
		assertEquals(IMAGE_URL, image.get("image_url"));

		Map<String, Object> file = asMap(parts.get(2));
		assertEquals("input_file", file.get("type"));
		assertEquals("document.pdf", file.get("filename"));
		assertEquals("data:application/pdf;base64," + PDF_BASE64, file.get("file_data"));
	}

	/** A text-only tool result still serializes as a plain string. */
	public void testTextOnlyToolResultStaysAString() throws Exception {
		Map<String, Object> item = asMap(input(buildJson(List.of())).get(2));
		assertEquals("15 degrees", item.get("output"));
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

		return OpenAIResponsesRequestBuilder.buildRequestJson(new ProviderRequest(
			"gpt-5", "You are helpful.", List.of(user, assistant, result), List.of(), 256));
	}

	private static List<Object> input(String json) throws Exception {
		@SuppressWarnings("unchecked")
		List<Object> input = (List<Object>) asMap(JsonUtil.parseJsonValue(json)).get("input");
		assertNotNull(input);
		return input;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		assertTrue("Expected a JSON object, got: " + value, value instanceof Map);
		return (Map<String, Object>) value;
	}
}

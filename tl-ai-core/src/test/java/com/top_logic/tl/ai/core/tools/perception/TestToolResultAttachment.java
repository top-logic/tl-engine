/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;
import com.top_logic.tl.ai.core.provider.bedrock.BedrockConverseRequestBuilder;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsRequestBuilder;
import com.top_logic.tl.ai.core.provider.openai.OpenAIResponsesRequestBuilder;
import com.top_logic.tl.ai.core.tools.ToolContents;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * End-to-end test of the {@code viewDocument} delivery path: stored bytes become a downscaled image
 * inside the tool's own {@link ToolResult}, that result becomes a
 * {@link com.top_logic.tl.ai.core.message.ToolState.Completed}, and every provider request builder
 * puts it on the wire the way its API accepts it.
 *
 * <p>
 * Covers everything except knowledge-base access, which the tool performs before this path starts.
 * </p>
 */
public class TestToolResultAttachment extends TestCase {

	private static final String CALL_ID = "toolu_1";

	/** A screenshot a tool captured reaches the model inside the tool result itself. */
	public void testStoredImageBecomesToolResultContent() throws Exception {
		byte[] storedPng = screenshotBytes(2400, 1400);

		// 1. What the tool does: turn the stored bytes into its own multimodal result.
		BinaryPerception.Plan plan = BinaryPerception.plan("\"demo.png\" (Screenshot:42.image)",
			storedPng, "image/png", visionModel());
		assertTrue("A vision model must receive the image.", plan.attaches());
		ToolResult result = ToolResult.multimodal(plan.message(), plan.attachment());

		// 2. What the tool catalog does: carry the parts into the conversation.
		List<ContentPart> parts = ToolContents.toContentParts(result.content());
		assertEquals(2, parts.size());
		assertTrue(parts.get(0) instanceof TextContent);
		assertTrue("The caption names the source.",
			((TextContent) parts.get(0)).text().contains("Screenshot:42.image"));
		ImageContent image = (ImageContent) parts.get(1);
		assertEquals("image/png", image.mediaType());
		assertTrue("Content parts carry a data URI.", image.url().startsWith("data:image/png;base64,"));

		// 3. The Anthropic builder emits it as a base64 image block inside the tool_result.
		String json = AnthropicRequestBuilder.buildRequestJson(request(parts));
		assertTrue("Expected a tool_result block.", json.contains("\"type\":\"tool_result\""));
		assertTrue("Expected an image block.", json.contains("\"type\":\"image\""));
		assertTrue("Expected a base64 source.", json.contains("\"type\":\"base64\""));
		assertTrue("Expected the PNG media type.", json.contains("\"media_type\":\"image/png\""));
		assertTrue("Expected the caption alongside the image.",
			json.contains("Screenshot:42.image"));
		assertTrue("Expected the image payload itself.",
			json.contains(image.url().substring(image.url().indexOf(',') + 1)));
	}

	/** Every builder accepts a multimodal tool result; only Chat Completions needs the fallback. */
	public void testAllProvidersRenderTheResult() throws Exception {
		List<ContentPart> parts = List.of(
			new TextContent("Image content of \"demo.png\" (image/png)"),
			new ImageContent("data:image/png;base64,QQ==", "image/png"));
		ProviderRequest request = request(parts);

		String anthropic = AnthropicRequestBuilder.buildRequestJson(request);
		assertTrue(anthropic.contains("\"type\":\"image\""));

		String responses = OpenAIResponsesRequestBuilder.buildRequestJson(request);
		assertTrue(responses.contains("\"type\":\"input_image\""));

		String bedrock = BedrockConverseRequestBuilder.buildRequestJson(request);
		assertTrue(bedrock.contains("\"image\""));
		assertTrue(bedrock.contains("\"bytes\":\"QQ==\""));

		String chatCompletions = ChatCompletionsRequestBuilder.buildRequestJson(request);
		assertTrue("Chat Completions carries the image in a follow-up user message.",
			chatCompletions.contains("\"type\":\"image_url\""));
	}

	/** A text-only model is told what the object is instead of being sent an image it cannot read. */
	public void testTextOnlyModelGetsNoImage() throws IOException {
		ModelPerception textOnly = new ModelPerception("text-only-model",
			modelInfo(new ModelModalities(List.of("text"), List.of("text")), false));
		assertFalse(textOnly.support(Modality.IMAGE).permitted());

		BinaryPerception.Plan plan =
			BinaryPerception.plan("shot.png", screenshotBytes(200, 100), "image/png", textOnly);

		assertFalse("A text-only model must not receive an image.", plan.attaches());
		ToolResult result = ToolResult.of(plan.message());
		assertTrue("The result must stay text-only.", result.content().isEmpty());
		assertTrue(result.text().contains("cannot view images"));
		assertTrue("The model must still learn what the object is.",
			result.text().contains("shot.png"));
	}

	/** user &#x2192; assistant(toolUse) &#x2192; the tool result carrying the given parts. */
	private static ProviderRequest request(List<ContentPart> parts) {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Show me the screenshot.")));

		AssistantMessage assistant = new AssistantMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new ToolCall(CALL_ID, "viewDocument", "{\"tid\":\"Screenshot:42\"}")),
			new TokenUsage(10, 20, 0, 0, 0), "tool_use");

		ToolResultMessage result = new ToolResultMessage(UUID.randomUUID().toString(), Instant.now(),
			CALL_ID, "viewDocument",
			new ToolState.Completed(text(parts), Instant.now(), Instant.now(), false, null, -1, parts));

		List<Message> messages = List.of(user, assistant, result);
		return new ProviderRequest("claude-sonnet-4-20250514", "You are helpful.", messages,
			List.of(), 256);
	}

	/** The text-only projection of the parts, as {@code ToolResult.multimodal} produces it. */
	private static String text(List<ContentPart> parts) {
		StringBuilder text = new StringBuilder();
		for (ContentPart part : parts) {
			if (part instanceof TextContent content) {
				text.append(content.text());
			}
		}
		return text.toString();
	}

	private static ModelPerception visionModel() {
		return new ModelPerception("claude-sonnet-4-20250514",
			modelInfo(new ModelModalities(List.of("text", "image", "pdf"), List.of("text")), true));
	}

	private static ModelInfo modelInfo(ModelModalities modalities, boolean attachment) {
		return new ModelInfo("test-model", null, null,
			new ModelCapabilities(false, true, true, attachment, false, modalities, null),
			null, null, false, null);
	}

	/** A PNG large enough that the downscaler has to act, as a real screenshot would be. */
	private static byte[] screenshotBytes(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			graphics.setColor(Color.BLUE);
			graphics.fillRect(width / 4, height / 4, width / 2, height / 2);
		} finally {
			graphics.dispose();
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buffer);
		return buffer.toByteArray();
	}
}

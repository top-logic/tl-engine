/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.perception.BinaryPerception;
import com.top_logic.tl.ai.core.agent.perception.Modality;
import com.top_logic.tl.ai.core.agent.perception.PerceivedContent;
import com.top_logic.tl.ai.core.agent.perception.PerceptionBinding;
import com.top_logic.tl.ai.core.agent.perception.PerceptionContext;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities;

/**
 * End-to-end test of the {@code viewDocument} injection seam: stored bytes become a downscaled
 * {@link ImageContent}, the agent loop appends it to the conversation at turn start, and the
 * provider request builder emits it on the wire.
 *
 * <p>
 * Covers everything except knowledge-base access, which the tool performs before this path starts.
 * </p>
 */
public class TestPerceptionInjection extends TestCase {

	/** A screenshot a tool captured is placed into the conversation and reaches the provider. */
	public void testStoredImageReachesTheProviderRequest() throws Exception {
		byte[] storedPng = screenshotBytes(2400, 1400);
		Conversation conversation = new Conversation();
		PerceptionContext perception = visionModel();

		try (PerceptionBinding.Binding binding = PerceptionBinding.bind(perception)) {
			// 1. What the tool does: turn the stored bytes into perceivable content.
			BinaryPerception.Plan plan = BinaryPerception.plan("\"demo.png\" (Screenshot:42.image)",
				storedPng, "image/png", PerceptionBinding.getContext());
			assertTrue("A vision model must receive the image.", plan.injects());
			PerceptionBinding.getContext().offer(plan.perceived());

			// 2. What the agent loop does at the start of the next turn.
			AgentLoop.injectPerceivedContentForTest(conversation);
		}

		// 3. The conversation now carries a user message with caption + image.
		assertEquals("Exactly one message must have been injected.", 1, conversation.size());
		Message message = conversation.messages().get(0);
		assertTrue("Images ride in user messages, like chat attachments.", message instanceof UserMessage);
		UserMessage injected = (UserMessage) message;
		assertEquals(2, injected.content().size());
		assertTrue(injected.content().get(0) instanceof TextContent);
		assertTrue(((TextContent) injected.content().get(0)).text().contains("Screenshot:42.image"));
		ImageContent image = (ImageContent) injected.content().get(1);
		assertEquals("image/png", image.mediaType());

		// 4. The provider request builder puts it on the wire as a base64 image block.
		String json = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
			"claude-sonnet-4-20250514", "You are helpful.", conversation.messages(), List.of(), 256));
		assertTrue("Expected an image block in the request.", json.contains("\"type\":\"image\""));
		assertTrue("Expected a base64 source.", json.contains("\"type\":\"base64\""));
		assertTrue("Expected the PNG media type.", json.contains("\"media_type\":\"image/png\""));
		assertTrue("Expected the caption alongside the image.", json.contains("Screenshot:42.image"));

		String base64 = image.url().substring(image.url().indexOf(',') + 1);
		assertTrue("Expected the image payload itself.", json.contains(base64));
	}

	/** The queue is drained, so a second turn does not re-send the same image. */
	public void testContentIsDeliveredExactlyOnce() throws IOException {
		Conversation conversation = new Conversation();

		try (PerceptionBinding.Binding binding = PerceptionBinding.bind(visionModel())) {
			PerceptionBinding.getContext().offer(new PerceivedContent("Look:",
				new ImageContent("data:image/png;base64,QQ==", "image/png")));

			AgentLoop.injectPerceivedContentForTest(conversation);
			AgentLoop.injectPerceivedContentForTest(conversation);
		}

		assertEquals("The queue must be drained on the first turn.", 1, conversation.size());
	}

	/** Several offers from one turn arrive together, in offer order. */
	public void testMultipleOffersShareOneMessage() {
		Conversation conversation = new Conversation();

		try (PerceptionBinding.Binding binding = PerceptionBinding.bind(visionModel())) {
			PerceptionContext perception = PerceptionBinding.getContext();
			perception.offer(new PerceivedContent("First:",
				new ImageContent("data:image/png;base64,QQ==", "image/png")));
			perception.offer(new PerceivedContent("Second:",
				new ImageContent("data:image/png;base64,Qg==", "image/png")));

			AgentLoop.injectPerceivedContentForTest(conversation);
		}

		assertEquals(1, conversation.size());
		UserMessage injected = (UserMessage) conversation.messages().get(0);
		assertEquals(4, injected.content().size());
		assertEquals("First:", ((TextContent) injected.content().get(0)).text());
		assertEquals("Second:", ((TextContent) injected.content().get(2)).text());
	}

	/** Outside an agent run nothing is bound and injection is a no-op. */
	public void testNoBindingIsANoOp() {
		Conversation conversation = new Conversation();

		AgentLoop.injectPerceivedContentForTest(conversation);

		assertEquals(0, conversation.size());
	}

	/** A text-only model never gets an image, and the run is not disturbed by that. */
	public void testTextOnlyModelReceivesNoImage() throws IOException {
		Conversation conversation = new Conversation();
		PerceptionContext textOnly = new PerceptionContext("text-only-model",
			modelInfo(new ModelModalities(List.of("text"), List.of("text")), false));
		assertFalse(textOnly.support(Modality.IMAGE).permitted());

		try (PerceptionBinding.Binding binding = PerceptionBinding.bind(textOnly)) {
			BinaryPerception.Plan plan =
				BinaryPerception.plan("shot.png", screenshotBytes(200, 100), "image/png", textOnly);
			assertFalse(plan.injects());

			AgentLoop.injectPerceivedContentForTest(conversation);
		}

		assertEquals("Nothing may be injected for a text-only model.", 0, conversation.size());
	}

	private static PerceptionContext visionModel() {
		return new PerceptionContext("claude-sonnet-4-20250514",
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

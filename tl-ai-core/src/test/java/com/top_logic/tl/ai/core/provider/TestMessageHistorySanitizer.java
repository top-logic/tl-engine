/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Instant;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;

/**
 * What a provider may be given back of its own and another's thinking.
 *
 * <p>
 * The regression behind a chat that died mid-conversation: an assistant turn carrying a thinking
 * block was replayed to Anthropic without the block's signature, the API answered
 * {@code messages.1.content.1.thinking.signature: Field required}, and because the block stayed in
 * memory every later send failed the same way.
 * </p>
 */
public class TestMessageHistorySanitizer extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-13T10:00:00Z");

	/** The history the user's report produced: a turn with thinking, a tool call and its result. */
	private static List<Message> history(ThinkingContent thinking) {
		return List.of(
			new UserMessage("u1", T0, List.of(new TextContent("Wer bist du? Denk nach."))),
			new AssistantMessage("a1", T0, List.of(
				thinking,
				new TextContent("Ich sehe nach."),
				new ToolCall("tc1", "outlineModel", "{}")), TokenUsage.EMPTY, "tool_use"),
			new ToolResultMessage("tr1", T0, "tc1", "outlineModel", new ToolState.Completed("ok", T0, T0, false)),
			new UserMessage("u2", T0, List.of(new TextContent("Und jetzt das Modell erkunden."))));
	}

	/** Anthropic gets its own signed thinking back untouched. */
	public void testSignedThinkingSurvivesToItsOwnProvider() {
		ThinkingContent signed = new ThinkingContent("Überlegung", "sig-abc", "anthropic");
		List<Message> sent = MessageHistorySanitizer.forProvider(history(signed), "anthropic");

		assertEquals(4, sent.size());
		assertEquals(List.of(signed), thinkingOf(sent));
	}

	/**
	 * A thinking block without its signature is dropped rather than sent.
	 *
	 * <p>
	 * Anthropic requires the signature and refuses the whole request without it. The block is the
	 * model's own scratchpad, so dropping it costs continuity of reasoning; sending it costs the
	 * conversation.
	 * </p>
	 */
	public void testUnsignedThinkingIsDropped() {
		List<Message> sent =
			MessageHistorySanitizer.forProvider(history(new ThinkingContent("Überlegung", null, "anthropic")),
				"anthropic");

		assertEquals("Only the thinking block goes; the turn keeps its text and tool call.", 4, sent.size());
		assertEquals(List.of(), thinkingOf(sent));
		assertEquals(2, ((AssistantMessage) sent.get(1)).content().size());
	}

	/** A block whose origin was never recorded could come from anywhere, so it is not replayed. */
	public void testThinkingOfUnknownOriginIsDropped() {
		List<Message> sent =
			MessageHistorySanitizer.forProvider(history(new ThinkingContent("Überlegung", "sig-abc", null)),
				"anthropic");

		assertEquals(List.of(), thinkingOf(sent));
	}

	/**
	 * Switching the model to another provider mid-session must not break the conversation.
	 *
	 * <p>
	 * An Anthropic signature means nothing to {@code OpenAI} and the other way round, so each
	 * provider only ever sees thinking it minted itself.
	 * </p>
	 */
	public void testThinkingIsStrippedAcrossProviders() {
		List<Message> anthropicHistory = history(new ThinkingContent("Überlegung", "sig-abc", "anthropic"));
		assertEquals(List.of(), thinkingOf(MessageHistorySanitizer.forProvider(anthropicHistory, "openai")));
		assertEquals(List.of(), thinkingOf(MessageHistorySanitizer.forProvider(anthropicHistory, "xai")));

		List<Message> openAiHistory = history(new ThinkingContent("Reasoning", "enc-xyz", "openai"));
		assertEquals(List.of(), thinkingOf(MessageHistorySanitizer.forProvider(openAiHistory, "anthropic")));
		assertEquals(1, thinkingOf(MessageHistorySanitizer.forProvider(openAiHistory, "openai")).size());
	}

	/** An assistant turn left with nothing to say goes with its content: providers refuse an empty one. */
	public void testAssistantTurnOfOnlyThinkingDisappears() {
		List<Message> sent = MessageHistorySanitizer.forProvider(List.of(
			new UserMessage("u1", T0, List.of(new TextContent("Denk nach."))),
			new AssistantMessage("a1", T0, List.of(new ThinkingContent("still", "sig", "anthropic")),
				TokenUsage.EMPTY, "stop")),
			"openai");

		assertEquals(1, sent.size());
		assertTrue(sent.get(0) instanceof UserMessage);
	}

	/** Nothing to remove means nothing is copied. */
	public void testCleanHistoryIsReturnedUnchanged() {
		List<Message> clean = List.of(
			new UserMessage("u1", T0, List.of(new TextContent("Hallo"))),
			new AssistantMessage("a1", T0, List.of(new TextContent("Hallo!")), TokenUsage.EMPTY, "stop"));

		assertSame(clean, MessageHistorySanitizer.forProvider(clean, "anthropic"));
	}

	/**
	 * The end of the chain: what the Anthropic request builder actually writes.
	 *
	 * <p>
	 * Every {@code thinking} block in the body must carry a {@code signature}; that is the exact
	 * condition the API checked when it rejected the conversation.
	 * </p>
	 */
	public void testEveryThinkingBlockInTheRequestIsSigned() {
		for (ThinkingContent thinking : List.of(
			new ThinkingContent("Überlegung", "sig-abc", "anthropic"),
			new ThinkingContent("Überlegung", null, "anthropic"),
			new ThinkingContent("Überlegung", "enc-xyz", "openai"))) {
			List<Message> sent = MessageHistorySanitizer.forProvider(history(thinking), "anthropic");
			String body;
			try {
				body = AnthropicRequestBuilder.buildRequestJson(new ProviderRequest(
					"claude-sonnet-4-6", "system", sent, List.of(), 1024));
			} catch (ProviderException ex) {
				throw new AssertionError("The request could not be built.", ex);
			}

			int block = body.indexOf("\"type\":\"thinking\"");
			while (block >= 0) {
				int next = body.indexOf("\"type\":", block + 1);
				String slice = next < 0 ? body.substring(block) : body.substring(block, next);
				assertTrue("A thinking block reached the request without a signature: " + slice,
					slice.contains("\"signature\""));
				block = body.indexOf("\"type\":\"thinking\"", block + 1);
			}
		}
	}

	private static List<ThinkingContent> thinkingOf(List<Message> messages) {
		return messages.stream()
			.filter(AssistantMessage.class::isInstance)
			.map(AssistantMessage.class::cast)
			.flatMap(message -> message.content().stream())
			.filter(ThinkingContent.class::isInstance)
			.map(ThinkingContent.class::cast)
			.toList();
	}

}

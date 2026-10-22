/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.ThinkingContent;

/**
 * Removes from a conversation what the provider about to receive it cannot accept.
 *
 * <p>
 * A thinking block is not portable. Anthropic mints a signature for every one it emits and refuses
 * the block back without it ({@code messages.N.content.M.thinking.signature: Field required}), while
 * {@code OpenAI} mints an {@code encrypted_content} of its own that means nothing to Anthropic.
 * Replaying such a block to the wrong provider - or to the right one without its token - fails the
 * request, and because the block stays in memory it fails every request after it: the conversation
 * is dead from that point on.
 * </p>
 *
 * <p>
 * Blocks are therefore dropped rather than repaired. Thinking is the model's own scratchpad; losing
 * it from the replayed history costs continuity of reasoning, which is a far smaller price than a
 * conversation that can no longer be continued.
 * </p>
 *
 * @see ThinkingContent#provider()
 */
public final class MessageHistorySanitizer {

	private MessageHistorySanitizer() {
		// Static utility.
	}

	/**
	 * The conversation as the given provider may receive it.
	 *
	 * <p>
	 * Returns the input unchanged when nothing has to go, so the common case allocates nothing.
	 * </p>
	 *
	 * @param messages
	 *        the conversation to send
	 * @param providerId
	 *        catalog ID of the provider that will receive it, e.g. {@code "anthropic"}
	 * @return a conversation the provider accepts
	 */
	public static List<Message> forProvider(List<Message> messages, String providerId) {
		for (Message message : messages) {
			if (message instanceof AssistantMessage assistant && needsSanitizing(assistant, providerId)) {
				return sanitize(messages, providerId);
			}
		}
		return messages;
	}

	/**
	 * Whether this block may be replayed to the given provider.
	 *
	 * <p>
	 * Two conditions, and both are necessary: the block must come from that same provider, and it
	 * must carry the token that provider issued for it. An unknown origin counts as foreign - a
	 * block recorded before origins were tracked could have come from anywhere.
	 * </p>
	 */
	public static boolean isReplayable(ThinkingContent thinking, String providerId) {
		if (thinking.continuityToken() == null || thinking.continuityToken().isBlank()) {
			return false;
		}
		String origin = thinking.provider();
		return origin != null && providerId != null
			&& origin.toLowerCase(Locale.ROOT).equals(providerId.toLowerCase(Locale.ROOT));
	}

	private static boolean needsSanitizing(AssistantMessage message, String providerId) {
		for (ContentPart part : message.content()) {
			if (part instanceof ThinkingContent thinking && !isReplayable(thinking, providerId)) {
				return true;
			}
		}
		return false;
	}

	private static List<Message> sanitize(List<Message> messages, String providerId) {
		List<Message> result = new ArrayList<>(messages.size());
		for (Message message : messages) {
			if (!(message instanceof AssistantMessage assistant)) {
				result.add(message);
				continue;
			}
			List<ContentPart> kept = new ArrayList<>(assistant.content().size());
			for (ContentPart part : assistant.content()) {
				if (part instanceof ThinkingContent thinking && !isReplayable(thinking, providerId)) {
					continue;
				}
				kept.add(part);
			}
			// An assistant turn whose only content was unusable thinking has nothing left to say;
			// the providers reject an empty assistant message, so it goes with its content.
			if (kept.isEmpty()) {
				continue;
			}
			result.add(new AssistantMessage(assistant.id(), assistant.timestamp(), kept,
				assistant.usage(), assistant.finishReason()));
		}
		return result;
	}
}

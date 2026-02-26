/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * {@link ChatModel} wrapper that intercepts the first LLM request and injects multimodal
 * attachment content (images, PDFs) into the last {@link UserMessage}.
 *
 * <p>
 * This is needed because LangChain4j's {@code DefaultAiServices} assembles the request messages
 * separately from what is stored in {@code ChatMemory}: the plain-text {@link UserMessage} is added
 * to the request list directly, bypassing any memory wrapper. This wrapper intercepts the actual
 * {@link ChatModel#chat(ChatRequest)} call to ensure the LLM sees the multimodal content.
 * </p>
 *
 * <p>
 * Self-disarms after the first call so that subsequent calls in the tool execution loop (which
 * re-read messages from memory) are not affected.
 * </p>
 */
class MultimodalChatModelWrapper implements ChatModel {

	private final ChatModel _delegate;

	private List<Content> _attachmentContents;

	MultimodalChatModelWrapper(ChatModel delegate, List<Content> attachmentContents) {
		_delegate = delegate;
		_attachmentContents = attachmentContents;
	}

	@Override
	public ChatResponse chat(ChatRequest chatRequest) {
		if (_attachmentContents != null) {
			List<ChatMessage> messages = new ArrayList<>(chatRequest.messages());
			// Find the last UserMessage and enrich it with multimodal content
			for (int i = messages.size() - 1; i >= 0; i--) {
				if (messages.get(i) instanceof UserMessage um) {
					List<Content> contents = new ArrayList<>(um.contents());
					contents.addAll(_attachmentContents);
					messages.set(i, UserMessage.from(contents));
					break;
				}
			}
			_attachmentContents = null; // Only enrich the first call
			return _delegate.chat(chatRequest.toBuilder().messages(messages).build());
		}
		return _delegate.chat(chatRequest);
	}
}

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
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;

/**
 * {@link ChatMemory} wrapper that intercepts the next {@link UserMessage} add and replaces it with
 * a multimodal message containing text + attachment content (images, PDFs).
 *
 * <p>
 * Used when the user sends a message with file attachments. LangChain4j's agent auto-adds a plain
 * text {@link UserMessage} to {@link ChatMemory} during {@code invoke()}. This wrapper intercepts
 * that add and enriches it with multimodal content.
 * </p>
 */
class MultimodalChatMemoryWrapper implements ChatMemory {

	private final ChatMemory _delegate;

	private List<Content> _attachmentContents;

	MultimodalChatMemoryWrapper(ChatMemory delegate, List<Content> attachmentContents) {
		_delegate = delegate;
		_attachmentContents = attachmentContents;
	}

	@Override
	public Object id() {
		return _delegate.id();
	}

	@Override
	public void add(ChatMessage message) {
		if (_attachmentContents != null && message instanceof UserMessage userMsg) {
			// Replace plain text UserMessage with multimodal version
			List<Content> contents = new ArrayList<>();
			contents.add(new TextContent(userMsg.singleText()));
			contents.addAll(_attachmentContents);
			_delegate.add(UserMessage.from(contents));
			_attachmentContents = null; // Only intercept once
		} else {
			_delegate.add(message);
		}
	}

	@Override
	public List<ChatMessage> messages() {
		return _delegate.messages();
	}

	@Override
	public void clear() {
		_delegate.clear();
	}
}

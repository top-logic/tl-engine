/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.tool.api.ToolContent;

/**
 * Translates the tool contract's {@link ToolContent} into the conversation's {@link ContentPart}.
 *
 * <p>
 * The two vocabularies are deliberately separate: {@code tl-ai-tool-api} must not depend on the
 * conversation model, and a tool has no business producing tool calls or thinking blocks. This is
 * the single place where the tool-side parts enter the conversation.
 * </p>
 */
public final class ToolContents {

	private ToolContents() {
		// Static utility.
	}

	/**
	 * Converts the parts a tool returned into conversation content parts, preserving order.
	 */
	public static List<ContentPart> toContentParts(List<ToolContent> content) {
		if (content == null || content.isEmpty()) {
			return List.of();
		}
		List<ContentPart> result = new ArrayList<>(content.size());
		for (ToolContent part : content) {
			result.add(toContentPart(part));
		}
		return result;
	}

	private static ContentPart toContentPart(ToolContent part) {
		if (part instanceof ToolContent.Text text) {
			return new TextContent(text.text());
		}
		if (part instanceof ToolContent.Image image) {
			return new ImageContent("data:" + image.mediaType() + ";base64," + image.data(),
				image.mediaType());
		}
		if (part instanceof ToolContent.Document document) {
			return new PdfContent(document.data(), document.mediaType());
		}
		throw new IllegalArgumentException("Unknown tool content type: " + part.getClass());
	}
}

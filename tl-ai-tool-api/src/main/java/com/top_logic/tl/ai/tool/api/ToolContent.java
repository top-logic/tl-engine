/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.Objects;

/**
 * A piece of content a tool hands back to the model as part of its {@link ToolResult}.
 *
 * <p>
 * Deliberately smaller than the conversation-level content vocabulary of {@code tl-ai-core}: a tool
 * produces text, images and documents, never tool calls or thinking blocks. The request builders map
 * these to whatever the target provider accepts inside a tool result.
 * </p>
 *
 * @see ToolResult#content()
 */
public sealed interface ToolContent
		permits ToolContent.Text, ToolContent.Image, ToolContent.Document {

	/**
	 * Text the model reads.
	 *
	 * @param text
	 *        The text, never {@code null}.
	 */
	record Text(String text) implements ToolContent {

		/** */
		public Text {
			Objects.requireNonNull(text, "Text must not be null.");
		}
	}

	/**
	 * A raster image the model looks at.
	 *
	 * @param data
	 *        Base64-encoded image bytes, without any {@code data:} URI prefix.
	 * @param mediaType
	 *        The image's media type, e.g. {@code "image/png"}.
	 */
	record Image(String data, String mediaType) implements ToolContent {

		/** */
		public Image {
			Objects.requireNonNull(data, "Image data must not be null.");
			Objects.requireNonNull(mediaType, "Media type must not be null.");
			if (data.isEmpty()) {
				throw new IllegalArgumentException("Image data must not be empty.");
			}
		}
	}

	/**
	 * A document the model reads, currently always a PDF.
	 *
	 * @param data
	 *        Base64-encoded document bytes, without any {@code data:} URI prefix.
	 * @param mediaType
	 *        The document's media type; defaults to {@code "application/pdf"} when {@code null}.
	 */
	record Document(String data, String mediaType) implements ToolContent {

		/** */
		public Document {
			Objects.requireNonNull(data, "Document data must not be null.");
			if (data.isEmpty()) {
				throw new IllegalArgumentException("Document data must not be empty.");
			}
			if (mediaType == null) {
				mediaType = "application/pdf";
			}
		}
	}
}

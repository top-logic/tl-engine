/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * A PDF document as part of a message.
 *
 * @param data      base64-encoded PDF data
 * @param mediaType the media type — defaults to {@code "application/pdf"} when {@code null}
 *
 * @see ContentPart
 */
public record PdfContent(String data, String mediaType) implements ContentPart {

	/**
	 * Creates a {@link PdfContent}.
	 */
	public PdfContent {
		Objects.requireNonNull(data, "PDF data must not be null.");
		if (data.isEmpty()) {
			throw new IllegalArgumentException("PDF data must not be empty.");
		}
		if (mediaType == null) {
			mediaType = "application/pdf";
		}
	}
}

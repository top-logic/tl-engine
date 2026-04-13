/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

/**
 * A PDF document as part of a message.
 *
 * @param data      base64-encoded PDF data
 * @param mediaType the media type, typically "application/pdf"
 *
 * @see ContentPart
 */
public record PdfContent(String data, String mediaType) implements ContentPart {

	/** */
	public PdfContent {
		if (data == null || data.isEmpty()) {
			throw new IllegalArgumentException("PDF data must not be null or empty.");
		}
		if (mediaType == null) {
			mediaType = "application/pdf";
		}
	}
}

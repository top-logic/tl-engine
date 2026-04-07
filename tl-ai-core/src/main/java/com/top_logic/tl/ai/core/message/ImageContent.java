/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Objects;

/**
 * An image content block within a message.
 *
 * @param url       the image URL or data URI
 * @param mediaType the MIME type (e.g. "image/png")
 *
 * @see ContentPart
 */
public record ImageContent(String url, String mediaType) implements ContentPart {

	/**
	 *
	 */
	public ImageContent {
		Objects.requireNonNull(url, "Image URL must not be null.");
		Objects.requireNonNull(mediaType, "Media type must not be null.");
	}
}

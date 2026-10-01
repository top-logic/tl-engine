/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.time.Instant;

/**
 * Description of a blob stored in a {@link BlobStore}, as delivered by {@link BlobStore#list()}.
 *
 * @param key
 *        The key of the blob in its {@link BlobStore}.
 * @param size
 *        The size of the content in bytes.
 * @param lastModified
 *        The time the blob was written to its {@link BlobStore}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record BlobInfo(String key, long size, Instant lastModified) {
	// Pure data.
}

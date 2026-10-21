/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import com.top_logic.basic.io.binary.BinaryDataSource;

/**
 * An image.
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param data
 *        The image data, e.g. a PNG, JPEG or SVG image.
 * @param fit
 *        How the image is fitted into its bounds.
 */
public record ImageSpec(Bounds bounds, BinaryDataSource data, ImageFit fit) implements ElementSpec {
	// Pure data.
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * A geometric shape, optionally with a text.
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param geometry
 *        The outline geometry.
 * @param fill
 *        The fill, <code>null</code> for no fill.
 * @param line
 *        The outline, <code>null</code> for no outline.
 * @param shadow
 *        The outer shadow, <code>null</code> for no shadow.
 * @param text
 *        The text in the shape, <code>null</code> for no text.
 */
public record ShapeSpec(Bounds bounds, GeometrySpec geometry, PaintSpec fill, LineSpec line, ShadowSpec shadow,
		TextSpec text) implements ElementSpec {
	// Pure data.
}

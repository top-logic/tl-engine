/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * A geometric shape, optionally with a text.
 *
 * <p>
 * A shape may refer to a {@link #style() style} of the template profile, which gives defaults for
 * the properties of the shape, see {@link StyleSpec#applyTo(ShapeSpec)}.
 * </p>
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param geometry
 *        The outline geometry, <code>null</code> for the geometry of the style, or a rectangle.
 * @param fill
 *        The fill, <code>null</code> for no fill.
 * @param line
 *        The outline, <code>null</code> for no outline.
 * @param shadow
 *        The outer shadow, <code>null</code> for no shadow.
 * @param text
 *        The text in the shape, <code>null</code> for no text.
 * @param style
 *        The name of the style of the template profile giving defaults for the properties of the
 *        shape, <code>null</code> for none.
 * @param role
 *        See {@link ElementSpec#role()}.
 */
public record ShapeSpec(Bounds bounds, GeometrySpec geometry, PaintSpec fill, LineSpec line, ShadowSpec shadow,
		TextSpec text, String style, String role) implements ElementSpec {

	/**
	 * Creates a shape without style and role.
	 */
	public ShapeSpec(Bounds bounds, GeometrySpec geometry, PaintSpec fill, LineSpec line, ShadowSpec shadow,
			TextSpec text) {
		this(bounds, geometry, fill, line, shadow, text, null, null);
	}

}

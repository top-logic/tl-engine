/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * A named style of the template profile: defaults for the properties of a shape or of a table
 * cell.
 *
 * <p>
 * A style is described with the same entries as a shape (or a table cell), see
 * {@link DescriptionReader#readStyle(Value, boolean)}. Each property the element gives itself wins
 * over the property of the style. A style can paint parts of the element in the color of the
 * element's role by the color role {@link PresentationData#ROLE_ACCENT}, see
 * {@link ElementSpec#role()}.
 * </p>
 *
 * @param role
 *        The color role of an element that gives none, <code>null</code> for none.
 * @param geometry
 *        The default geometry, <code>null</code> for none.
 * @param fill
 *        The default fill, <code>null</code> for none.
 * @param line
 *        The default outline, whose properties the outline of the element overrides individually,
 *        <code>null</code> for none.
 * @param shadow
 *        The default shadow, <code>null</code> for none.
 * @param text
 *        The default text formatting (without paragraphs), whose properties the text of the
 *        element overrides individually, <code>null</code> for none.
 * @param data
 *        The data representation the style was read from.
 */
public record StyleSpec(String role, GeometrySpec geometry, PaintSpec fill, LineSpec line, ShadowSpec shadow,
		TextSpec text, Map<String, Object> data) {

	/**
	 * The given shape with the properties of this style where it gives none itself.
	 *
	 * @return The shape without {@link ShapeSpec#style() style reference}.
	 */
	public ShapeSpec applyTo(ShapeSpec shape) {
		return new ShapeSpec(shape.bounds(), or(shape.geometry(), geometry), or(shape.fill(), fill),
			applyTo(shape.line()), or(shape.shadow(), shadow), applyTo(shape.text()), null, or(shape.role(), role));
	}

	/**
	 * The given table cell with the fill and the text formatting of this style where it gives none
	 * itself.
	 *
	 * @return The cell without {@link CellSpec#rating() rating}.
	 */
	public CellSpec applyTo(CellSpec cell) {
		return new CellSpec(applyTo(cell.text()), or(cell.fill(), fill), null);
	}

	private LineSpec applyTo(LineSpec own) {
		if (own == null || line == null) {
			return or(own, line);
		}
		return new LineSpec(or(own.paint(), line.paint()), or(own.width(), line.width()), or(own.dash(), line.dash()));
	}

	/**
	 * The given text with the formatting of this style where it gives none itself.
	 *
	 * @return The text, <code>null</code> if the given text is <code>null</code>.
	 */
	private TextSpec applyTo(TextSpec own) {
		if (own == null || text == null) {
			return own;
		}
		return new TextSpec(own.paragraphs(), own.style().inheritFrom(text.style()), or(own.align(), text.align()),
			or(own.anchor(), text.anchor()), or(own.insets(), text.insets()), or(own.overflow(), text.overflow()),
			own.split() || text.split(), or(own.wrap(), text.wrap()));
	}

	private static <T> T or(T own, T fallback) {
		return own != null ? own : fallback;
	}

}

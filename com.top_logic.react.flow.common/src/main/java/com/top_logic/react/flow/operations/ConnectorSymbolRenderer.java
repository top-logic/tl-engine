/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Shared rendering logic for {@link ConnectorSymbol}s (arrows, diamonds) at edge endpoints.
 *
 * <p>
 * Used by both tree connections and graph edges.
 * </p>
 */
public class ConnectorSymbolRenderer {

	/** Length of arrow symbols. */
	public static final double ARROW_LENGTH = 12.0;

	/** Width (opening) of arrow symbols. */
	public static final double ARROW_WIDTH = 8.0;

	/** Radius of diamond symbols. */
	public static final double DIAMOND_RADIUS = 6.0;

	/**
	 * The space to reserve (inset) for the given symbol so the line does not overlap it.
	 */
	public static double inset(ConnectorSymbol symbol, double scale) {
		switch (symbol) {
			case NONE:
			case ARROW:
				return 0;
			case CLOSED_ARROW:
			case FILLED_ARROW:
				return ARROW_LENGTH * scale;
			case DIAMOND:
			case FILLED_DIAMOND:
				return 2 * DIAMOND_RADIUS * scale;
		}
		return 0;
	}

	/**
	 * Draws a connector symbol at the given position.
	 *
	 * @param out
	 *        The SVG writer.
	 * @param x
	 *        X position of the symbol.
	 * @param y
	 *        Y position of the symbol.
	 * @param dx
	 *        Direction X component (normalized: -1, 0, or 1). Points from the edge toward the
	 *        node.
	 * @param dy
	 *        Direction Y component (normalized: -1, 0, or 1).
	 * @param symbol
	 *        The symbol to draw.
	 * @param strokeStyle
	 *        Stroke color.
	 * @param thickness
	 *        Stroke width.
	 */
	public static void drawSymbol(SvgWriter out, double x, double y, double dx, double dy,
			ConnectorSymbol symbol, String strokeStyle, double thickness) {

		if (symbol == null || symbol == ConnectorSymbol.NONE) {
			return;
		}

		double scale = thickness / 2;

		out.beginPath();
		out.setStrokeWidth(thickness);
		if (strokeStyle != null && !strokeStyle.isEmpty()) {
			out.setStroke(strokeStyle);
		}

		switch (symbol) {
			case ARROW:
			case CLOSED_ARROW:
			case FILLED_ARROW: {
				if (symbol != ConnectorSymbol.FILLED_ARROW) {
					out.setFill("none");
				}
				out.beginData();

				double arrowLen = ARROW_LENGTH * scale;
				double arrowHalf = ARROW_WIDTH * scale / 2;

				// Arrow tip is at (x, y), pointing in direction (dx, dy).
				// Perpendicular direction is (-dy, dx).
				out.moveToAbs(x - dx * arrowLen - dy * arrowHalf, y - dy * arrowLen + dx * arrowHalf);
				out.lineToAbs(x, y);
				out.lineToAbs(x - dx * arrowLen + dy * arrowHalf, y - dy * arrowLen - dx * arrowHalf);
				if (symbol != ConnectorSymbol.ARROW) {
					out.closePath();
				}

				out.endData();
				out.endPath();
				break;
			}
			case DIAMOND:
			case FILLED_DIAMOND: {
				if (symbol != ConnectorSymbol.FILLED_DIAMOND) {
					out.setFill("none");
				}
				out.beginData();

				double r = DIAMOND_RADIUS * scale;

				// Diamond centered at (x - dx*r, y - dy*r), rotated to direction.
				double cx = x - dx * r;
				double cy = y - dy * r;
				out.moveToAbs(cx + dx * r, cy + dy * r); // tip toward node
				out.lineToAbs(cx - dy * r, cy + dx * r); // perpendicular
				out.lineToAbs(cx - dx * r, cy - dy * r); // opposite tip
				out.lineToAbs(cx + dy * r, cy - dx * r); // other perpendicular
				out.closePath();

				out.endData();
				out.endPath();
				break;
			}
			default:
				break;
		}
	}

}

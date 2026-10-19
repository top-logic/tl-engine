/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.presentation.template.PaintInfo;

/**
 * The fill of a shape, a line or a table cell in a presentation description.
 *
 * <p>
 * The kinds of fills are named like the {@link PaintInfo.Type types} of the template analysis.
 * </p>
 */
public sealed interface PaintSpec {

	/**
	 * Paints nothing.
	 */
	PaintSpec NONE = new None();

	/**
	 * A fill painting nothing.
	 */
	record None() implements PaintSpec {
		// Pure data.
	}

	/**
	 * A fill with a single color.
	 *
	 * @param color
	 *        The fill color.
	 */
	record Solid(ColorSpec color) implements PaintSpec {
		// Pure data.
	}

	/**
	 * A color gradient.
	 *
	 * @param stops
	 *        The colors along the gradient, at least two.
	 * @param angle
	 *        The direction of a linear gradient in degrees, clockwise with 0 running from left to
	 *        right, <code>null</code> for a path gradient.
	 * @param path
	 *        The shape of a path gradient (<code>circle</code>, <code>rect</code> or
	 *        <code>shape</code>) spreading from the center, <code>null</code> for a linear
	 *        gradient.
	 */
	record Gradient(List<Stop> stops, Double angle, String path) implements PaintSpec {

		/**
		 * The shapes of a path gradient.
		 */
		public static final List<String> PATHS = List.of("circle", "rect", "shape");

	}

	/**
	 * A color at a position of a {@link Gradient}.
	 *
	 * @param position
	 *        The position along the gradient, between 0 and 1.
	 * @param color
	 *        The color at this position.
	 */
	record Stop(double position, ColorSpec color) {
		// Pure data.
	}

	/**
	 * A fill with an image stretched to the filled area.
	 *
	 * @param data
	 *        The image.
	 * @param opacity
	 *        The opacity of the image between 0 and 1, <code>null</code> for an opaque image.
	 */
	record Picture(BinaryDataSource data, Double opacity) implements PaintSpec {
		// Pure data.
	}

}

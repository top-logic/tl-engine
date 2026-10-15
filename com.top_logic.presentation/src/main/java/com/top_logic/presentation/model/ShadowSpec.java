/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.Map;

import com.top_logic.presentation.template.ColorInfo;

/**
 * The outer shadow of a shape in a presentation description.
 *
 * @param color
 *        The shadow color, its transparency given by the modification
 *        {@link ColorInfo#ALPHA}; <code>null</code> only for {@link #NONE}.
 * @param blur
 *        The blur radius in points.
 * @param distance
 *        The distance of the shadow from the shape in points.
 * @param direction
 *        The direction of the shadow in degrees, clockwise with 0 pointing to the right (90 casts
 *        the shadow downwards).
 */
public record ShadowSpec(ColorSpec color, double blur, double distance, double direction) {

	/**
	 * No shadow, also not the shadow of the style of the shape.
	 */
	public static final ShadowSpec NONE = new ShadowSpec(null, 0, 0, 0);

	/**
	 * The {@link #color()} of a shadow without explicit color: black with 35% opacity.
	 */
	public static final ColorSpec DEFAULT_COLOR =
		new ColorSpec.Rgb("000000", Map.of(ColorInfo.ALPHA, 0.35));

	/**
	 * The {@link #blur()} of a shadow without explicit blur radius.
	 */
	public static final double DEFAULT_BLUR = 6;

	/**
	 * The {@link #distance()} of a shadow without explicit distance.
	 */
	public static final double DEFAULT_DISTANCE = 2;

	/**
	 * The {@link #direction()} of a shadow without explicit direction: downwards.
	 */
	public static final double DEFAULT_DIRECTION = 90;

}

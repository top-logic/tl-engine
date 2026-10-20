/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.Map;

/**
 * The outline geometry of a shape in a presentation description.
 *
 * @param preset
 *        The name of the preset geometry, e.g. <code>rect</code>, <code>roundRect</code>,
 *        <code>ellipse</code> or <code>rightArrow</code>.
 * @param adjust
 *        The adjust values of the preset geometry by their guide names (e.g. <code>adj</code>), as
 *        given in the file (100000 for 100%).
 * @param cornerRadius
 *        The corner radius in points of a <code>roundRect</code>, <code>null</code> for the default
 *        radius.
 */
public record GeometrySpec(String preset, Map<String, Double> adjust, Double cornerRadius) {

	/**
	 * The {@link #preset()} of a rectangle.
	 */
	public static final String RECT = "rect";

	/**
	 * The {@link #preset()} of a rectangle with rounded corners.
	 */
	public static final String ROUND_RECT = "roundRect";

	/**
	 * A plain rectangle.
	 */
	public static final GeometrySpec RECTANGLE = new GeometrySpec(RECT, Map.of(), null);

}

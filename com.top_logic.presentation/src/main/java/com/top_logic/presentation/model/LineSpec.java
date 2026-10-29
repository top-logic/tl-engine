/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * The outline of a shape in a presentation description.
 *
 * @param paint
 *        The fill of the line, {@link PaintSpec#NONE} for no outline, <code>null</code> to keep the
 *        default.
 * @param width
 *        The line width in points, <code>null</code> to keep the default.
 * @param dash
 *        The dash style, one of {@link #DASHES}, <code>null</code> to keep the default.
 */
public record LineSpec(PaintSpec paint, Double width, String dash) {

	/**
	 * No outline.
	 */
	public static final LineSpec NONE = new LineSpec(PaintSpec.NONE, null, null);

	/**
	 * The names of the dash styles.
	 */
	public static final List<String> DASHES = List.of("solid", "dot", "dash", "lgDash", "dashDot", "lgDashDot",
		"lgDashDotDot", "sysDash", "sysDot", "sysDashDot", "sysDashDotDot");

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Inner margins of a text in its shape or table cell, in points.
 *
 * @param left
 *        The left margin.
 * @param top
 *        The top margin.
 * @param right
 *        The right margin.
 * @param bottom
 *        The bottom margin.
 */
public record InsetsSpec(double left, double top, double right, double bottom) {

	/**
	 * No margins.
	 */
	public static final InsetsSpec ZERO = new InsetsSpec(0, 0, 0, 0);

}

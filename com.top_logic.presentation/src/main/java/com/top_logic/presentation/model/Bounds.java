/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The position and size of an element on a slide in a presentation description.
 *
 * <p>
 * All values are given in points relative to the top left corner of the slide. A value that is
 * not given is determined by the area the element is placed in, e.g. the content area of the slide
 * layout.
 * </p>
 *
 * @param x
 *        The distance of the left border from the left slide border, <code>null</code> if not
 *        given.
 * @param y
 *        The distance of the top border from the top slide border, <code>null</code> if not
 *        given.
 * @param width
 *        The width, <code>null</code> if not given.
 * @param height
 *        The height, <code>null</code> if not given.
 */
public record Bounds(Double x, Double y, Double width, Double height) {

	/**
	 * Bounds without any value.
	 */
	public static final Bounds NONE = new Bounds(null, null, null, null);

	/**
	 * Whether position and size are given completely.
	 */
	public boolean isComplete() {
		return x != null && y != null && width != null && height != null;
	}

	/**
	 * Whether no value is given.
	 */
	public boolean isEmpty() {
		return x == null && y == null && width == null && height == null;
	}

}

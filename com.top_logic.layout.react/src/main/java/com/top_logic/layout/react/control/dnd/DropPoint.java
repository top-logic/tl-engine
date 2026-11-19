/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * The point a drop is made at, in the coordinate system of its target.
 *
 * <p>
 * Only a control displaying its items on a surface - a diagram, say - reports such a point. For a
 * drop {@link DropLocation.Onto onto} an item, the point is relative to the upper left corner of
 * that item; for a drop on the {@link DropLocation.Control control} as a whole, it is relative to the
 * origin of the control's surface. The coordinates are in the units of that surface, independent of
 * how far the user zoomed or scrolled the display.
 * </p>
 *
 * @param x
 *        The horizontal coordinate, growing to the right.
 * @param y
 *        The vertical coordinate, growing downwards.
 *
 * @see DropArguments#getX()
 * @see DropArguments#getY()
 */
public record DropPoint(double x, double y) {
	// Pure data.
}

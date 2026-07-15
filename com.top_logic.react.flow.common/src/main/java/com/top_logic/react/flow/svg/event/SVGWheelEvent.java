/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * Event that announces a mouse wheel action on a SVG element.
 */
public interface SVGWheelEvent extends SVGEvent {

	/**
	 * Horizontal scroll delta (pixels).
	 */
	double getDeltaX();

	/**
	 * Vertical scroll delta (pixels).
	 */
	double getDeltaY();

	/**
	 * Whether the ctrl key was pressed.
	 */
	boolean isCtrlKey();

	/**
	 * Whether the shift key was pressed.
	 */
	boolean isShiftKey();

	/**
	 * The X coordinate of the mouse pointer relative to the event target's offset parent.
	 */
	double getOffsetX();

	/**
	 * The Y coordinate of the mouse pointer relative to the event target's offset parent.
	 */
	double getOffsetY();

	/**
	 * Updates the {@code transform} attribute on the SVG element with the given ID.
	 *
	 * <p>
	 * This allows event handlers in the common module to manipulate SVG transforms without
	 * direct DOM access. The implementation is provided by the client-side SVG builder.
	 * </p>
	 */
	default void updateTransform(String elementId, double translateX, double translateY) {
		// No-op for non-interactive writers.
	}

	/**
	 * Requests a full layout recomputation and SVG re-draw.
	 *
	 * <p>
	 * Use this when a model change (e.g. zoom) alters element positions. For pure scroll
	 * (transform-only changes), use {@link #updateTransform} instead.
	 * </p>
	 */
	default void requestRelayout() {
		// No-op for non-interactive writers.
	}

}

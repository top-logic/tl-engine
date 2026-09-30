/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * Event for drag-to-pan gestures on SVG elements.
 *
 * <p>
 * Phases: {@link Phase#START} on pointer down, {@link Phase#MOVE} on pointer move,
 * {@link Phase#END} on pointer up.
 * </p>
 */
public interface SVGPanEvent extends SVGEvent {

	/** Phase of the pan gesture. */
	enum Phase {
		/** Pointer down — pan session starts. */
		START,
		/** Pointer move — pan in progress. */
		MOVE,
		/** Pointer up — pan session ends. */
		END
	}

	/**
	 * The current phase.
	 */
	Phase getPhase();

	/**
	 * X coordinate in SVG coordinate space (converted from client coords via CTM).
	 */
	double getX();

	/**
	 * Y coordinate in SVG coordinate space.
	 */
	double getY();

	/**
	 * Updates the {@code transform} attribute on the SVG element with the given ID.
	 */
	default void updateTransform(String elementId, double translateX, double translateY) {
		// No-op for non-interactive writers.
	}

}

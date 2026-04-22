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

}

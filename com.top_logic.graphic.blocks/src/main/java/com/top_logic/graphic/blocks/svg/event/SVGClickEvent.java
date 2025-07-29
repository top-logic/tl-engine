/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg.event;

/**
 * Event that announces a click on a SVG drawing.
 */
public interface SVGClickEvent extends SVGEvent {

	/**
	 * Whether the given {@link MouseButton} was pressed.
	 */
	boolean getButton(MouseButton button);

	/**
	 * Whether the meta key was pressed.
	 */
	boolean isMetaKey();

	/**
	 * Whether the shift key was pressed.
	 */
	boolean isShiftKey();

	/**
	 * Whether the alt key was pressed.
	 */
	boolean isAltKey();

	/**
	 * Whether the ctrl key was pressed.
	 */
	boolean isCtrlKey();

}

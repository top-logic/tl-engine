/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * A key pressed while an element of a SVG diagram has the keyboard focus.
 *
 * @see SVGKeyHandler
 */
public interface SVGKeyEvent extends SVGEvent {

	/** The {@link #getKey() key} of the Enter key. */
	String KEY_ENTER = "Enter";

	/** The {@link #getKey() key} of the space bar. */
	String KEY_SPACE = " ";

	/**
	 * The value of the pressed key, as the DOM {@code KeyboardEvent.key} reports it, e.g.
	 * {@link #KEY_ENTER}.
	 */
	String getKey();

}

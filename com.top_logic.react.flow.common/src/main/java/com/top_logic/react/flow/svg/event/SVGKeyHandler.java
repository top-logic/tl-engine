/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * Handler that accepts {@link SVGKeyEvent}s.
 */
public interface SVGKeyHandler {

	/**
	 * A key was pressed while the SVG element had the keyboard focus.
	 *
	 * <p>
	 * A handler that does not act on the pressed key leaves the event alone, so that it keeps its
	 * default action and reaches the enclosing elements.
	 * </p>
	 */
	void onKeyDown(SVGKeyEvent event);

}

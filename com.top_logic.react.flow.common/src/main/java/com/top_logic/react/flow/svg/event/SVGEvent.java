/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Base interface for events that might occur in a SVG diagram.
 */
public interface SVGEvent {

	/**
	 * The element that was clicked.
	 */
	Object getSender();

	/**
	 * Stops propagation of the event to parent elements.
	 */
	void stopPropagation();

	/**
	 * Prevents the default action for this event.
	 */
	void preventDefault();

	/**
	 * Updates the {@code transform} attribute on the SVG element the given owner has drawn with
	 * the ID for the given purpose, see {@link SvgWriter#uniqueId(Widget, String)}.
	 *
	 * <p>
	 * This allows event handlers in the common module to manipulate SVG transforms without
	 * direct DOM access. The implementation is provided by the client-side SVG builder.
	 * </p>
	 */
	default void updateTransform(Widget owner, String purpose, double translateX, double translateY) {
		// No-op for non-interactive writers.
	}

}

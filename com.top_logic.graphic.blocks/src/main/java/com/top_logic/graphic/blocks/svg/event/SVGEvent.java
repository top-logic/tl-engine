/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg.event;

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

}

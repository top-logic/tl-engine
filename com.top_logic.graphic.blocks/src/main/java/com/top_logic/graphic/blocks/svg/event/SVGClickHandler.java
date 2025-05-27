/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg.event;

/**
 * Handler that accepts {@link SVGClickEvent}s.
 */
public interface SVGClickHandler {

	/**
	 * A mouse click was observed.
	 */
	void onClick(SVGClickEvent event);

}

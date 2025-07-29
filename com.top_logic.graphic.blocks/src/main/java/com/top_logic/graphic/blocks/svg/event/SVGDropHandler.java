/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg.event;

/**
 * Handler that accepts {@link SVGDropEvent}s.
 */
public interface SVGDropHandler {

	/**
	 * A drop was observed.
	 */
	void onDrop(SVGDropEvent event);

}

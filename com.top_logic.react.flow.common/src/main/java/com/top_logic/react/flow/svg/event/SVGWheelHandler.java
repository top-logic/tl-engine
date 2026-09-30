/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * Handler that accepts {@link SVGWheelEvent}s.
 */
public interface SVGWheelHandler {

	/**
	 * A mouse wheel action was observed on the SVG element.
	 */
	void onWheel(SVGWheelEvent event);

}

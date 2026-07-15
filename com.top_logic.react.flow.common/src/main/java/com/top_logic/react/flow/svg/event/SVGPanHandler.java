/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg.event;

/**
 * Handler for drag-to-pan gestures on SVG elements.
 *
 * <p>
 * The handler receives all three phases ({@link SVGPanEvent.Phase#START},
 * {@link SVGPanEvent.Phase#MOVE}, {@link SVGPanEvent.Phase#END}) through a single method.
 * </p>
 */
public interface SVGPanHandler {

	/**
	 * A pan gesture phase was observed.
	 */
	void onPan(SVGPanEvent event);

}

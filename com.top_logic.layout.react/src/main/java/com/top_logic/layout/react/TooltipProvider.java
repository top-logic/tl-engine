/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.servlet.ReactServlet;

/**
 * Interface for React controls that provide lazy-loaded tooltip content.
 *
 * <p>
 * Controls implementing this interface serve rich tooltip HTML for opaque, control-local keys via
 * the {@code /react-api/tooltip} endpoint. The {@link ReactServlet} dispatches incoming GET
 * requests to the appropriate control based on the {@code controlId} parameter.
 * </p>
 *
 * <p>
 * The client declares a tooltip with a single {@code data-tooltip} attribute whose value names the
 * mode: {@code text:<text>} and {@code html:<markup>} carry the content itself, {@code content}
 * takes the element's own text, {@code key:<key>} asks this provider, and {@code dynamic} lets the
 * control resolve key or content from the hover target via a {@code tl-tooltip-resolve} DOM event.
 * In the latter two cases, the looked-up key is handed to this method.
 * </p>
 *
 * <p>
 * An element may restrict its tooltip with a {@code data-tooltip-when} attribute, read on the
 * declaring element or on any element between it and the hover target. The value
 * {@code truncated} shows the tooltip only while that element's text is not fully readable, i.e.
 * clipped by its box or hidden altogether, so that a tooltip offers text the user cannot read
 * where it belongs.
 * </p>
 */
public interface TooltipProvider {

	/**
	 * Returns the tooltip content for the given key, or {@code null} if no tooltip exists.
	 *
	 * @param key
	 *        Opaque, control-local tooltip identifier.
	 */
	TooltipContent getTooltipContent(String key);

}

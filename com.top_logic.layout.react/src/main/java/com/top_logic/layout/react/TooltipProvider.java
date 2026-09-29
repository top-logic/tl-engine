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
 * The client uses a single {@code data-tooltip} attribute whose value is either {@code key:<key>}
 * (for a server round-trip) or {@code dynamic} (the control resolves the key from the hover target
 * via a {@code tl-tooltip-resolve} DOM event). In both cases, the looked-up key is handed to this
 * method.
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

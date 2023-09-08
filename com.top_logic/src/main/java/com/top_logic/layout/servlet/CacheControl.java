/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.servlet;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledFromJSP;

/**
 * Utility for controling response caching.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CacheControl {

	/**
	 * Prevent caching the given response.
	 */
	@CalledFromJSP
	public static void setNoCache(HttpServletResponse response) {
		// HTTP 1.1.
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		// HTTP 1.0.
		response.setHeader("Pragma", "no-cache");
		// Proxies.
		response.setDateHeader("Expires", 0);
	}

}

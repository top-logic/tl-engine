/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledFromJSP;

/**
 * Utilities for implementing behavior during a pending boot.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeferredBootUtil {

	/**
	 * If {@link #isBootPending()}, redirect to the configured deferred boot location.
	 * 
	 * @param request
	 *        The current request.
	 * @param response
	 *        The current response.
	 * @return Whether the request was redirected.
	 */
	@CalledFromJSP
	public static boolean redirectOnPendingBoot(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (!isBootPending()) {
			return false;
		}
		
		DeferredBootService.getInstance().redirect(request, response);
		return true;
	}

	/**
	 * Whether the system has not yet finally booted.
	 */
	@CalledFromJSP
	public static boolean isBootPending() {
		return DeferredBootService.Module.INSTANCE.isActive();
	}

	/**
	 * Start the system.
	 */
	@CalledFromJSP
	public static final void boot() throws BootFailure {
		DeferredBootService.getInstance().boot();
	}
}

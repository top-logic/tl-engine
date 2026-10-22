/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.Locale;

/**
 * Detects whether the app host has a local graphical display a headed (visible) Chromium could open
 * a window on.
 *
 * <p>
 * Consulted when the browser is configured headed ({@link BrowserToolsService.Config#getHeadless()
 * headless} off): a headed browser needs a desktop, so on a headless server or container startup
 * must fail fast with a clear message rather than block on a launch that can never succeed. On Linux
 * "a display" means an X11 ({@code DISPLAY}) or Wayland
 * ({@code WAYLAND_DISPLAY}) server is reachable; other desktop operating systems always have one.
 * </p>
 *
 * <p>
 * The result can be forced through the system property {@value #FORCE_PROPERTY} ({@code true}/
 * {@code false}) so the fail-fast wiring is testable on any host without a real display.
 * </p>
 */
final class HeadedDisplay {

	/**
	 * System property that, when set to {@code true}/{@code false}, overrides the environment-based
	 * display detection. Intended for tests of the display guard.
	 */
	static final String FORCE_PROPERTY = "tl.ai.browser.headed.displayAvailable";

	private HeadedDisplay() {
		// Static utility.
	}

	/**
	 * Whether a local display is available for a headed browser window.
	 *
	 * @return {@code true} if a headed Chromium can plausibly open a window on this host.
	 */
	static boolean isAvailable() {
		String forced = System.getProperty(FORCE_PROPERTY);
		if (forced != null && !forced.isBlank()) {
			return Boolean.parseBoolean(forced.trim());
		}
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("linux") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			// A headless Linux host / container has neither an X11 nor a Wayland server.
			return notBlank(System.getenv("DISPLAY")) || notBlank(System.getenv("WAYLAND_DISPLAY"));
		}
		// Windows and macOS run a desktop session; assume a display is present.
		return true;
	}

	private static boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}
}

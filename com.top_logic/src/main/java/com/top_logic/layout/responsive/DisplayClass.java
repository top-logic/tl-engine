/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.responsive;

/**
 * Classification of the available display space of the client viewport.
 *
 * <p>
 * The value is detected on the client (via a media query at the responsive breakpoint) and reported
 * to the server, where it is held per browser tab in a {@link DisplayClassModel}. UI that adapts to
 * the available space (e.g. a responsive master-detail) branches on this value to choose a
 * presentation that fits the device.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum DisplayClass {

	/**
	 * A wide viewport (e.g. a desktop browser) with enough room for side-by-side presentations such
	 * as a master-detail split.
	 */
	REGULAR,

	/**
	 * A narrow viewport (e.g. a smartphone) where side-by-side presentations do not fit and a
	 * step-by-step drill-in is used instead.
	 */
	COMPACT;

	/**
	 * The {@link DisplayClass} assumed before the client has reported its actual viewport.
	 *
	 * <p>
	 * Defaulting to {@link #REGULAR} matches the desktop-first rendering of the rest of the UI: a
	 * narrow client corrects the assumption with its first report.
	 * </p>
	 */
	public static final DisplayClass DEFAULT = REGULAR;

}

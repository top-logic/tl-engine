/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Hamburger button that toggles a target {@link ReactSidebarControl}'s mobile drawer.
 *
 * <p>
 * Designed to be placed inside a {@code SlotContentControl} contributing to whichever slot the
 * sidebar is configured to address via {@code drawer-open-slot-name}, so the button surfaces in
 * the matched placeholder (typically the app bar's leading area) while the sidebar itself lives
 * in the content area. CSS hides the button at desktop breakpoints; at mobile breakpoints the
 * sidebar's CSS turns the nav rail into an off-canvas drawer that this button opens. The drawer
 * state is independent of the desktop rail's persisted {@code collapsed} preference.
 * </p>
 */
public class DrawerToggleControl extends ReactControl {

	private static final String REACT_MODULE = "TLDrawerToggle";

	private final ReactSidebarControl _target;

	/**
	 * Creates a drawer-toggle button bound to the given sidebar.
	 *
	 * @param target
	 *        The sidebar whose mobile drawer this button opens/closes.
	 */
	public DrawerToggleControl(ReactContext context, ReactSidebarControl target) {
		super(context, null, REACT_MODULE);
		_target = target;
	}

	/**
	 * Handles toggle clicks from the client.
	 */
	@ReactCommandHandler("toggle")
	void handleToggle() {
		_target.toggleDrawer();
	}
}

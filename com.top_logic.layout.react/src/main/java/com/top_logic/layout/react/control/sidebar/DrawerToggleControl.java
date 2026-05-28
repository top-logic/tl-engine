/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Hamburger button that toggles a target {@link ReactSidebarControl}'s collapsed state.
 *
 * <p>
 * Designed to be placed inside a {@code SlotContentControl} contributing to the app bar's
 * {@code appbar-leading} slot, so the button surfaces in the app bar while the sidebar lives in
 * the content area. CSS hides the button at desktop breakpoints; at mobile breakpoints the
 * sidebar's CSS turns the nav rail into an off-canvas drawer that this button opens.
 * </p>
 */
public class DrawerToggleControl extends ReactControl {

	private static final String REACT_MODULE = "TLDrawerToggle";

	private final ReactSidebarControl _target;

	/**
	 * Creates a drawer-toggle button bound to the given sidebar.
	 *
	 * @param target
	 *        The sidebar whose collapsed state this button toggles.
	 */
	public DrawerToggleControl(ReactContext context, ReactSidebarControl target) {
		super(context, null, REACT_MODULE);
		_target = target;
	}

	/**
	 * Handles toggle clicks from the client.
	 */
	@ReactCommand("toggle")
	void handleToggle() {
		_target.toggleCollapse();
	}
}

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
 * Hamburger button that opens and closes the drawer of a target {@link ReactSidebarControl}.
 *
 * <p>
 * Placed inside a {@code SlotContentControl} contributing to the slot the sidebar addresses via
 * {@code drawer-open-slot-name}, so the button surfaces in the matched placeholder (typically the app bar's leading
 * area) while the sidebar itself lives in the content area. The client renders the button only while the app shell
 * is compact; there the sidebar's navigation is a modal drawer this button opens. The drawer state is independent of
 * the desktop rail's persisted {@code collapsed} preference.
 * </p>
 *
 * <p>
 * State: {@code open} - whether the drawer is open (the button's {@code aria-expanded}); {@code controls} - the id of
 * the sidebar's navigation ({@code aria-controls} while open).
 * </p>
 */
public class DrawerToggleControl extends ReactControl {

	private static final String REACT_MODULE = "TLDrawerToggle";

	/** State key: whether the drawer is open. */
	private static final String OPEN = "open";

	/** State key: the id of the navigation the button opens. */
	private static final String CONTROLS = "controls";

	private final ReactSidebarControl _target;

	/**
	 * Creates a drawer-toggle button bound to the given sidebar.
	 *
	 * @param target
	 *        The sidebar whose drawer this button opens and closes.
	 */
	public DrawerToggleControl(ReactContext context, ReactSidebarControl target) {
		super(context, null, REACT_MODULE);
		_target = target;
		putState(OPEN, Boolean.FALSE);
		putState(CONTROLS, target.getID() + "-nav");
		target.setDrawerToggle(this);
	}

	/**
	 * Reports whether the drawer this button opens is open.
	 */
	void setOpen(boolean open) {
		putState(OPEN, Boolean.valueOf(open));
	}

	/**
	 * Handles toggle clicks from the client.
	 */
	@ReactCommandHandler("toggle")
	void handleToggle() {
		_target.toggleDrawer();
	}
}

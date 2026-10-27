/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.view.ViewConfig;

/**
 * How a {@link ShowStep} displays its view: where it is displayed within the window, as a dialog,
 * or as the window's new root view.
 *
 * @see DisplayTargetService.ShowConfig#getDisplay()
 */
public enum ShowDisplay implements ExternallyNamed {

	/**
	 * The view is brought into view where it is displayed within the window: the containers on the
	 * way to it are opened, or it is drilled down to as a frame of the surrounding stack.
	 */
	IN_PLACE("in-place"),

	/**
	 * The view is opened as a dialog on top of the current display.
	 */
	DIALOG("dialog"),

	/**
	 * The window loads the view as its new root view, replacing everything it displayed.
	 *
	 * <p>
	 * The view must be an entry point of the application, its {@link ViewConfig#getDefaultView()
	 * default view} or one of its {@link ViewConfig#getEntryPoints() entry points}. Its channels
	 * receive no values: the page displaying it is a new one.
	 * </p>
	 */
	TOP_LEVEL("top-level");

	private final String _externalName;

	ShowDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}

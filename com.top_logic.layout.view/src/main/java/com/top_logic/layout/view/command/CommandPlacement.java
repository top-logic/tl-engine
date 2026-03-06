/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * Where a {@link ViewCommand} is rendered in the UI.
 */
public enum CommandPlacement {

	/**
	 * Rendered in the toolbar area (typically at the top of a view).
	 */
	TOOLBAR,

	/**
	 * Rendered in a button bar (typically at the bottom of a form or dialog).
	 */
	BUTTON_BAR,

	/**
	 * Rendered in the context menu (right-click or overflow menu).
	 */
	CONTEXT_MENU,

	/**
	 * Not rendered at all. The command can still be invoked programmatically.
	 */
	NONE;
}

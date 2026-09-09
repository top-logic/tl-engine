/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewLoader;

/**
 * One view a {@link DisplayTarget} displays, together with the values its channels receive.
 *
 * @param viewRef
 *        Path of the view file to display, relative to {@link ViewLoader#VIEW_BASE_PATH}.
 * @param dialog
 *        Whether the view is displayed as a dialog on top of the current display, rather than
 *        within it.
 * @param label
 *        The label the view is announced with, or {@code null} to leave the naming to the display
 *        itself.
 * @param bindings
 *        The values the view's channels receive, in configuration order.
 */
public record ShowStep(String viewRef, boolean dialog, ResKey label, List<Binding> bindings) {

	/**
	 * Creates a {@link ShowStep} with an unmodifiable copy of the given bindings.
	 */
	public ShowStep {
		bindings = List.copyOf(bindings);
	}

	@Override
	public String toString() {
		return viewRef + bindings;
	}
}

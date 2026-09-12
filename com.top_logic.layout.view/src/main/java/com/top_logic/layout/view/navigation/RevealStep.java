/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.view.UIElement;

/**
 * One step of a {@link RevealPath}: the container that had to display a child for the display below
 * it to exist, and the key of that child.
 *
 * @param container
 *        The element whose control chose the child, or {@code null} for a step no configured
 *        element accounts for - a dialog opened on top of the display.
 * @param key
 *        The key the container addresses the child by, as
 *        {@link ChildRevealer#revealChild(String)} takes it.
 */
public record RevealStep(UIElement container, String key) {

	/**
	 * The same step as part of a statically scanned {@link MountPath}.
	 *
	 * @return The mount step, or {@code null} for a step without a {@link #container()}, which no
	 *         scan of the configuration produces.
	 */
	public MountStep mountStep() {
		return container == null ? null : new MountStep(container, key);
	}

	@Override
	public String toString() {
		return (container == null ? "dialog" : container.getClass().getSimpleName()) + "[" + key + "]";
	}
}

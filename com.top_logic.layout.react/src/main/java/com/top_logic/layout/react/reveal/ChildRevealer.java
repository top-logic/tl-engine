/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.reveal;

import com.top_logic.layout.react.dirty.ChannelVetoException;

/**
 * A control that addresses parts of its content by a key and can bring one of them into view.
 *
 * <p>
 * Implemented by the containers that display one of their children at a time - a sidebar (keyed by
 * item id), a tab bar (keyed by tab id), a master-detail element (keyed by side), a stack of
 * drilled-down views (keyed by frame position). Whoever wants to display something nested deeply
 * within the display asks each container on the way to reveal the child leading further down.
 * </p>
 */
public interface ChildRevealer {

	/**
	 * Displays the child addressed by the given key, creating it if the container has not displayed
	 * it yet.
	 *
	 * @param key
	 *        The key the container addresses the child by.
	 * @throws ChannelVetoException
	 *         If what is currently displayed holds unsaved changes. The caller asks the user how to
	 *         proceed and retries through
	 *         {@link ChannelVetoException#getContinuation() the veto's continuation}.
	 * @throws IllegalArgumentException
	 *         If the container addresses no child by that key.
	 */
	void revealChild(String key) throws ChannelVetoException;
}

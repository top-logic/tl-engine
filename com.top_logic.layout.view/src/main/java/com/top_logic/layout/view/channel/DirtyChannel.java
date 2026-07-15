/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Tracks the dirty state of multiple {@link StateHandler}s within a scope (e.g. a tab).
 *
 * <p>
 * This class extends the base {@link com.top_logic.layout.react.dirty.DirtyChannel} defined in the
 * React module and adds bridge methods that accept
 * {@link com.top_logic.layout.view.form.StateHandler}.
 * </p>
 *
 * @see StateHandler
 */
public class DirtyChannel extends com.top_logic.layout.react.dirty.DirtyChannel {

	/**
	 * Bridge for {@link #updateState(com.top_logic.layout.react.dirty.StateHandler, boolean)} that
	 * accepts the view-level {@link StateHandler}.
	 */
	public void updateState(StateHandler handler, boolean dirty) {
		super.updateState(handler, dirty);
	}

	/**
	 * Bridge for {@link #removeHandler(com.top_logic.layout.react.dirty.StateHandler)} that accepts
	 * the view-level {@link StateHandler}.
	 */
	public void removeHandler(StateHandler handler) {
		super.removeHandler(handler);
	}
}

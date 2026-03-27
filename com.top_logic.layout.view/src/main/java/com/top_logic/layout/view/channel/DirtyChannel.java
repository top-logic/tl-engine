/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

/**
 * Tracks the dirty state of multiple {@link com.top_logic.layout.view.form.StateHandler}s within a
 * scope (e.g. a tab).
 *
 * <p>
 * This class extends the base {@link com.top_logic.layout.react.dirty.DirtyChannel} defined in the
 * React module and is kept here for backward compatibility in the view module.
 * </p>
 *
 * @see com.top_logic.layout.view.form.StateHandler
 */
public class DirtyChannel extends com.top_logic.layout.react.dirty.DirtyChannel {
	// All functionality inherited from com.top_logic.layout.react.dirty.DirtyChannel.
}

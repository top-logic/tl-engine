/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Abstraction for a component that holds state that can be dirty (modified but not yet persisted).
 *
 * <p>
 * Provides the operations needed by the dirty-check confirmation dialog: querying dirty/error
 * status, saving, and discarding changes.
 * </p>
 *
 * <p>
 * This interface extends the base {@link com.top_logic.layout.react.dirty.StateHandler} defined in
 * the React module and is kept here for backward compatibility.
 * </p>
 *
 * @see com.top_logic.layout.view.channel.DirtyChannel
 * @see com.top_logic.layout.view.channel.ChannelVetoException
 */
public interface StateHandler extends com.top_logic.layout.react.dirty.StateHandler {
	// Pure marker extension for backward compatibility.
}

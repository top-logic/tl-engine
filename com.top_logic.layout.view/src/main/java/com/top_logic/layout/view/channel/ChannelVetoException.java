/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Thrown when a channel value change is blocked by one or more dirty forms.
 *
 * <p>
 * This class extends the base {@link com.top_logic.layout.react.dirty.ChannelVetoException}
 * defined in the React module and is kept here for backward compatibility in the view module.
 * </p>
 */
public class ChannelVetoException extends com.top_logic.layout.react.dirty.ChannelVetoException {

	/**
	 * Creates a new {@link ChannelVetoException}.
	 *
	 * @param dirtyHandlers
	 *        The dirty state handlers that caused the veto. Must not be empty.
	 * @param continuation
	 *        A {@link Runnable} that retries the blocked action after dirty state is resolved.
	 */
	public ChannelVetoException(List<StateHandler> dirtyHandlers, Runnable continuation) {
		super(new ArrayList<>(dirtyHandlers), continuation);
	}
}

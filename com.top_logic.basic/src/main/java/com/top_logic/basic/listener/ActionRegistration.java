/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.util.Objects;

/**
 * {@link Registration} that executes an action when it is {@link #dispose() disposed}.
 * 
 * @see Registration#onDispose(Runnable)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class ActionRegistration implements Registration {

	private Runnable _action;

	/**
	 * Creates a {@link ActionRegistration}.
	 * 
	 * @param action
	 *        The action that ends the registration.
	 */
	ActionRegistration(Runnable action) {
		_action = Objects.requireNonNull(action);
	}

	@Override
	public void dispose() {
		Runnable action = _action;
		if (action == null) {
			return;
		}
		_action = null;
		action.run();
	}

	@Override
	public boolean isActive() {
		return _action != null;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * Handle of a single listener registration.
 * 
 * <p>
 * Registering a listener with an observable yields a {@link Registration}. Disposing it ends the
 * registration: from that moment on, the listener is no longer notified, also not by a
 * notification that is currently running and has not yet reached the listener.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Registration {

	/**
	 * {@link Registration} that is not {@link #isActive() active}.
	 * 
	 * <p>
	 * Result of a registration with an observable that never reports anything.
	 * </p>
	 */
	Registration NONE = new Registration() {
		@Override
		public void dispose() {
			// Nothing registered.
		}

		@Override
		public boolean isActive() {
			return false;
		}
	};

	/**
	 * Creates a {@link Registration} that executes the given action when it is
	 * {@link #dispose() disposed} the first time.
	 * 
	 * @param action
	 *        The action that ends the registration.
	 * @return The {@link Registration} that is {@link #isActive() active} until it is disposed.
	 */
	static Registration onDispose(Runnable action) {
		return new ActionRegistration(action);
	}

	/**
	 * Ends this registration.
	 * 
	 * <p>
	 * Calling this method on an already disposed registration has no effect.
	 * </p>
	 */
	void dispose();

	/**
	 * Whether this registration has not yet been {@link #dispose() disposed}.
	 */
	boolean isActive();

}

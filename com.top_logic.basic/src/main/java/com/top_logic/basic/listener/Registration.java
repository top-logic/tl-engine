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

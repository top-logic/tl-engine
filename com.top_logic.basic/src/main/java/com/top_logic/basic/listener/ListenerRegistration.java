/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * {@link Registration} that provides access to the registered listener.
 * 
 * <p>
 * Allows a caller that collects registrations (possibly from several observables) to deliver an
 * event later on only to those listeners whose registration is still {@link #isActive() active}.
 * </p>
 * 
 * @param <L>
 *        The listener type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListenerRegistration<L> extends Registration {

	/**
	 * The registered listener, or {@code null}, if this registration is no longer
	 * {@link #isActive() active}.
	 */
	L getListener();

}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * Result of an "add listener" operation.
 * 
 * <p>
 * The {@link ListenerBinding} can be used to terminate the established listener registration using
 * the {@link #close()} method.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListenerBinding {

	/**
	 * Utility constant for a no-op binding.
	 */
	ListenerBinding NONE = () -> {
		// Ignore, no listener was registered.
	};

	/**
	 * Removes the listener registration that created this {@link ListenerBinding}.
	 */
	public void close();

}

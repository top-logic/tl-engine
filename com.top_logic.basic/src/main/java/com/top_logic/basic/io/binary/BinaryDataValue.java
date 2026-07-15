/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import com.top_logic.basic.listener.Listener;

/**
 * Observable holder of {@link BinaryData}.
 *
 * <p>
 * Multiple controls can share a single {@link BinaryDataValue}. When one control writes via
 * {@link #setData(BinaryData)}, all registered listeners are notified, keeping every observer in
 * sync automatically.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BinaryDataValue {

	/**
	 * The current binary data, or {@code null} if no data is available.
	 */
	BinaryData getData();

	/**
	 * Sets the binary data.
	 *
	 * <p>
	 * All registered listeners are notified with the new value.
	 * </p>
	 *
	 * @param data
	 *        The new binary data, or {@code null} to clear.
	 */
	void setData(BinaryData data);

	/**
	 * Registers a listener that is notified whenever the data changes.
	 *
	 * @param listener
	 *        The listener to register.
	 */
	void addListener(Listener<? super BinaryData> listener);

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeListener(Listener<? super BinaryData> listener);

}

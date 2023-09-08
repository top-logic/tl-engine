/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * Utilities for internal access to {@link PropertyObservableBase}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertyEventUtil {

	/**
	 * Allow external access to
	 * {@link PropertyObservableBase#notifyListeners(EventType, Object, Object, Object)}
	 * 
	 * <p>
	 * This accessor method is required, if code firing update events should be implemented outside
	 * the {@link PropertyObservableBase} class (e.g. in a static utility method).
	 * </p>
	 */
	public static <L extends PropertyListener, S extends PropertyObservableBase, V> void notifyListeners(
			S sender, EventType<L, ? super S, ? super V> type, V oldValue, V newValue) {
		sender.notifyListeners(type, sender, oldValue, newValue);
	}

}

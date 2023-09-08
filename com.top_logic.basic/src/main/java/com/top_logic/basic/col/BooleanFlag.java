/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Mutable wrapper for a single <code>boolean</code> value.
 * 
 * <p>
 * Note: This class is not synchronized. If an instance is used from multiple
 * threads, access must be externally synchronized.
 * </p>
 * 
 * @see Boolean for an immutable object wrapper for <code>boolean</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanFlag {

	private boolean value;

	/**
	 * Creates a new {@link BooleanFlag} with the given initial value.
	 * 
	 * @param initialValue
	 *        The value to be returned from {@link #get()}.
	 */
	public BooleanFlag(boolean initialValue) {
		this.value = initialValue;
	}

	/**
	 * The current value of this flag. 
	 */
	public boolean get() {
		return value;
	}

	/**
	 * Updates the value of this flag to the given new value.
	 */
	public void set(boolean newValue) {
		this.value = newValue;
	}

}

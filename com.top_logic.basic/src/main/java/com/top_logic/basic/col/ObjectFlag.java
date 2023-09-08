/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Mutable wrapper for an arbitrary object of type <code>T</code>. Abstraction of {@link BooleanFlag}.
 * 
 * @see BooleanFlag
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ObjectFlag<T> {

	/**
	 * current value of this flag
	 */
	private T value;

	/**
	 * creates a new {@link ObjectFlag} with the given value.
	 * 
	 * @param initialValue
	 *        the value returned by {@link #get()}
	 */
	public ObjectFlag(T initialValue) {
		this.value = initialValue;
	}

	/**
	 * Returns the current value of this flag
	 */
	public T get() {
		return value;
	}

	/**
	 * Sets the new value of this flag
	 */
	public void set(T value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return ObjectFlag.class.getName() + "[value: " + value + "]";
	}
	
}

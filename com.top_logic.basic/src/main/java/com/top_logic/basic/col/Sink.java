/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Add-only interface to a consumer/buffer.
 * 
 * @param <T>
 *        The type of values that can be buffered.
 */
public interface Sink<T> {

	/**
	 * Adds the given value to this buffer.
	 * 
	 * @param value
	 *        The value to add.
	 */
	void add(T value);

}
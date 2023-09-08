/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * {@link Value} that can be explicitly updated.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModifiableValue<T> extends Value<T> {

	/**
	 * Updates this {@link ModifiableValue} with a new value.
	 * 
	 * @param newValue
	 *        The new value of this {@link ModifiableValue}.
	 */
	void set(T newValue);

}

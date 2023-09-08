/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} that always returns a constant value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantMapping<T> implements Mapping<Object, T> {

	private final T defaultValue;

	/**
	 * Creates a {@link ConstantMapping}.
	 * 
	 * @param defaultValue
	 *        The value deliverd for all inputs.
	 */
	public ConstantMapping(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public T map(Object input) {
		return defaultValue;
	}

}

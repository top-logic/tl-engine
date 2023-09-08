/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.convert;

import com.top_logic.basic.col.Mapping;

/**
 * Converts a {@link Number} to a {@link Long}.
 * <p>
 * Returns null if the input is null.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NumberToLong implements Mapping<Number, Long> {

	/**
	 * The instance of {@link NumberToLong}.
	 */
	public static final NumberToLong INSTANCE = new NumberToLong();

	/**
	 * @param input
	 *        Is allowed to be null.
	 * @return Is null if the parameter is null.
	 */
	@Override
	public Long map(Number input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Long) {
			return (Long) input;
		}
		return input.longValue();
	}

}

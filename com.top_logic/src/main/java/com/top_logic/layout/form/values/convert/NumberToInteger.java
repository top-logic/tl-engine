/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.convert;

import com.top_logic.basic.col.Mapping;

/**
 * Converts a {@link Number} to a {@link Integer}.
 * <p>
 * Returns null if the input is null.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NumberToInteger implements Mapping<Number, Integer> {

	/**
	 * The instance of {@link NumberToInteger}.
	 */
	public static final NumberToInteger INSTANCE = new NumberToInteger();

	/**
	 * @param input
	 *        Is allowed to be null.
	 * @return Is null if the parameter is null.
	 */
	@Override
	public Integer map(Number input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Integer) {
			return (Integer) input;
		}
		return input.intValue();
	}

}

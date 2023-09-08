/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.convert;

import com.top_logic.basic.col.Mapping;

/**
 * Converts a {@link Number} to a {@link Float}.
 * <p>
 * Returns null if the input is null.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NumberToFloat implements Mapping<Number, Float> {

	/**
	 * The instance of {@link NumberToFloat}.
	 */
	public static final NumberToFloat INSTANCE = new NumberToFloat();

	@Override
	public Float map(Number input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Float) {
			return (Float) input;
		}
		return input.floatValue();
	}

}

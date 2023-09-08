/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.convert;

import com.top_logic.basic.col.Mapping;

/**
 * Converts a {@link Number} to a {@link Double}.
 * <p>
 * Returns null if the input is null.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NumberToDouble implements Mapping<Number, Double> {

	/**
	 * The instance of {@link NumberToDouble}.
	 */
	public static final NumberToDouble INSTANCE = new NumberToDouble();

	@Override
	public Double map(Number input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Double) {
			return (Double) input;
		}
		return input.doubleValue();
	}

}

/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * {@link GenericFunction} casting/parsing a {@link Number} or {@link String} to {@link Double}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToDouble extends Function1<Double, Object> {

	@Override
	public Double apply(Object arg1) {
		if (arg1 instanceof Number) {
			return ((Number) arg1).doubleValue();
		} else {
			return Double.parseDouble(arg1.toString());
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * {@link GenericFunction} multiplying two values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Mul extends Function2<Number, Number, Number> {

	@Override
	public Number apply(Number arg1, Number arg2) {
		return arg1.doubleValue() * arg2.doubleValue();
	}

}

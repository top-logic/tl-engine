/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;


/**
 * {@link GenericFunction} comparing two values and returns whether the first is greater than the
 * second.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Gt<T extends Comparable<T>> extends Function2<Boolean, T, T> {

	@Override
	public Boolean apply(T arg1, T arg2) {
		return Boolean.valueOf(compare(arg1, arg2) > 0);
	}

	static <T extends Comparable<T>> int compare(T arg1, T arg2) {
		if (arg1 instanceof Number && arg2 instanceof Number && arg1.getClass() != arg2.getClass()) {
			Number n1 = (Number) arg1;
			Number n2 = (Number) arg2;
			return Double.compare(n1.doubleValue(), n2.doubleValue());
		}

		return arg1.compareTo(arg2);
	}

}

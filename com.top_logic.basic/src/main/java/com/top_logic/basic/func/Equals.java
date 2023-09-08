/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import com.top_logic.basic.StringServices;

/**
 * {@link GenericFunction} comparing two values for equality.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Equals extends Function2<Boolean, Object, Object> {

	@Override
	public Boolean apply(Object arg1, Object arg2) {
		if (arg1 instanceof Number && arg2 instanceof Number && arg1.getClass() != arg2.getClass()) {
			Number n1 = (Number) arg1;
			Number n2 = (Number) arg2;
			return n1.doubleValue() == n2.doubleValue();
		}

		return Boolean.valueOf(StringServices.equals(arg1, arg2));
	}

}

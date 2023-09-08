/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * {@link GenericFunction} computing the size of a collection (in the widest possible sense).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Size extends Function1<Integer, Object> {

	@Override
	public Integer apply(Object arg) {
		if (arg == null) {
			return 0;
		}
		if (arg instanceof Collection<?>) {
			return ((Collection<?>) arg).size();
		}
		if (arg instanceof CharSequence) {
			return ((CharSequence) arg).length();
		}
		if (arg.getClass().isArray()) {
			return Array.getLength(arg);
		}
		return 1;
	}

}

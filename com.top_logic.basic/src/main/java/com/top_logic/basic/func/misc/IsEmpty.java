/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.GenericFunction;

/**
 * {@link GenericFunction} that tests its argument for being empty in the widest sense.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsEmpty extends Function1<Boolean, Object> {

	/**
	 * Singleton {@link IsEmpty} instance.
	 */
	public static final IsEmpty INSTANCE = new IsEmpty();

	private IsEmpty() {
		// Singleton constructor.
	}

	@Override
	public Boolean apply(Object arg) {
		return Boolean.valueOf(isEmpty(arg));
	}

	public static boolean isEmpty(Object arg) {
		if (arg == null) {
			return true;
		}
		if (arg instanceof Collection<?>) {
			return ((Collection<?>) arg).isEmpty();
		}
		if (arg instanceof Map<?, ?>) {
			return ((Map<?, ?>) arg).isEmpty();
		}
		if (arg instanceof CharSequence) {
			return ((CharSequence) arg).length() == 0;
		}
		if (arg instanceof Collection<?>) {
			return ((Collection<?>) arg).isEmpty();
		}
		if (arg instanceof Iterable<?>) {
			return !((Iterable<?>) arg).iterator().hasNext();
		}
		if (arg.getClass().isArray()) {
			return Array.getLength(arg) == 0;
		}
		return false;
	}

}

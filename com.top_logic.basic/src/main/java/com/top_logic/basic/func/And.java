/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * {@link GenericFunction} computing a boolean <code>and</code> of two values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class And extends GenericFunction<Boolean> {

	@Override
	public Boolean invoke(Object... args) {
		boolean result = true;
		for (Object arg : args) {
			result = toBoolean(arg);
			if (!result) {
				break;
			}
		}
		return Boolean.valueOf(result);
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}

	@Override
	public int getArgumentCount() {
		return -1;
	}

	static boolean toBoolean(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		if (obj instanceof Collection<?>) {
			return !((Collection<?>) obj).isEmpty();
		}
		if (obj instanceof Number) {
			return ((Number) obj).intValue() != 0;
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) > 0;
		}
		return true;
	}

}

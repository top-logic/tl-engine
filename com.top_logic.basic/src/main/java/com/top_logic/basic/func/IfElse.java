/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * {@link GenericFunction} testing for a boolean condition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IfElse extends Function3<Object, Object, Object, Object> {

	@Override
	public Object apply(Object arg1, Object arg2, Object arg3) {
		if (And.toBoolean(arg1)) {
			return arg2;
		} else {
			return arg3;
		}
	}

}

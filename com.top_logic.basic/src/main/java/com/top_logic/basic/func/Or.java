/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;


/**
 * {@link GenericFunction} computing a boolean <code>or</code> of two values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Or extends GenericFunction<Boolean> {

	@Override
	public Boolean invoke(Object... args) {
		boolean result = false;
		for (Object arg : args) {
			result = And.toBoolean(arg);
			if (result) {
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

}

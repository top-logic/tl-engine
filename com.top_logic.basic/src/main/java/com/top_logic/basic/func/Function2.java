/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * Base class for two-argument functions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Function2<R, A1, A2> extends GenericFunction<R> implements IFunction2<R, A1, A2> {

	@Override
	public final R invoke(Object... args) {
		A1 arg1 = arg(0, args);
		A2 arg2 = arg(1, args);
		return apply(arg1, arg2);
	}

	@Override
	public final int getArgumentCount() {
		return 2;
	}

	@Override
	public final boolean hasVarArgs() {
		return false;
	}

}

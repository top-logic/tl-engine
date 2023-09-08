/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;


/**
 * Base class for no-argument functions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Function0<R> extends GenericFunction<R> implements IFunction0<R> {

	@Override
	public final R invoke(Object... args) {
		return apply();
	}

	@Override
	public final int getArgumentCount() {
		return 0;
	}

	@Override
	public final boolean hasVarArgs() {
		return false;
	}

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import java.util.function.Function;

import com.top_logic.basic.col.Mapping;

/**
 * Base class for one-argument functions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Function1<R, A> extends GenericFunction<R> implements IFunction1<R, A>, Mapping<A, R> {

	@Override
	public final R invoke(Object... args) {
		A arg1 = arg(0, args);
		return apply(arg1);
	}

	@Override
	public final R map(A input) {
		return apply(input);
	}

	@Override
	public final int getArgumentCount() {
		return 1;
	}

	@Override
	public final boolean hasVarArgs() {
		return false;
	}

	/**
	 * See: {@link Function#apply(Object)}
	 * <p>
	 * This override is needed for Javac. Without it, java reports the following error:
	 * "Function1.java:13: error: class <xmp>Function1<R,A></xmp> inherits abstract and default for
	 * apply(A) from types Mapping and IFunction1"
	 * </p>
	 * <p>
	 * Apparently JavaC is correct according to the
	 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.4.1.3">Java
	 * Specification</a>.
	 * </p>
	 */
	@Override
	public R apply(A arg) {
		return Mapping.super.apply(arg);
	}

}

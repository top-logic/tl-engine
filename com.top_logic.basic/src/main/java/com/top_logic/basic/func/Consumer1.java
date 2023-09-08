/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * Base class for functions taking one parameter and returning nothing.
 * <p>
 * Implemented to avoid that all consumers have to end with "return null;".
 * </p>
 * <p>
 * Named like the Java 8 interface, but with a suffix indicating the number of parameters, like all
 * TL function types. ({@link IFunction1}, {@link Function1}, ...)
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class Consumer1<T> extends Function1<Void, T> {

	@Override
	public Void apply(T arg) {
		accept(arg);
		return null;
	}

	/**
	 * Named after the method in the Java 8 "Consumer" interface.
	 */
	protected abstract void accept(T arg);

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * The identity {@link Function1} returning its argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Identity<A> extends Function1<A, A> {

	/**
	 * Singleton {@link Identity} instance.
	 * <p>
	 * If the type parameter matters, use {@link #getInstance()}.
	 * </p>
	 */
	public static final Identity<Object> INSTANCE = new Identity<>();

	/** Getter for the singleton {@link Identity} instance. */
	@SuppressWarnings("unchecked") // Okay for the identity function because of its very nature.
	public static final <T> Identity<T> getInstance() {
		return (Identity<T>) INSTANCE;
	}

	private Identity() {
		// Singleton constructor.
	}

	@Override
	public A apply(A arg) {
		return arg;
	}
}
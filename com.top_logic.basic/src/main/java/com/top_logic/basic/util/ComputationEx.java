/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * {@link Runnable} that can return a value.
 * 
 * <p>
 * Note: To be able to allow more than one exception to be tunneled from the computation to the
 * caller of the indirection, APIs should declare {@link ComputationEx2} as callback interface.
 * </p>
 * 
 * @see ComputationEx2
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComputationEx<T, E extends Throwable> extends ComputationEx2<T, E, RuntimeException> {

	/**
	 * Computes a value and returns it.
	 */
	@Override
	T run() throws E;
	
}

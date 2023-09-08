/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * {@link Runnable} that can return a value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComputationEx2<T, E1 extends Throwable, E2 extends Throwable> {

	/**
	 * Computes a value and returns it.
	 */
	T run() throws E1, E2;
	
}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

/**
 * Alternative {@link Runnable} interface for defining a {@link ThreadContext#inContext(InContext)}
 * callback.
 * 
 * <p>
 * This interface is used instead of {@link Runnable}, since in many cases {@link Runnable} is
 * already implemented for other reasons.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InContext {

	/**
	 * The code to execute in a certain {@link ThreadContext}.
	 * 
	 * @see ThreadContext#inContext(InContext)
	 */
	void inContext();

}
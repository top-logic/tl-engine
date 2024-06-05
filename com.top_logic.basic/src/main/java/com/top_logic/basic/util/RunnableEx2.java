/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * {@link Runnable} that can throw two types of exceptions.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface RunnableEx2<E1 extends Throwable, E2 extends Throwable> {

	/** Does something which might throw exceptions. */
	void run() throws E1, E2;

}

/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.thread.ThreadContext;

/**
 * {@link TestSetup} that executes the {@link #doSetUp()} and {@link #doTearDown()} within a
 * {@link ThreadContext}.
 * 
 * @see ThreadContextDecorator
 * @see RearrangableThreadContextSetup
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ThreadContextSetup extends DecoratedTestSetup {

	/**
	 * Creates a new {@link ThreadContextSetup}.
	 * 
	 * @param test
	 *        see {@link #getTest()}
	 */
	public ThreadContextSetup(Test test) {
		super(ThreadContextDecorator.INSTANCE, test);
	}

}


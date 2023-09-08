/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.thread.ThreadContext;

/**
 * A {@link RearrangableTestSetup} that executes {@link #doSetUp()} and {@link #doTearDown()} within
 * a {@link ThreadContext}.
 * 
 * @see ThreadContextDecorator
 * @see ThreadContextSetup
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RearrangableThreadContextSetup extends RearrangableTestSetup {

	/**
	 * Creates a new {@link RearrangableThreadContextSetup}.
	 * 
	 * @see RearrangableThreadContextSetup#RearrangableThreadContextSetup(TestSetupDecorator, Test,
	 *      MutableInteger)
	 */
	public RearrangableThreadContextSetup(Test test, MutableInteger setupCnt) {
		this(null, test, setupCnt);
	}

	/**
	 * Creates a new {@link RearrangableThreadContextSetup}.
	 * 
	 * <p>
	 * The given decorator is executed after the {@link ThreadContextDecorator}, i.e. within the
	 * additional {@link TestSetupDecorator} a {@link ThreadContext} is available.
	 * </p>
	 * 
	 * @see RearrangableTestSetup#RearrangableTestSetup(TestSetupDecorator, Test, MutableInteger)
	 */
	public RearrangableThreadContextSetup(TestSetupDecorator additionalDecorator, Test test, MutableInteger setupCnt) {
		super(join(ThreadContextDecorator.INSTANCE, additionalDecorator), test, setupCnt);
	}

}


/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.col.MutableInteger;

/**
 * The class {@link RearrangableTestSetup} is a {@link TestSetup} which allows
 * to merge the tests of other {@link RearrangableTestSetup} in which have an
 * equal {@link #configKey() configuration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RearrangableTestSetup extends NestableTestSetup {

	/**
	 * Creates an undecorated {@link RearrangableTestSetup}.
	 *
	 * @see #RearrangableTestSetup(TestSetupDecorator, Test, MutableInteger)
	 */
	public RearrangableTestSetup(Test test, MutableInteger setupCnt) {
		this(null, test, setupCnt);
	}
	
	/**
	 * Creates a {@link RearrangableTestSetup}.
	 *
	 * @param test The test to wrap.
	 * @param setupCnt The setup counter to use for prevention of multiple nested setups.
	 */
	public RearrangableTestSetup(TestSetupDecorator decorator, Test test, MutableInteger setupCnt) {
		super(decorator, test, setupCnt);
	}
	
	/**
	 * The {@link #configKey()} will be used to identify {@link TestSetup test
	 * setups} which can be merged, e.g. if there is a rearrangable test setup
	 * <i>testSetup</i> having an inner test <i>t_1</i> and a
	 * {@link #configKey()} <i>a</i> and another rearrangable test setup with
	 * inner test <i>t_2</i> with a {@link #configKey()} which equals <i>a</i>
	 * then <i>t_1</i> and <i>t_2</i> can be run in the same setup.
	 * 
	 */
	public Object configKey() {
		return getClass();
	}
	
	void setTest(Test decoratedTest) {
		fTest = decoratedTest;
	}

}

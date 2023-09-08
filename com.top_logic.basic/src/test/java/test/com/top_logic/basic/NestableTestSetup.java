/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;


import java.util.HashMap;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.col.MutableInteger;

/**
 * {@link TestSetup} that is only executed once if a test is wrapped multiple
 * times with the same type of {@link NestableTestSetup}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NestableTestSetup extends DecoratedTestSetup {

	private final MutableInteger setupCnt;

	/**
	 * Creates an undecorated {@link NestableTestSetup}.
	 *
	 * @see #NestableTestSetup(TestSetupDecorator, Test, MutableInteger)
	 */
	public NestableTestSetup(Test test, MutableInteger setupCnt) {
		this(null, test, setupCnt);
	}
	
	/**
	 * Creates a {@link NestableTestSetup}.
	 * 
	 * @param decorator
	 *        See
	 *        {@link DecoratedTestSetup#DecoratedTestSetup(TestSetupDecorator, Test)}.
	 * @param test
	 *        See {@link TestSetup#TestSetup(Test)}.
	 * @param setupCnt
	 *        The counter that determines whether a setup is wrapped multiple
	 *        times. The same type of {@link NestableTestSetup} must use the
	 *        same instance of this counter. Typically, the counter is a
	 *        singleton in the concrete setup class.
	 */
	public NestableTestSetup(TestSetupDecorator decorator, Test test, MutableInteger setupCnt) {
		super(decorator, test);
		
		this.setupCnt = setupCnt;
	}

	@Override
	protected final void setUp() throws Exception {
		if (setupCnt.inc() == 1) {
			boolean success = false;
			try {
				super.setUp();
				success = true;
			} catch (Exception ex) {
				String testName = TestUtils.computeTestName(getTest());
				throw (AssertionError) new AssertionError("Setup failed for test '" + testName + "'. Cause: "
					+ ex.getMessage()).initCause(ex);
			} finally {
				if (! success) {
					setupCnt.dec();
				}
			}
		}
	}

	@Override
	protected final void tearDown() throws Exception {
		if (setupCnt.dec() == 0) {
			super.tearDown();
		}
	}
	
	/**
	 * Object to provide {@link MutableInteger} for more than one object.
	 * 
	 * <p>
	 * Create a new instance with {@link NestableTestSetup#newMultipleCounter()}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected interface MultipleSetupCounter {
		
		/**
		 * Fetches of create the counter for the given key.
		 */
		MutableInteger getCounterFor(Object key);
		
	}
	
	/**
	 * Creates a new {@link MultipleSetupCounter}.
	 * 
	 * <p>
	 * Use this with following pattern:
	 * 
	 * <pre>
	 * class X extends NestableTestSetup {
	 * 
	 *   private static SETUP_CNT = newMultipleCounter();
	 * 
	 *   public X(Test t, Object additional) {
	 *     super(t, SETUP_CNT.getCounterFor(additional));
	 *   }
	 *   ...
	 * }
	 * </pre>
	 * </p>
	 * 
	 */
	protected static final MultipleSetupCounter newMultipleCounter() {
		return new MultipleSetupCounterImpl();
	}

	static class MultipleSetupCounterImpl extends HashMap<Object, MutableInteger> implements
			MultipleSetupCounter {

		@Override
		public MutableInteger getCounterFor(Object key) {
			MutableInteger setupCnt = get(key);
			if (setupCnt == null) {
				setupCnt = new MutableInteger();
				put(key, setupCnt);
			}
			return setupCnt;
		}

	}

}

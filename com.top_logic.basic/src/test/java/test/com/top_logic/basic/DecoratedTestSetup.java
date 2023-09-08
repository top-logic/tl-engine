/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.TestSetupDecorator.SetupAction;

import com.top_logic.basic.col.Maybe;

/**
 * {@link TestSetup} that itself can be decorated with a {@link TestSetupDecorator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DecoratedTestSetup extends NamedTestSetup implements TestSetupDecorator.SetupAction {

	private final TestSetupDecorator _decorator;

	/**
	 * Creates a {@link DecoratedTestSetup}.
	 * 
	 * @param decorator
	 *        The {@link TestSetupDecorator} to use, can be <code>null</code> to
	 *        perform an undecorated setup.
	 * @param test
	 *        See {@link TestSetup#TestSetup(Test)}.
	 */
	public DecoratedTestSetup(TestSetupDecorator decorator, Test test) {
		super(test);
		
		_decorator = decorator;
	}

	private TestSetupDecorator decorator() {
		Maybe<TestSetupDecorator> loggingDecorator = LoggingTestSetup.getDecorator();
		if (!loggingDecorator.hasValue()) {
			return _decorator;
		}
		if (_decorator == null) {
			return loggingDecorator.get();
		}
		return join(loggingDecorator.get(), _decorator);
		
	}
	
	@Override
	protected void setUp() throws Exception {
		TestSetupDecorator decorator = decorator();
		if (decorator == null) {
			this.setUpDecorated();
		} else {
			decorator.setup(this);
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		TestSetupDecorator decorator = decorator();
		if (decorator == null) {
			this.tearDownDecorated();
		} else {
			decorator.tearDown(this);
		}
	}
	
	@Override
	public final void setUpDecorated() throws Exception {
		doSetUp();
	}
	
	protected abstract void doSetUp() throws Exception;
	
	@Override
	public final void tearDownDecorated() throws Exception {
		this.doTearDown();
	}

	protected abstract void doTearDown() throws Exception;

	@Override
	public final DecoratedTestSetup decoratedTest() {
		return this;
	}

	/**
	 * Factory for a combined {@link TestSetupDecorator}.
	 * 
	 * @param decorators
	 *        The {@link TestSetupDecorator} to wrap into each other, outer
	 *        first.
	 * @return The combined {@link TestSetupDecorator}.
	 */
	public static TestSetupDecorator join(TestSetupDecorator... decorators) {
		if (decorators == null || decorators.length == 0) {
			return null;
		}
		
		else if (decorators.length == 1) {
			return decorators[0];
		}
		
		else {
			TestSetupDecorator result = decorators[decorators.length - 1];
			for (int n = decorators.length - 2; n >= 0; n++) {
				result = join(decorators[n], result);
			}
			return result;
		}
	}

	/**
	 * Factory for a combined {@link TestSetupDecorator}.
	 * 
	 * @param outer
	 *        The {@link TestSetupDecorator} that is run first, may be
	 *        <code>null</code>.
	 * @param inner
	 *        The {@link TestSetupDecorator} that is run last, may be
	 *        <code>null</code>.
	 * @return The combined {@link TestSetupDecorator} or <code>null</code> if
	 *         both <code>outer</code> and <code>inner</code> are
	 *         <code>null</code>.
	 */
	public static TestSetupDecorator join(final TestSetupDecorator outer, final TestSetupDecorator inner) {
		if (outer == null) return inner;
		if (inner == null) return outer; 
		
		return new JoinedDecorator(inner, outer);
	}

	/**
	 * {@link TestSetupDecorator} that is the combination of two other
	 * {@link TestSetupDecorator}s
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class JoinedDecorator implements TestSetupDecorator {
		private final TestSetupDecorator inner;

		private final TestSetupDecorator outer;

		JoinedDecorator(TestSetupDecorator inner, TestSetupDecorator outer) {
			this.inner = inner;
			this.outer = outer;
		}

		@Override
		public void setup(final SetupAction innerSetup) throws Exception {
			outer.setup(new DecoratedSetupAction(inner, innerSetup));
		}

		@Override
		public void tearDown(SetupAction innerSetup) throws Exception {
			outer.tearDown(new DecoratedSetupAction(inner, innerSetup));
		}
	}

	/**
	 * {@link TestSetupDecorator.SetupAction} that represents a decorated
	 * {@link TestSetupDecorator.SetupAction}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class DecoratedSetupAction implements TestSetupDecorator.SetupAction {
		private final SetupAction action;

		private final TestSetupDecorator decorator;
		
		DecoratedSetupAction(TestSetupDecorator decorator, SetupAction action) {
			this.action = action;
			this.decorator = decorator;
		}

		@Override
		public void setUpDecorated() throws Exception {
			decorator.setup(action);
		}

		@Override
		public void tearDownDecorated() throws Exception {
			decorator.tearDown(action);
		}

		@Override
		public DecoratedTestSetup decoratedTest() {
			return action.decoratedTest();
		}
	}

}

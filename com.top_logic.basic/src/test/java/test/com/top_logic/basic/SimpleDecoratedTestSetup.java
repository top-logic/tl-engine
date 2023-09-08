/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;

/**
 * A {@link DecoratedTestSetup} which is just an adapter to apply a
 * {@link TestSetupDecorator} to some test.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleDecoratedTestSetup extends DecoratedTestSetup {

	/**
	 * @param decorator
	 *        must no be <code>null</code>
	 * 
	 * @see DecoratedTestSetup#DecoratedTestSetup(TestSetupDecorator, Test)
	 */
	public SimpleDecoratedTestSetup(TestSetupDecorator decorator, Test test) {
		super(checkNotNull(decorator), test);
	}

	/**
	 * Checks that the given {@link TestSetupDecorator decorator} is not
	 * <code>null</code>.
	 * 
	 * @param decorator
	 *        the {@link TestSetupDecorator} which is checked to be not
	 *        <code>null</code>.
	 * 
	 * @return a reference to the given decorator
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given decorator is <code>null</code>
	 */
	private static final TestSetupDecorator checkNotNull(TestSetupDecorator decorator) throws IllegalArgumentException {
		if (decorator == null) {
			throw new IllegalArgumentException("Must not create " + SimpleDecoratedTestSetup.class.getName() + " with null decorator!");
		}
		return decorator;
	}

	@Override
	protected void doSetUp() throws Exception {
	}

	@Override
	protected void doTearDown() throws Exception {
	}

}

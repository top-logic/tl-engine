/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import junit.framework.Test;

import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess.PoolRef;

import com.top_logic.basic.col.TupleFactory;

/**
 * {@link RearrangableTestSetup} changing the default connection pool.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SwitchDefaultPoolSetup extends RearrangableTestSetup {

	private static MultipleSetupCounter SETUP_CNT = newMultipleCounter();

	private final String _newDefaultPool;

	private PoolRef _formerDefaultPool;

	/**
	 * Creates a new {@link SwitchDefaultPoolSetup}.
	 */
	public SwitchDefaultPoolSetup(Test test, String newDefaultPool) {
		super(test, SETUP_CNT.getCounterFor(newDefaultPool));
		setName(SwitchDefaultPoolSetup.class.getName() + " " + newDefaultPool);
		_newDefaultPool = newDefaultPool;
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _newDefaultPool);
	}

	@Override
	protected void doSetUp() throws Exception {
		_formerDefaultPool = TestingConnectionPoolRegistryAccess.setupDefaultPool(_newDefaultPool);

	}

	@Override
	protected void doTearDown() throws Exception {
		TestingConnectionPoolRegistryAccess.restoreDefaultPool(_formerDefaultPool);
		_formerDefaultPool = null;
	}

}


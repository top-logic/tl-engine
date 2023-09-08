/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import junit.framework.Test;

import test.com.top_logic.basic.sql.TestingConnectionPool;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Superclass for {@link DBKnowledgeBaseTestSetup} that work with two {@link KnowledgeBase}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMultiKBTestSetup extends DBKnowledgeBaseTestSetup {

	private static DBKnowledgeBase _kbNode2;

	public AbstractMultiKBTestSetup(Test test) {
		super(test);
	}

	@Override
	public void setUpLocal() throws Exception {
		super.setUpLocal();
		KnowledgeBaseConfiguration config = createKBConfigItem("testNode2", getTestDBNode2());
		_kbNode2 = setupSecondKB(config);
	}

	protected abstract DBKnowledgeBase setupSecondKB(KnowledgeBaseConfiguration config) throws Exception;

	@Override
	public void tearDownLocal() throws Exception {
		super.tearDownLocal();
		teardownSecondKB(_kbNode2);
		_kbNode2 = null;
	}

	protected abstract String getTestDBNode2();

	protected abstract void teardownSecondKB(DBKnowledgeBase kb) throws Exception;

	protected static void setKBNode2(DBKnowledgeBase kb) {
		_kbNode2 = kb;
	}

	/**
	 * Access to the {@link KnowledgeBase} on the simulated second cluster node.
	 */
	public static DBKnowledgeBase kbNode2() {
		return _kbNode2;
	}

	public static void simulateConnectionBreakdownNode2() throws SQLException {
		ConnectionPool pool = kbNode2().getConnectionPool();
		((TestingConnectionPool) pool).simulateReadConnectionBreakdown();
	}

}

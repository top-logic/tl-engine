/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.knowledge.service.db2.PersistentIdFactory;

/**
 * Test case for {@link PersistentIdFactory}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPersistentIdFactory extends BasicTestCase {

	static final String SEQUENCE_NAME = TestPersistentIdFactory.class.getSimpleName();

	public void testCreateIds() throws SQLException, InterruptedException {
		final ConnectionPool connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		final DBHelper dbHelper = connectionPool.getSQLDialect();

		final int concurrency = 10;
		final int idCnt = 10000;
		final long[][] ids = new long[concurrency][];

		parallelTest(concurrency, new ExecutionFactory() {
			@Override
			public Execution createExecution(int id) {
				final long[] localIds = new long[idCnt];
				ids[id] = localIds;

				return new Execution() {
					@Override
					public void run() throws Exception {
						PersistentIdFactory idFactory =
							new PersistentIdFactory(connectionPool, SEQUENCE_NAME);

						for (int n = 0; n < idCnt; n++) {
							localIds[n] = idFactory.createId();
						}
					}
				};
			}
		});

		Set<Long> allIds = new HashSet<>();
		for (long[] localIds : ids) {
			for (long id : localIds) {
				assertTrue(allIds.add(id));
			}
		}

	}

	public static Test suite() {
		TestFactory factory = DefaultTestFactory.INSTANCE;
		factory = DropSequenceSetup.dropSequences(factory, SEQUENCE_NAME);
		Test t = DatabaseTestSetup.getDBTest(TestPersistentIdFactory.class, factory);
		t = TLTestSetup.createTLTestSetup(t);
		return t;
	}

}

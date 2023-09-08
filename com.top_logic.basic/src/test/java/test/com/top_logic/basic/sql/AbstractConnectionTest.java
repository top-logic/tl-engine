/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;

/**
 * The class {@link AbstractConnectionTest} is an abstract superclass to create Tests using {@link ConnectionSetup}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConnectionTest extends BasicTestCase {
	
	private ConnectionPool pool;
	private DBHelper dbh;
	private PooledConnection connection;
	
	public AbstractConnectionTest() {
	}

	public AbstractConnectionTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		dbh = pool.getSQLDialect();
		connection = pool.borrowWriteConnection();
	}
	
	@Override
	protected void tearDown() throws Exception {
		pool.releaseWriteConnection(connection);
		connection = null;
		dbh = null;
		pool = null;
		super.tearDown();
	}

	protected DBHelper getSQLDialect() {
		return dbh;
	}

	protected PooledConnection getConnection() {
		return connection;
	}

	protected static Test suite(Class<? extends AbstractConnectionTest> testClass, DBType dbType, TestFactory fac) {
		return BasicTestSetup.createBasicTestSetup(ConnectionSetup.createSuite(testClass, dbType, fac));
	}

	protected static Test suite(Class<? extends AbstractConnectionTest> testClass, TestFactory fac) {
		return BasicTestSetup.createBasicTestSetup(ConnectionSetup.createSuite(testClass, fac));
	}
	
	protected static Test suite(Class<? extends AbstractConnectionTest> testClass) {
		return suite(testClass, DefaultTestFactory.INSTANCE);
	}
}


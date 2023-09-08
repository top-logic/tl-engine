/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.SQLException;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup;

import com.top_logic.basic.sql.AbstractConfiguredConnectionPool.ConnectionType;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPool.PooledConnectionFactory;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case for {@link PooledConnectionFactory}.
 * 
 * @author    <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class TestPooledConnectionFactory extends BasicTestCase {

	private PooledConnectionFactory factory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		this.factory = new PooledConnectionFactory(defaultPool, ConnectionType.READ);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.factory = null;
		
		super.tearDown();
	}
	
	public void testValidate() throws SQLException {
		PooledConnection connection = (PooledConnection) factory.makeObject();
		try {
			assertNotNull(connection);

			factory.activateObject(connection);
			assertTrue(factory.validateObject(connection));
			assertTrue(factory.validateObject(connection));

			TestingConnectionPool.breakConnection(connection);

			assertFalse(factory.validateObject(connection));
			assertFalse(factory.validateObject(connection));
		} finally {
			factory.destroyObject(connection);
		}
	}

    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(DatabaseTestSetup.getDBTest(TestPooledConnectionFactory.class));
    }
	
}

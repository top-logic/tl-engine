/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema.properties;

import java.sql.SQLException;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dsa.DataAccessService;

/** 
 * Test for the {@link DBProperties}.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@SuppressWarnings("javadoc")
public class TestDBProperties extends BasicTestCase {

	private ConnectionPool _pool;

	private PooledConnection _writeConnection;

	private PooledConnection _readConnection;

    /**
     * Constructor for TestNewDataManager.
     */
    public TestDBProperties(String name) {
        super(name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_writeConnection = _pool.borrowWriteConnection();
		_readConnection = _pool.borrowReadConnection();
	}

	@Override
	protected void tearDown() throws Exception {
		_pool.releaseWriteConnection(_writeConnection);
		_writeConnection = null;
		_pool.releaseReadConnection(_readConnection);
		_readConnection = null;
		_pool = null;
		super.tearDown();
	}

	public void testGetPropertiesForNode() throws SQLException {
		DBProperties dbProperties = new DBProperties(_pool);
		dbProperties.setProperty("prop1", "val1");
		dbProperties.setProperty("prop2", "val2");
		dbProperties.setProperty("node1", "prop1", "val2");

		Map<String, String> globalProps =
			DBProperties.getPropertiesForNode(_readConnection, DBProperties.GLOBAL_PROPERTY);
		assertEquals(2, globalProps.size());
		assertEquals("val1", globalProps.get("prop1"));
		assertEquals("val2", globalProps.get("prop2"));

		Map<String, String> nodeProps = DBProperties.getPropertiesForNode(_readConnection, "node1");
		assertEquals(1, nodeProps.size());
		assertEquals("val2", nodeProps.get("prop1"));
	}

	public void testGetProperties() throws SQLException {
		Map<String, String> values;
		DBProperties dbProperties = new DBProperties(_pool);
		String property = "testGetProperties1";
		String property2 = "testGetProperties2";

		assertEquals(0, DBProperties.getProperties(_readConnection, DBProperties.GLOBAL_PROPERTY).size());

		values = DBProperties.getProperties(_readConnection, DBProperties.GLOBAL_PROPERTY, property);
		assertEquals(1, values.size());
		assertEquals(null, values.get(property));
		assertTrue(values.containsKey(property));

		dbProperties.setProperty(property, "value1");
		values = DBProperties.getProperties(_readConnection, DBProperties.GLOBAL_PROPERTY, property);
		assertEquals(1, values.size());
		assertEquals("value1", values.get(property));

		values = DBProperties.getProperties(_readConnection, DBProperties.GLOBAL_PROPERTY, property, property2);
		assertEquals(2, values.size());
		assertEquals("value1", values.get(property));
		assertEquals(null, values.get(property2));
		assertTrue(values.containsKey(property2));

		dbProperties.setProperty(property2, "value2");
		values = DBProperties.getProperties(_readConnection, DBProperties.GLOBAL_PROPERTY, property, property2);
		assertEquals(2, values.size());
		assertEquals("value1", values.get(property));
		assertEquals("value2", values.get(property2));

		dbProperties.setProperty(property, null);
		dbProperties.setProperty(property2, null);

	}

	public void testCompareAndSet() throws SQLException {
		String property = "testCompareAndSet";

		// test insert
		boolean set1 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, null, "foo");
		assertTrue(set1);

		assertEquals("foo", DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));
		_writeConnection.commit();
		assertEquals("foo", DBProperties.getProperty(_readConnection, DBProperties.GLOBAL_PROPERTY, property));

		boolean set2 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, null, "foo2");
		assertFalse(set2);
		assertEquals("foo", DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));

		// test noOp update
		boolean set3 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, "foo", "foo");
		assertTrue(set3);
		assertEquals("foo", DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));

		// test update
		boolean set4 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, "foo", "foo2");
		assertTrue(set4);
		assertEquals("foo2", DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));
		_writeConnection.commit();
		assertEquals("foo2", DBProperties.getProperty(_readConnection, DBProperties.GLOBAL_PROPERTY, property));

		boolean set5 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, "foo", "foo3");
		assertFalse(set5);
		assertEquals("foo2", DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));

		// test delete
		boolean set6 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, "foo2", null);
		assertTrue(set6);
		assertEquals(null, DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));
		_writeConnection.commit();
		assertEquals(null, DBProperties.getProperty(_readConnection, DBProperties.GLOBAL_PROPERTY, property));

		// test noOp delete
		boolean set7 =
			DBProperties.compareAndSet(_writeConnection, DBProperties.GLOBAL_PROPERTY, property, null, null);
		assertTrue(set7);
		assertEquals(null, DBProperties.getProperty(_writeConnection, DBProperties.GLOBAL_PROPERTY, property));

	}

    public void testSetProperties() throws Exception {
		DBProperties dbProperties = new DBProperties(_pool);

		String theProperty = dbProperties.getProperty("testProp");

        assertNull("Invalid test configuration, property 'testProp' is already set!", theProperty);
        
        try {
			dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, null, "testValue");

            fail("Invalid handling of key 'null', expected IllegalArgumentException here!");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }

		dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, "testProp", "testValue");
		theProperty = dbProperties.getProperty("testProp");
        assertEquals("Property 'testProp' has been set wrong!", "testValue", theProperty);
        
		dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, "testProp", null);
		theProperty = dbProperties.getProperty("testProp");
        assertEquals("Property 'testProp' has been set wrong!", null, theProperty);
    }

    /**
     * the suite of tests to perform
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(
        	DatabaseTestSetup.getDBTest(TestDBProperties.class, new TestFactory() {
				@Override
				public Test createSuite(Class<? extends Test> testCase, String suiteName) {
					Test test = new TestSuite(testCase);
					test = DBPropertiesTableSetup.setup(test);
					test = ServiceTestSetup.createSetup(test, DataAccessService.Module.INSTANCE);
					return ServiceTestSetup.withThreadContext(test);
				}
			}));
    }

}
/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess.PoolRef;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Environment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Set up a databases related Tests using different {@link ConnectionPool}s.
 * 
 * @see #getDBTest(Class, TestFactory) Creating a test suite that is run with
 *      all configured databases.
 * @see #getDBTest(Test, DatabaseTestSetup.DBType) Wrapping a test to run with a
 *      given database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DatabaseTestSetup extends RearrangableThreadContextSetup {

	/**
	 * Name of the environment or system variable to set to "true" to test database tests only with
	 * the default database.
	 * 
	 * <p>
	 * This variable is also used in base-build.xml.
	 * </p>
	 */
	public static final String ONLY_DEFAULT_DB_PROPERTY = "tl_test_onlyDefaultDB";

	/**
	 * Name of the environment or system variable to set to the name of the default database to use
	 * in tests.
	 * 
	 * <p>
	 * Values must be the {@link DBType#getExternalName() name} of a {@link DBType}.
	 * </p>
	 */
	private static final String DEFAULT_DB_PROPERTY = "tl_test_defaultDB";

	/**
	 * {@link ConnectionPool} name of the "second" unit test database that is intalled by this
	 * setup.
	 */
	public static final String MIGRATION_POOL_NAME = "testMigration";

	/**
     * Constants referencing a database for test.
     */
	public static enum DBType implements ExternallyNamed {
		/** Representation of a MySQL database */
		MYSQL_DB("mysql"),
		/** Representation of a H2 database */
		H2_DB("h2"),
		/** Representation of an ORACLE database */
		ORACLE_DB("oracle"),
		/** Representation of an ORACLE12 database */
		ORACLE12_DB("oracle12"),
		/** Representation of an ORACLE19 database */
		ORACLE19_DB("oracle19"),
		/** Representation of a Microsoft MSSQL database */
		MSSQL_DB("mssql"),
		/** Representation of an IBM DB2 database */
		DB2_DB("db2"),
		/** Representation of a PostgreSQL database */
		POSTGRESQL_DB("postgresql");

        /** Name to use for {@link ConnectionPoolRegistry#getConnectionPool(String)} */
		private final String _poolName;

		private final String _externalName;
        
		DBType(String poolName) {
			this(poolName, poolName);
        }
        
		DBType(String externalName, String poolName) {
			_externalName = externalName;
			_poolName = poolName;
		}

        @Override
		public String toString() {
			return getExternalName();
		}

		/**
		 * The name of the {@link ConnectionPool}.
		 */
		public String poolName() {
			return _poolName;
        }

		@Override
		public String getExternalName() {
			return _externalName;
		}

    }

	/**
	 * The database to test with, if {@link #ONLY_DEFAULT_DB_PROPERTY} is set to <code>true</code>.
	 */
	public static final DBType DEFAULT_DB = defaultDBType();
    
	private static MutableInteger setupCnt = new MutableInteger();

	/**
	 * Holds a reference to the connection pool that was installed before this test was set up.
	 * 
	 * <p>
	 * The global pool is reinstalled after this test is finished.
	 * </p>
	 */
	private PoolRef globalDefaultDB;
	private ConnectionPool globalMigrationDB;
	private boolean migrationDBChanged;
	
	/**
	 * The database type this test is run with.
	 */
	private final DBType dbType;
	
	private static DBType currentType;
	
	/**
	 * Protected constructor for super classes.
	 * 
	 * @see #getDBTest(Test, DatabaseTestSetup.DBType) Wrapping a test with
	 *      a {@link DatabaseTestSetup}.
	 */
	private DatabaseTestSetup(Test test, DBType dbType) {
		super(test, setupCnt);
		setName(getClass().getSimpleName() + " " + dbType.name());
		this.dbType = dbType;
	}
	
	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), dbType);
	}
	
	@Override
	protected void doSetUp() throws Exception {
		try {
			currentType = dbType;
			String newDefaultPool = getDefaultDB(dbType);
			globalDefaultDB = TestingConnectionPoolRegistryAccess.setupDefaultPool(newDefaultPool);
			setupMigrationDB();
			dropAllTables(ConnectionPoolRegistry.getConnectionPool(newDefaultPool));
		} catch (Throwable ex) {
			// If if setup crashes with an exception, teardown is not called. Therefore, global
			// state must be reset directly.
			revertDefaultPool();
			throw ex;
		}
	}

	private void dropAllTables(ConnectionPool pool) throws SQLException {
		PooledConnection writeConnection = pool.borrowWriteConnection();
		try {
			try (Statement dropStmt = writeConnection.createStatement()) {
				drop(writeConnection, dropStmt, "TABLE");
				drop(writeConnection, dropStmt, "VIEW");
				writeConnection.commit();
			}
		} finally {
			pool.releaseWriteConnection(writeConnection);
		}
	}

	private void drop(PooledConnection writeConnection, Statement dropStmt, String type) throws SQLException {
		DatabaseMetaData metaData = writeConnection.getMetaData();
		String catalog = writeConnection.getCatalog();
		String schemaPattern = writeConnection.getSQLDialect().getCurrentSchema(writeConnection);
		String tableNamePattern = "%";

		boolean isDebugEnabled = Logger.isDebugEnabled(DatabaseTestSetup.class);
		try (ResultSet tables = metaData.getTables(catalog, schemaPattern, tableNamePattern, new String[] { type })) {
			String TABLE_NAME = "TABLE_NAME";
			while (tables.next()) {
				String tableName = tables.getString(TABLE_NAME);
				if (isDebugEnabled) {
					Logger.debug("Dropping " + type + " " + tableName, DatabaseTestSetup.class);
				}
				try {
					// Note: `tableType` from the meta data must not be used for constructing the
					// statement. At least h2 reports as table type "BASE TABLE", but does this
					// phrase not understand in the DROP syntax.
					dropStmt.execute("DROP " + type + " " + writeConnection.getSQLDialect().tableRef(tableName));
				} catch (SQLException ex) {
					switch (ex.getErrorCode()) {
						case 3701:
							// deleting not allowed: potentially a system table (e.g. in
							// MSSQL)
						case -204:
							// deleting not allowed: potentially a system table (e.g. in
							// DB2)
							if (isDebugEnabled) {
								Logger.debug(
									"Dropping " + type + " " + tableName + " failed: " + ex.getMessage(),
									DatabaseTestSetup.class);
							}
							break;
						default:
							throw ex;
					}
				}
			}
		}
	}

	private void setupMigrationDB() throws SQLException {
		final String migrationPoolName = getMigrationPoolName(dbType);
	    if (TestingConnectionPoolRegistryAccess.hasConnectionPool(migrationPoolName)) {
	    	final ConnectionPool migrationPool = ConnectionPoolRegistry.getConnectionPool(migrationPoolName);
			globalMigrationDB =
				TestingConnectionPoolRegistryAccess.installConnectionPool(MIGRATION_POOL_NAME, migrationPool);
	    	migrationDBChanged = true;
			dropAllTables(migrationPool);
	    }
	}

	private void resetMigrationDB() {
		if (migrationDBChanged) {
			TestingConnectionPoolRegistryAccess.installConnectionPool(MIGRATION_POOL_NAME, globalMigrationDB);
			globalMigrationDB = null;
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		revertDefaultPool();
	}

	private void revertDefaultPool() {
		currentType = null;
		resetMigrationDB();
		TestingConnectionPoolRegistryAccess.restoreDefaultPool(globalDefaultDB);
		globalDefaultDB = null;
	}

	/**
	 * The {@link DBType} that is currently tested.
	 */
	public static DBType getCurrentType() {
		return currentType;
	}
	
	private static String getDefaultDB(DBType dbType) {
		return getDefaultPoolName(dbType);
	}

	private static String getDefaultPoolName(DBType dbType) {
		String poolName;
		switch (dbType) {
			case MYSQL_DB: {
				poolName = "testUnitMysql";
				break;
			}
			case MSSQL_DB: {
				poolName = "testUnitMssql";
				break;
			}
			case ORACLE_DB: {
				poolName = "testUnitOracle";
				break;
			}
			case H2_DB: {
				poolName = "testUnitH2";
				break;
			}
			case ORACLE12_DB: {
				poolName = "testUnitOracle12";
				break;
			}
			case ORACLE19_DB: {
				poolName = "testUnitOracle19";
				break;
			}
			case DB2_DB: {
				poolName = "testUnitDB2";
				break;
			}
			case POSTGRESQL_DB: {
				poolName = "testUnitPostgresql";
				break;
			}
			default: {
				throw new IllegalArgumentException("No such DB type: " + dbType);
			}
		}
		return poolName;
	}

	private static String getMigrationPoolName(DBType dbType) {
		String poolName;
		switch (dbType) {
			case MYSQL_DB: {
				poolName = "testMigrationMysql";
				break;
			}
			case MSSQL_DB: {
				poolName = "testMigrationMssql";
				break;
			}
			case ORACLE_DB: {
				poolName = "testMigrationOracle";
				break;
			}
			case H2_DB: {
				poolName = "testMigrationH2";
				break;
			}
			case ORACLE12_DB: {
				poolName = "testMigrationOracle12";
				break;
			}
			case ORACLE19_DB: {
				poolName = "testMigrationOracle19";
				break;
			}
			case DB2_DB: {
				poolName = "testMigrationDB2";
				break;
			}
			case POSTGRESQL_DB: {
				poolName = "testMigrationPostgresql";
				break;
			}
			default: {
				throw new IllegalArgumentException("No such DB type: " + dbType);
			}
		}
		return poolName;
	}
	
	/**
	 * Returns the default {@link DBType} use for tests.
	 * 
	 * <p>
	 * Checks whether {@value #DEFAULT_DB_PROPERTY} is set as system property or environment
	 * variable. The value is resolved to a {@link DBType}.
	 * </p>
	 */
	private static DBType defaultDBType() {
		DBType defaultDBType = DBType.H2_DB;
		String systemProp = Environment.getSystemPropertyOrEnvironmentVariable(DEFAULT_DB_PROPERTY, defaultDBType.getExternalName());
		try {
			return ConfigUtil.getEnum(DBType.class, systemProp);
		} catch (ConfigurationException ex) {
			return defaultDBType;
		}
	}

	/**
	 * Tests whether only the default database should be used for tests. If a test uses a service
	 * method to create the "same" test for more than one DB and this method returns
	 * <code>true</code>, then the tests are only executed on the "default" database.
	 * 
	 * <p>
	 * Checks whether {@value #ONLY_DEFAULT_DB_PROPERTY} is set as system property or environment
	 * variable and set to <code>true</code>.
	 * </p>
	 */
	public static boolean useOnlyDefaultDB() {
		return Environment.getSystemPropertyOrEnvironmentVariable(ONLY_DEFAULT_DB_PROPERTY, false);
	}

	/**
	 * Wraps the given test case to run with the given database.
	 * 
	 * @param test
	 *        The test to wrap
	 * @param dbType
	 *        The database to use.
	 * @return The wrapped test.
	 */
	public static Test getDBTest(Test test, DBType dbType) {
		DatabaseTestSetup createInnerSetup = createSetup(test, dbType);
		return wrap(createInnerSetup);
	}

	/**
	 * Creates a default test suite from the given class to run with all
	 * configured databases.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @return the wrapped test suite.
	 */
	public static Test getDBTest(Class<? extends TestCase> testCase) {
		return getDBTest(testCase, DefaultTestFactory.INSTANCE);
	}

	/**
	 * Creates a test suite from the given class using the given test factory to
	 * run with all configured databases.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @param f
	 *        The test factory to create a test suite for the given test case
	 *        class.
	 * @return the wrapped test suite.
	 */
	public static Test getDBTest(Class<? extends TestCase> testCase, TestFactory f) {
		TestSuite result = new TestSuite(testCase.getName());
		if (useOnlyDefaultDB()) {
			result.addTest(newTest(testCase, DEFAULT_DB, f));
		} else {
			result.addTest(newTest(testCase, DBType.MYSQL_DB, f));
			result.addTest(newTest(testCase, DBType.MSSQL_DB, f));
			result.addTest(newTest(testCase, DBType.H2_DB, f));
			result.addTest(newTest(testCase, DBType.ORACLE_DB, f));
			result.addTest(newTest(testCase, DBType.ORACLE12_DB, f));
			result.addTest(newTest(testCase, DBType.ORACLE19_DB, f));
			result.addTest(newTest(testCase, DBType.POSTGRESQL_DB, f));
			// #8797 licence has been expired
//		result.addTest(newTest(testCase, DBType.DB2_DB, f));
		}
		
		return wrap(result);
	}

	private static DatabaseTestSetup newTest(Class<? extends TestCase> testCase, DBType db, TestFactory f) {
		return newTest(testCase, db, f, db.toString());
	}

	private static DatabaseTestSetup newTest(Class<? extends TestCase> testCase, DBType db, TestFactory f,
			String testNameAppendix) {
		DatabaseTestSetup setup = createSetup(testCase, db, f);
		if (!StringServices.isEmpty(testNameAppendix)) {
			setup = TestUtils.tryEnrichTestnames(setup, testNameAppendix);
		}
		return setup;
	}

	/**
	 * Creates a default test suite from the given class to run with only the
	 * given database.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @param dbType
	 *        The database to use.
	 * @return the wrapped test.
	 */
	public static Test getDBTest(Class<? extends TestCase> testCase, DBType dbType) {
		return getDBTest(testCase, dbType, DefaultTestFactory.INSTANCE);
	}

	/**
	 * Creates a test suite from the given class using the given test factory to
	 * run with the given database.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @param dbType
	 *        The database to use.
	 * @param f
	 *        The test factory to create a test suite for the given test case
	 *        class.
	 * @return the wrapped test.
	 */
	public static Test getDBTest(Class<? extends TestCase> testCase, DBType dbType, TestFactory f) {
		return getDBTest(testCase, dbType, f, null);
	}

	/**
	 * Creates a test suite from the given class using the given test factory to run with the given
	 * database.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @param dbType
	 *        The database to use.
	 * @param f
	 *        The test factory to create a test suite for the given test case class.
	 * @param testNameAppendix
	 *        The appendix to append to all tests created by the given factory.
	 * 
	 * @return the wrapped test.
	 */
	public static Test getDBTest(Class<? extends TestCase> testCase, DBType dbType, TestFactory f, String testNameAppendix) {
		return wrap(newTest(testCase, dbType, f, testNameAppendix));
	}

	public static Test getSingleDBTest(Test test) {
		return getDBTest(test, DEFAULT_DB);
	}

	private static Test wrap(Test innerTest) {
		return ServiceTestSetup.withThreadContext(ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE,
			innerTest, ConnectionPoolRegistry.Module.INSTANCE));
	}

	private static DatabaseTestSetup createSetup(Class<? extends TestCase> testCase, DBType dbType, TestFactory f) {
		return createSetup(f.createSuite(testCase, testCase.getName() + " with " + dbType), dbType);
	}

	private static DatabaseTestSetup createSetup(Test test, DBType dbType) {
		return new DatabaseTestSetup(test, dbType);
	}


}
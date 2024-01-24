/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.RearrangableThreadContextSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DB2Helper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MSAccessHelper;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.PostgreSQLHelper;

/**
 * TestSetup to Wrap some different types of connections for testing
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ConnectionSetup extends RearrangableThreadContextSetup {

	private static final boolean MANDATORY = true;

	private static final String AUTO_TEST = "AUTO_TEST";

	private static final String SEQ_AUTO_TEST = "SEQ_AUTO_TEST";

	public static final String TABLE_NAME = "perstest";

	private static final MutableInteger setupCnt = new MutableInteger();

	DBHelper dbh;

    ConnectionSetup(Test test) {
		super(test, setupCnt);
	}

    /** Make this Connection the currentConnection.
     * 
     *  And create some demodata. 
     */
    @Override
    protected void doSetUp() throws Exception {
    	final ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
    	dbh = pool.getSQLDialect();
    	final PooledConnection con = pool.borrowWriteConnection();
    	try {
    		dropTable(con);
    		createTable(con);
    	} finally {
    		pool.releaseWriteConnection(con);
    		dbh = null;
    	}
    }
    
    /** Reset the current Connection and close it. */
    @Override
    protected void doTearDown() throws Exception {
    	final ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
    	dbh = pool.getSQLDialect();
    	final PooledConnection con = pool.borrowWriteConnection();
    	try {
    		dropTable(con);
    	} finally {
    		pool.releaseWriteConnection(con);
    		dbh = null;
    	}
    }

	private void appendColumn(Appendable builder, String name, DBType type, long size, boolean binary,
			boolean mandatory, boolean isLast) throws IOException {
		builder.append(dbh.columnRef(name));
		builder.append(" ");
		dbh.appendDBType(builder, type, name, size, 0, mandatory, binary);

		if (!isLast) {
			builder.append(",");
		}
	}

	private void appendColumn(Appendable builder, String name, String args, boolean isLast) throws IOException {
		builder.append(dbh.columnRef(name));
		builder.append(" ");
		builder.append(args);

		if (!isLast) {
			builder.append(",");
		}
	}

	/** Create the table {@link #TABLE_NAME} and add some data. */
    protected void createTable(Connection aCon) throws Exception {
		try (Statement statement = aCon.createStatement()) {

			createPretestTable(statement);
			createSeqAutoTestTable(statement);

			insertValues(statement);
		}

		// Note: MSSQL supports transactional schema modification.
		aCon.commit();
    }

	private void createSeqAutoTestTable(Statement stm) throws IOException, SQLException {
		StringBuilder autoTestTablebuilder = new StringBuilder("CREATE TABLE " + dbh.tableRef(AUTO_TEST) + " ( ");

		if (dbh instanceof MySQLHelper) {
			appendColumn(autoTestTablebuilder, "IDENT", "INTEGER UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, true);
        }
        else if (dbh instanceof MSAccessHelper) {
			appendColumn(autoTestTablebuilder, "IDENT", "AUTOINCREMENT", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, true);
         }
		else if (dbh instanceof MSSQLHelper) {
			appendColumn(autoTestTablebuilder, "IDENT", "INTEGER IDENTITY PRIMARY KEY", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, true);
         }
		else if (dbh instanceof OracleHelper) {
			appendColumn(autoTestTablebuilder, "IDENT", "INTEGER NOT NULL PRIMARY KEY", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, true);

			stm.execute("CREATE SEQUENCE " + seq(SEQ_AUTO_TEST));
          }
		else if (dbh instanceof DB2Helper) {
			appendColumn(autoTestTablebuilder, "IDENT", "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, false);
			autoTestTablebuilder.append("PRIMARY KEY (IDENT)");

			stm.execute("CREATE SEQUENCE " + seq(SEQ_AUTO_TEST));
		}
		else if (dbh instanceof H2Helper) {
			appendColumn(autoTestTablebuilder, "IDENT", "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, MANDATORY, false);
			autoTestTablebuilder.append("PRIMARY KEY (IDENT)");

			stm.execute("CREATE SEQUENCE " + seq(SEQ_AUTO_TEST));
		} else if (dbh instanceof PostgreSQLHelper) {
			appendColumn(autoTestTablebuilder, "IDENT",
				"SERIAL PRIMARY KEY NOT NULL CHECK (" + dbh.columnRef("IDENT") + " > 0)", false);
			appendColumn(autoTestTablebuilder, "NAME", DBType.STRING, 64, false, true, true);
		} else {
			throw new IllegalArgumentException("Unknown SQL dialect: " + dbh);
        }

		autoTestTablebuilder.append(")");
		stm.execute(autoTestTablebuilder.toString());
	}

	private void insertValues(Statement stm) throws SQLException {
		StringBuilder insertIntoBuilder1 = new StringBuilder("INSERT INTO " + dbh.tableRef(TABLE_NAME) + " VALUES ");
		Tuple insertValues1 =
			TupleFactory.newTuple(-1, 65565, true, true, 'A', "   Mandy  ", 1.0, 2.0, null, null, null, 0, null);
		dbh.appendValue(insertIntoBuilder1, insertValues1);

		stm.executeUpdate(insertIntoBuilder1.toString());

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 11);
		cal.set(Calendar.MINUTE, 33);
		cal.set(Calendar.SECOND, 44);

		StringBuilder insertIntoBuilder2 = new StringBuilder("INSERT INTO " + dbh.tableRef(TABLE_NAME) + " VALUES ");
		Tuple insertValues2 =
			TupleFactory.newTuple(0, -32767, false, null, null, "Wilhelm", 99.99, 88.88,
				DateUtil.createDate(2001, Calendar.JULY, 14),
				cal.getTime(), DateUtil.createDate(2004, Calendar.JULY, 14, 19, 23, 18), 111111111, 222222222);
		dbh.appendValue(insertIntoBuilder2, insertValues2);

		stm.executeUpdate(insertIntoBuilder2.toString());

		StringBuilder insertIntoBuilder3 = new StringBuilder("INSERT INTO " + dbh.tableRef(TABLE_NAME) + " VALUES ");
		Tuple insertValues3 =
			TupleFactory.newTuple(2, 3242, true, true, 'e', null, 1.2, 1.324,
				DateUtil.createDate(2002, Calendar.SEPTEMBER, 10), null, null, 555555555,
				666666666);
		dbh.appendValue(insertIntoBuilder3, insertValues3);

		stm.executeUpdate(insertIntoBuilder3.toString());

		StringBuilder insertIntoBuilder4 = new StringBuilder("INSERT INTO " + dbh.tableRef(TABLE_NAME) + " VALUES ");
		Tuple insertValues4 =
			TupleFactory.newTuple(3, 100, true, true, 'w', "Werner", 3.14159, 20.0,
				DateUtil.createDate(1970, Calendar.JANUARY, 1), null,
				DateUtil.createDate(2004, Calendar.JULY, 14, 0, 13, 47), 777777777, 888888888);
		dbh.appendValue(insertIntoBuilder4, insertValues4);

		stm.executeUpdate(insertIntoBuilder4.toString());
	}

	private void createPretestTable(Statement stm) throws IOException, SQLException {
		StringBuilder preTestTablebuilder = new StringBuilder("CREATE TABLE " + dbh.tableRef(TABLE_NAME) + " ( ");

		appendColumn(preTestTablebuilder, "i1", DBType.INT, 0, false, MANDATORY, false);
		appendColumn(preTestTablebuilder, "i2", DBType.INT, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "b1", DBType.BOOLEAN, 0, false, MANDATORY, false);
		appendColumn(preTestTablebuilder, "b2", DBType.BOOLEAN, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "s1", DBType.CHAR, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "s2", DBType.STRING, 22, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "r1", DBType.FLOAT, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "r2", DBType.DOUBLE, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "d1", DBType.DATE, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "t1", DBType.TIME, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "dt", DBType.DATETIME, 0, false, !MANDATORY, false);
		appendColumn(preTestTablebuilder, "l1", DBType.LONG, 0, false, MANDATORY, false);
		appendColumn(preTestTablebuilder, "l2", DBType.LONG, 0, false, !MANDATORY, true);

		preTestTablebuilder.append(")");

		stm.execute(preTestTablebuilder.toString());
	}

	private String table(String tableName) {
		return dbh.tableRef(tableName);
	}

	private String seq(String name) {
		return dbh.tableRef(name);
	}

    /** Dop the tabe when we are done. */
    protected void dropTable(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			// Drop the tables etc., just in case they was accidently left over

			if (dbh instanceof OracleHelper) {
				try {
					statement.execute("DROP SEQUENCE " + seq(SEQ_AUTO_TEST));
					statement.execute("PURGE RECYCLEBIN"); // Silently PURGE RECYCLEBIN for SA here
				} catch (SQLException ignored) { /* ignored */
				}
			}
			try {
				statement.execute("DROP TABLE " + table(TABLE_NAME));
			} catch (SQLException ex) {
				// ignore
//				ex.printStackTrace();
			}
			try {
				statement.execute("DROP TABLE " + table(AUTO_TEST));
			} catch (SQLException ex) {
				// ignore
//				ex.printStackTrace();
			}
			if (dbh instanceof DB2Helper || dbh instanceof H2Helper) {
				try {
					statement.execute("DROP SEQUENCE " + seq(SEQ_AUTO_TEST));
				} catch (SQLException ex) {
					// ignore
//				ex.printStackTrace();
				}
			}

			// Note: MSSQL supports transactional schema modification.
			connection.commit();
		}
        

        
    }
    
    /** Create a Suite of Tests wrapped for all Connections */
	public static Test createSuite(Class<? extends TestCase> testClass, TestFactory factory) {
		return DatabaseTestSetup.getDBTest(testClass, wrapConnectionSetup(factory));
    }

	/** Create a Suite of Tests wrapped for the given {@link DBType}. */
	public static Test createSuite(Class<? extends TestCase> testCase,
			test.com.top_logic.basic.DatabaseTestSetup.DBType dbType, TestFactory factory) {
		return DatabaseTestSetup.getDBTest(testCase, dbType, wrapConnectionSetup(factory));
	}

	private static TestFactoryProxy wrapConnectionSetup(TestFactory factory) {
		return new TestFactoryProxy(factory) {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				return new ConnectionSetup(super.createSuite(testCase, suiteName));
			}
		};
	}
}

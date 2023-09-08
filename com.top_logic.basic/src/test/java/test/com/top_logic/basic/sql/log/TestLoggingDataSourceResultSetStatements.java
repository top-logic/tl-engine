/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.sql.dummy.DummyDataSource;

import com.top_logic.basic.sql.LoggingDataSourceProxy;

/**
 * Tests for the {@link LoggingDataSourceProxy} which use statements that cause no changed rows.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestLoggingDataSourceResultSetStatements extends TestCase {

	public void testSingleStatementResultSet() throws Throwable {
		String sql = DummyDataSource.RESULT_SET + ":3:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		statement.execute(sql);
		readAndClose(statement.getResultSet());
		connection.commit();
		flush(dataSource);

		try {
			assertIdenticalUpdates(1, 3, sql, dataSource, "Ticket #12070: ");
			fail("Known problem #12070 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #12070:");
		}
	}

	public void testSingleStatementPreparedResultSet() throws Throwable {
		String sql = DummyDataSource.RESULT_SET + ":3:Testquery Alpha";
		
		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.execute();
		readAndClose(statement.getResultSet());
		connection.commit();
		flush(dataSource);
		
		try {
			assertIdenticalUpdates(1, 3, sql, dataSource, "Ticket #12070: ");
			fail("Known problem #12070 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #12070:");
		}
	}
	
	public void testIdenticalStatementsResultSet() throws Throwable {
		int updates = 3;
		String sql = DummyDataSource.RESULT_SET + ":4:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < updates; i++) {
			statement.execute(sql);
			readAndClose(statement.getResultSet());
		}
		connection.commit();
		flush(dataSource);

		try {
			assertIdenticalUpdates(updates, 12, sql, dataSource, "Ticket #12070: ");
			fail("Known problem #12070 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #12070:");
		}
	}

	public void testIdenticalStatementsPrepareResultSet() throws Throwable {
		int updates = 3;
		String sql = DummyDataSource.RESULT_SET + ":4:Testquery Alpha";
		
		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 0; i < updates; i++) {
			statement.execute();
			readAndClose(statement.getResultSet());
		}
		connection.commit();
		flush(dataSource);
		
		try {
			assertIdenticalUpdates(updates, 12, sql, dataSource, "Ticket #12070: ");
			fail("Known problem #12070 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #12070:");
		}
	}

	public void testDistinctStatementsResultSet() throws Throwable {
		String[] sql = {
			DummyDataSource.RESULT_SET + ":2:Testquery Alpha",
			DummyDataSource.RESULT_SET + ":3:Testquery Beta",
			DummyDataSource.RESULT_SET + ":4:Testquery Gamma"
		};

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < sql.length; i++) {
			statement.execute(sql[i]);
			readAndClose(statement.getResultSet());
		}
		connection.commit();
		flush(dataSource);

		try {
			assertDistinctUpdates(new int[] { 1, 1, 1 }, new int[] { 2, 3, 4 }, sql, dataSource, "Ticket #12070: ");
			fail("Known problem #12070 fixed?");
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertStartsWith(ex.getMessage(), "Ticket #12070:");
		}
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceResultSetStatements.class);
	}

}

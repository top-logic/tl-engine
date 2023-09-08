/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.sql.dummy.DummyDataSource.*;
import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sql.LoggingDataSourceProxy;

/**
 * Tests for the {@link LoggingDataSourceProxy} which use statements that cause no changed rows.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestLoggingDataSourceUpdateCountStatements extends TestCase {

	public void testSingleStatementUpdateCount() throws Throwable {
		String sql = markUpdateCount("3:Testquery Alpha");

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		statement.execute(sql);
		connection.commit();
		flush(dataSource);

		assertIdenticalUpdates(1, 3, sql, dataSource);
	}

	public void testSingleStatementPreparedUpdateCount() throws Throwable {
		String sql = markUpdateCount("3:Testquery Alpha");
		
		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.execute();
		connection.commit();
		flush(dataSource);
		
		assertIdenticalUpdates(1, 3, sql, dataSource);
	}
	
	public void testIdenticalStatementsUpdateCount() throws Throwable {
		int updates = 3;
		String sql = markUpdateCount("4:Testquery Alpha");

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < updates; i++) {
			statement.execute(sql);
		}
		connection.commit();
		flush(dataSource);

		assertIdenticalUpdates(updates, 12, sql, dataSource);
	}

	public void testIdenticalStatementsPrepareUpdateCount() throws Throwable {
		int updates = 3;
		String sql = markUpdateCount("4:Testquery Alpha");
		
		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 0; i < updates; i++) {
			statement.execute();
		}
		connection.commit();
		flush(dataSource);
		
		assertIdenticalUpdates(updates, 12, sql, dataSource);
	}

	public void testDistinctStatementsUpdateCount() throws Throwable {
		String[] sql = {
			markUpdateCount("2:Testquery Alpha"),
			markUpdateCount("3:Testquery Beta"),
			markUpdateCount("4:Testquery Gamma")
		};

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < sql.length; i++) {
			statement.execute(sql[i]);
		}
		connection.commit();
		flush(dataSource);

		assertDistinctUpdates(new int[] { 1, 1, 1 }, new int[] { 2, 3, 4 }, sql, dataSource);
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceUpdateCountStatements.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sql.LoggingDataSourceProxy;

/**
 * Tests for the {@link LoggingDataSourceProxy} which use multiple connections.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestLoggingDataSourceMultipleConnections extends TestCase {

	public void testIdenticalUpdatesMultipleConnections() throws Throwable {
		int connections = 3;
		String sql = "3:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		for (int i = 0; i < connections; i++) {
			Connection connection = dataSource.getConnection();
			connection.createStatement().executeUpdate(sql);
			connection.commit();
		}
		flush(dataSource);

		assertIdenticalUpdates(connections, 9, sql, dataSource);
	}

	public void testIdenticalUpdatesMultipleConnectionsPrepared() throws Throwable {
		int connections = 3;
		String sql = "3:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		for (int i = 0; i < connections; i++) {
			Connection connection = dataSource.getConnection();
			connection.prepareStatement(sql).executeUpdate();
			connection.commit();
		}
		flush(dataSource);

		assertIdenticalUpdates(connections, 9, sql, dataSource);
	}

	public void testDistinctUpdatesMultipleConnections() throws Throwable {
		String[] sql = { "2:Testquery Alpha", "3:Testquery Beta", "4:Testquery Gamma" };

		LoggingDataSourceProxy dataSource = newDataSource();
		for (int i = 0; i < sql.length; i++) {
			Connection connection = dataSource.getConnection();
			connection.createStatement().executeUpdate(sql[i]);
			connection.commit();
		}
		flush(dataSource);

		assertDistinctUpdates(new int[] { 1, 1, 1 }, new int[] { 2, 3, 4 }, sql, dataSource);
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceMultipleConnections.class);
	}

}

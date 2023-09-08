/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sql.LoggingDataSourceProxy;

/**
 * Tests for the {@link LoggingDataSourceProxy} which use batches.
 * <p>
 * Batches are tested only with prepared statements, currently.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestLoggingDataSourceBatches extends TestCase {

	public void testBatchesIdenticalPrepared() throws Throwable {
		int batches = 3;
		String sql = "0:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 0; i < batches; i++) {
			statement.addBatch();
		}
		statement.executeBatch();
		connection.commit();
		flush(dataSource);

		assertIdenticalUpdates(1, 0, sql, dataSource);
	}

	public void testBatchesIdenticalPreparedWithChanges() throws Throwable {
		int batches = 3;
		String sql = "4:Testquery Alpha";

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		for (int i = 0; i < batches; i++) {
			statement.addBatch();
		}
		statement.executeBatch();
		connection.commit();
		flush(dataSource);

		assertIdenticalUpdates(1, 12, sql, dataSource);
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceBatches.class);
	}

}

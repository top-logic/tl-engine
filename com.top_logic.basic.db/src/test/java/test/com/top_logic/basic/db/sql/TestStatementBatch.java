/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.sql.AbstractStatementBatch;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.StatementProxy;

/**
 * The class {@link TestStatementBatch} tests {@link Batch} based on {@link Statement}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM internally.")
public class TestStatementBatch extends BasicTestCase {

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestCloseOnFinalize() throws SQLException, InterruptedException {
		ConnectionPool connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PooledConnection connection = connectionPool.borrowReadConnection();
		try {
			final CountDownLatch countdownLatch = new CountDownLatch(1);
			final PreparedStatement stmt =
				connection.prepareStatement("SELECT 1" + connectionPool.getSQLDialect().fromNoTable() + ";");
			try {
				StatementProxy stmtProxy = new StatementProxy() {

					@Override
					protected Statement impl() {
						return stmt;
					}

					@Override
					public void close() throws SQLException {
						countdownLatch.countDown();
						super.close();
					}
				};
				new AbstractStatementBatch<Statement>(stmtProxy) {

					@Override
					public void addBatch(Object... arguments) throws SQLException {
						throw new UnsupportedOperationException();
					}
				};
				provokeOutOfMemory();
				countdownLatch.await(10, TimeUnit.SECONDS);
				assertEquals("close is not called in finalize", 0, countdownLatch.getCount());
			} finally {
				if (countdownLatch.getCount() > 0) {
					stmt.close();
				}
			}
		} finally {
			connectionPool.releaseReadConnection(connection);
		}

	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestStatementBatch.class));
	}

}

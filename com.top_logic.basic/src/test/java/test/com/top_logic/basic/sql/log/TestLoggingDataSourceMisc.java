/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.sql.DefaultDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Config;

/**
 * Tests for the {@link LoggingDataSourceProxy} which use update statements that cause changed rows.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestLoggingDataSourceMisc extends TestCase {

	public void testFailureMessageOnMissingDriverClass() throws SQLException {
		Config config = createConfig();
		config.setDriverClassName(null);
		try {
			newDataSource(config);
			fail("Failure expected");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains(DefaultDataSourceProxy.IMPL_CLASS_PROPERTY, ex.getMessage());
		}
	}

	public void testIgnores() throws Throwable {
		String sql = "3:Testquery Alpha";

		Config config = createConfig();
		config.setIgnorePattern(getClass().getCanonicalName());
		LoggingDataSourceProxy dataSource = newDataSource(config);
		
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
		connection.commit();
		flush(dataSource);

		assertIdenticalUpdates(0, 0, sql, dataSource);
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceMisc.class);
	}

}

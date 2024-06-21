/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.log;

import static test.com.top_logic.basic.BasicTestCase.*;
import static test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;

/**
 * Tests for unifying statements in {@link LoggingDataSourceProxy}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLoggingDataSourceUnifyQuery extends TestCase {

	public void testInSet() throws Throwable {
		String[] sql = { "row in (1,2,3)", "row in (3,4)", "row2 in (3,4)" };

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < sql.length; i++) {
			ResultSet results = statement.executeQuery(sql[i]);
			readAndClose(results);
		}
		connection.commit();
		flush(dataSource);

		TestableLoggingDataSource analyzer = analyzer(dataSource);
		
		Map<String, Statistics> entries = analyzer.getEntries();
		assertEquals(set("row in (...)", "row2 in (...)"), entries.keySet());
		assertEquals(2, entries.get("row in (...)").getCnt());
	}

	public void testNumber() throws Throwable {
		String[] sql = { "revMax > 0 AND", "revMax > 1564 AND", "15=revMin" };

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < sql.length; i++) {
			ResultSet results = statement.executeQuery(sql[i]);
			readAndClose(results);
		}
		connection.commit();
		flush(dataSource);

		TestableLoggingDataSource analyzer = analyzer(dataSource);

		Map<String, Statistics> entries = analyzer.getEntries();
		assertEquals(set("revMax > <number> AND", "<number>=revMin"), entries.keySet());
		assertEquals(2, entries.get("revMax > <number> AND").getCnt());
	}

	public void testStringLiteral() throws Throwable {
		String[] sql = { "WHERE attr = 'attr1'", "WHERE attr = 'attr2'" };

		LoggingDataSourceProxy dataSource = newDataSource();
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		for (int i = 0; i < sql.length; i++) {
			ResultSet results = statement.executeQuery(sql[i]);
			readAndClose(results);
		}
		connection.commit();
		flush(dataSource);

		TestableLoggingDataSource analyzer = analyzer(dataSource);

		Map<String, Statistics> entries = analyzer.getEntries();
		assertEquals(set("WHERE attr = '...'"), entries.keySet());
		assertEquals(2, entries.get("WHERE attr = '...'").getCnt());
	}

	/** Entry point for JUnit. Returns a {@link Test} suite containing all the tests of this class. */
	public static Test suite() {
		return new TestSuite(TestLoggingDataSourceUnifyQuery.class);
	}

}

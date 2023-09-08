/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.sql.Param;
import com.top_logic.basic.sql.StatementScope;

/**
 * Test case for {@link StatementScope}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestStatementScope extends BasicTestCase {

	/**
	 * A statement constructed with a {@link StatementScope}.
	 * 
	 * <p>
	 * For each statement, a static class is created:
	 * </p>
	 * 
	 * <ul>
	 * <li>A statically initialized {@link #scope} member</li>
	 * 
	 * <li>For each parameter of the statement, a {@link Param} object is
	 * constructed by calling {@link StatementScope#newParam()}.</li>
	 * 
	 * <li>
	 * For each result column of the statement, a {@link Param} object is
	 * constructed by calling {@link StatementScope#newResult(String)}.</li>
	 * 
	 * <li>The statement source (see {@link #STATEMENT_STRING}) is constructed
	 * by concatenating the static SQL parts with the parameter and result
	 * objects. This internally computes the indices that can be used to access
	 * values in the {@link PreparedStatement} and {@link ResultSet} objects.</li>
	 * </ul>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	static class DemoStatement {
		static StatementScope scope = new StatementScope();
		
		static Param PARAM_P1 = scope.newParam();
		static Param PARAM_P2 = scope.newParam();

		static Param RESULT_R1 = scope.newResult("COL_A");
		static Param RESULT_R2 = scope.newResult("COL_B");
		
		static String STATEMENT_STRING = 
			"SELECT " + RESULT_R1 + ", " + RESULT_R2 + " FROM TABLE_X WHERE COL_C >= " + PARAM_P1 + " AND COL_C <= " + PARAM_P2;
	}
	
	public void testIndices() {
		assertEquals(1, DemoStatement.PARAM_P1.getIndex());
		assertEquals(2, DemoStatement.PARAM_P2.getIndex());
		assertEquals(1, DemoStatement.RESULT_R1.getIndex());
		assertEquals(2, DemoStatement.RESULT_R2.getIndex());
		assertEquals("SELECT COL_A, COL_B FROM TABLE_X WHERE COL_C >= ? AND COL_C <= ?", DemoStatement.STATEMENT_STRING);
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStatementScope.class));
	}

}

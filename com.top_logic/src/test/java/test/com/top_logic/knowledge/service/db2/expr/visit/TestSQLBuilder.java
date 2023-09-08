/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.Logger;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLPart;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.SQLFactory;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;

/**
 * Test case for {@link SQLBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSQLBuilder extends AbstractDBKnowledgeBaseTest {

	public void testNavigation() throws SQLException {
		SetExpression expr =
			filter(
				map(
					filter(
						allOf(AB_NAME),
						eval(destination(),
							and(hasType(B_NAME), eqBinary(attribute(A_NAME, A2_NAME), literal("v1"))))),
					source()),
				and(hasType(C_NAME), eqBinary(flex(MOPrimitive.STRING, "f1"), attribute(A_NAME, A1_NAME))));
		
		CompiledStatement stmt = compile(expr);
		executeQuery(stmt);
	}
	
	public void testFlexFilter() throws SQLException {
		SetExpression expr =
			filter(
				allOf(B_NAME),
				eqBinary(attribute(A_NAME, A1_NAME), flex(MOPrimitive.STRING, "f1")));
		
		CompiledStatement stmt = compile(expr);
		executeQuery(stmt);
	}

	/**
	 * Test all potential occurrences of filter expressions in a (normalized)
	 * expression.
	 */
	public void testFilterExpressions() throws SQLException {
		SetExpression expr = 
			filter(
				map(
					filter(
						map(
							filter(
								allOf(AB_NAME),
								eqBinary(attribute(AB_NAME, AB1_NAME), literal("ab1"))),
							source()), 
						and(hasType(E_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal("a1")))),
					reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME)),
				and(hasType(D_NAME), eqBinary(attribute(A_NAME, A2_NAME), literal("a2"))));

		CompiledStatement stmt = compile(expr);
		executeQuery(stmt);
	}

	public void testErrorNoContextType() throws SQLException {
		SetExpression expr =
			filter(
				map(
					filter(
						map(
							filter(
								allOf(AB_NAME),
								eqBinary(attribute(AB_NAME, AB1_NAME), literal("ab1"))),
							source()),
						and(hasType(BC_NAME), eqBinary(attribute(BC_NAME, BC1_NAME), literal("bc1")))),
					destination()),
				and(hasType(C_NAME), eqBinary(attribute(C_NAME, C1_NAME), literal("c1"))));
		
		try {
			compileFail(expr);
			fail("Must not compile due to type error.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
	}

	public void testMultiStepNavigation() throws SQLException {
		SetExpression expr = 
			navigateForwards(
				navigateForwards(
					allOf(B_NAME),
					allOf(BC_NAME), C_NAME),
				allOf(AB_NAME), U_NAME);

		CompiledStatement stmt = compile(expr);
		executeQuery(stmt);
	}

	public void testCaseNavigation() throws SQLException, NoSuchAttributeException {
		MOClass e_type = type(E_NAME);
		DBAttribute d1 = e_type.getAttribute(D1_NAME).getDbMapping()[0];
		DBAttribute d2 = e_type.getAttribute(D2_NAME).getDbMapping()[0];

		String tableAlias = "t";
		SQLExpression conditionExpr =
			SQLFactory.eq(SQLFactory.column(tableAlias, d1, false), SQLFactory.column(tableAlias, d2, false));
		SQLExpression thenExpr = SQLFactory.column(tableAlias, d1, false);
		SQLExpression elseExpr = SQLFactory.column(tableAlias, d2, false);
		SQLExpression sqlCase = SQLFactory.sqlCase(conditionExpr, thenExpr, elseExpr);
		SQLExpression where = SQLFactory.eq(sqlCase, SQLFactory.literalString("value"));
		List<SQLColumnDefinition> columnDefs = SQLFactory.columnDefs(tableAlias, e_type);
		SQLTable table = SQLFactory.table(E_NAME, tableAlias);
		SQLSelect select = SQLFactory.select(true, columnDefs, table, where, SQLFactory.NO_ORDERS);

		CompiledStatement stmt = compile(select, Collections.<String, Integer> emptyMap());
		executeQuery(stmt);
	}

	private CompiledStatement compile(SetExpression expr) throws SQLException {
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new AssertProtocol());
		return createSql(log, expr);
	}

	private CompiledStatement compileFail(SetExpression expr) throws ExpectedFailure, SQLException {
		ExpressionCompileProtocol log = new ExpressionCompileProtocol(new ExpectedFailureProtocol());
		return createSql(log, expr);
	}

	private CompiledStatement createSql(ExpressionCompileProtocol log, SetExpression expr) throws SQLException {
		TypeSystem typeSystem = typeSystem();
		RevisionQuery<KnowledgeObject> query = queryUnresolved(expr);
		TestSymbolCreator.computeSymbols(typeSystem, log, expr);
		log.checkErrors();

		SQLPart select =
			SQLBuilder.createRevisionSearchSQL(kb().getConnectionPool().getSQLDialect(), typeSystem, query, true);
		long elapsed = -System.nanoTime();
		
		Map<String, Integer> args = query.getArgumentIndexByName();
		CompiledStatement sql = compile(select, args);
		
		elapsed += System.nanoTime();
		Logger.debug((((float) (elapsed / 1000)) / 1000) + "ms: " + ExpressionPrinter.toString(expr), TestSQLBuilder.class);
		
		return sql;
	}

	private CompiledStatement compile(SQLPart select, Map<String, Integer> args) throws SQLException {
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		CompiledStatement sql = SQLQuery.toSql(sqlDialect, select, args);

		Logger.debug(sql.toString(), TestSQLBuilder.class);
		return sql;

	}

	void executeQuery(CompiledStatement stmt) {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			/* general mechanism introduces the requested revision and history context into the
			 * query */
			Object[] arguments = new Object[] { Revision.CURRENT_REV, Revision.CURRENT_REV };
			// Tests that the generated SQL do not cause failures.
			stmt.executeQuery(connection, arguments);
		} catch (SQLException ex) {
			fail("SQLBuilder created invalid SQL.", ex);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		if (!true) {
			return runOneTest(TestSQLBuilder.class, "testFilterExpressions", DBType.MYSQL_DB);
		}
		return suite(TestSQLBuilder.class);
	}
	
}

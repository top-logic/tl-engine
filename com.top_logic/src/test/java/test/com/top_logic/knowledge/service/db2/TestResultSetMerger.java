/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.ResultSetMerger;

/**
 * Test of {@link ResultSetMerger}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestResultSetMerger extends AbstractDBKnowledgeBaseTest {

	protected static final String TYPE = TestResultSetMerger.class.getSimpleName();

	protected static final String LONG_ATTR = "longAttr";

	protected static final String STRING_ATTR = "stringAttr";

	protected static final String GROUP_ATTR = "groupAttr";

	private CompiledStatement _insertStmt;

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseTestSetup(self);
		setup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOClass type = new MOKnowledgeItemImpl(TYPE);
				try {
					type.addAttribute(new MOAttributeImpl(LONG_ATTR, MOPrimitive.LONG, false));
					type.addAttribute(new MOAttributeImpl(STRING_ATTR, MOPrimitive.STRING, false));
					type.addAttribute(new MOAttributeImpl(GROUP_ATTR, MOPrimitive.STRING, false));
				} catch (DuplicateAttributeException ex) {
					log.error("Dupplicate attribute in " + type.getName(), ex);
				}
				try {
					typeRepository.addMetaObject(type);
				} catch (DuplicateTypeException ex) {
					log.error("Unable to add " + type.getName(), ex);
				}

			}
		});
		return setup;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createInsertStatment();
	}

	private void createInsertStatment() throws SQLException {
		String tableAlias = NO_TABLE_ALIAS;
		MOClass type = type(TYPE);
		MOAttributeImpl stringAttr = stringAtribute(type);
		MOAttributeImpl longAttr = longAttribute(type);
		MOAttributeImpl groupAttr = groupAttribute(type);
		List<String> columnNames = columnNames(
			longAttr.getDBName(),
			stringAttr.getDBName(),
			groupAttr.getDBName()
			);
		List<? extends SQLExpression> values = Arrays.asList(
			parameter(longAttr, longAttr.getDBName()),
			parameter(stringAttr, stringAttr.getDBName()),
			parameter(groupAttr, groupAttr.getDBName())
			);
		SQLInsert insert = SQLFactory.insert(table(type.getDBMapping(), tableAlias), columnNames, values);
		List<Parameter> parameters = parameters(
			parameterDef(longAttr, longAttr.getDBName()),
			parameterDef(stringAttr, stringAttr.getDBName()),
			parameterDef(groupAttr, groupAttr.getDBName())
			);
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		_insertStmt = query(parameters, insert).toSql(sqlDialect);
	}

	private MOAttributeImpl groupAttribute(MOClass type) {
		return (MOAttributeImpl) type.getAttributeOrNull(GROUP_ATTR);
	}

	private MOAttributeImpl longAttribute(MOClass type) {
		return (MOAttributeImpl) type.getAttributeOrNull(LONG_ATTR);
	}

	private MOAttributeImpl stringAtribute(MOClass type) {
		return (MOAttributeImpl) type.getAttributeOrNull(STRING_ATTR);
	}

	private void insert(PooledConnection writeCon, String stringValue, long longValue, String groupValue)
			throws SQLException {
		_insertStmt.executeUpdate(writeCon, longValue, stringValue, groupValue);
	}

	public void testOrderLongDescending() throws SQLException {
		testLongOrder(true);
	}

	public void testOrderLongAscending() throws SQLException {
		testLongOrder(false);
	}

	private void testLongOrder(boolean descending) throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection writeCon = pool.borrowWriteConnection();
		try {
			insert(writeCon, "a", 0, "even");
			insert(writeCon, "b", 1, "odd");
			insert(writeCon, "c", 2, "even");
			insert(writeCon, "d", 3, "odd");
			insert(writeCon, "e", 4, "even");
			insert(writeCon, "f", 5, "odd");
			writeCon.commit();
		} finally {
			pool.releaseWriteConnection(writeCon);
		}
		
		String tableAlias = NO_TABLE_ALIAS;
		MOClass type = type(TYPE);
		MOAttributeImpl orderAttr = longAttribute(type);
		
		List<SQLColumnDefinition> columns = columnDefs(NO_TABLE_ALIAS, type);
		SQLTableReference from = table(type.getDBMapping(), tableAlias);
		SQLExpression where =
			eq(column(tableAlias, groupAttribute(type), !NOT_NULL), parameter(groupAttribute(type), "group"));
		List<SQLOrder> orderBy = Arrays.asList(order(descending, column(tableAlias, orderAttr, !NOT_NULL)));
		SQLSelect select = select(columns, from, where, orderBy);
		List<Parameter> parameters = parameters(
			parameterDef(groupAttribute(type), "group")
			);
		CompiledStatement selectStmt = query(parameters, select).toSql(pool.getSQLDialect());
		
		int longAttributeIndex = 1;
		PooledConnection readConnection = pool.borrowReadConnection();
		try {
			ResultSet result1 = selectStmt.executeQuery(readConnection, "even");
			ResultSet result2 = selectStmt.executeQuery(readConnection, "odd");
			ResultSet[] results = new ResultSet[] {result1,result2};
			DBAttribute[] orderAttributes = new DBAttribute[] {orderAttr};
			boolean[] orderDirection = new boolean[] {descending};
			ResultSet result = ResultSetMerger.newMultipleResultSet(results, orderAttributes, orderDirection);
			try {
				assertTrue(result.next());
				assertEquals(descending ? 5 : 0, result.getLong(longAttributeIndex));
				assertTrue(result.next());
				assertEquals(descending ? 4 : 1, result.getLong(longAttributeIndex));
				assertTrue(result.next());
				assertEquals(descending ? 3 : 2, result.getLong(longAttributeIndex));
				assertTrue(result.next());
				assertEquals(descending ? 2 : 3, result.getLong(longAttributeIndex));
				assertTrue(result.next());
				assertEquals(descending ? 1 : 4, result.getLong(longAttributeIndex));
				assertTrue(result.next());
				assertEquals(descending ? 0 : 5, result.getLong(longAttributeIndex));
				assertFalse(result.next());
			} finally {
				result.close();
			}
		} finally {
			pool.releaseReadConnection(readConnection);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestResultSetMerger}.
	 */
	public static Test suite() {
		return suite(TestResultSetMerger.class);
	}

}


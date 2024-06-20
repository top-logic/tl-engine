/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.db.sql.SQLCopy;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFun;
import com.top_logic.basic.db.sql.SQLInlineTransformation;
import com.top_logic.basic.db.sql.SQLPart;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBType;

/**
 * Test for {@link SQLCopy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSQLCopy extends TestCase {

	public void testEqSql() {
		SQLExpression expression = eqSQL(literalInteger(1), column("t", "col"));
		assertCopy(expression);
	}

	public void testQuery() {
		SQLQuery<SQLSelect> query = query(
			parameters(
				parameterDef(DBType.INT, "foo"),
				parameterDef(DBType.STRING, "bar")),
			select(true,
				columns(
					columnDef(column("t1", "col1"), "c1"),
					columnDef(literalInteger(1), "c2")),
				join(true,
					table("table1", "t1"),
					table("table2", "t2"),
					eqSQL(column("t1", "col1"), column("t2", "col5"))),
				and(
					ge(column("t2", "col2"), literalLong(99)),
					eq(column("t2", "col9"), parameter(DBType.INT, "foo"))),
				orders(
					order(false, column("t2", "col3")),
					order(true, CollationHint.NATURAL, column("t1", "col5"))),
				limit(literalInteger(0), literalInteger(999))));
		assertCopy(query);
	}

	public void testSubQuery() {
		assertCopy(subQuery(select(columns(), table("table1", "t1")), "t2"));
	}

	public void testUnion() {
		assertCopy(union(Arrays.asList(
			select(columns(), table("table1", "t1")),
			select(columns(), table("table2", "t2")))));
	}

	public void testInsert() {
		assertCopy(insert(
			table("table1", "t1"),
			columnNames("c1", "c2"),
			expressions(
				parameter(DBType.STRING, "x"),
				parameter(DBType.STRING, "y"))));
	}

	public void testInsertSelect() {
		assertCopy(
			insert(
				table("table1", "t1"), 
				columnNames("c1", "c2"), 
				select(
					columns(
						columnDef(column("t2", "cx"), "cy")), 
					table("table2", "t2"))));
	}
	
	public void testDelete() {
		assertCopy(delete(
			table("table1", NO_TABLE_ALIAS),
			eqSQL(column(NO_TABLE_ALIAS, "c1"), parameter(DBType.STRING, "x"))));
	}

	public void testUpdate() {
		assertCopy(update(
			table("table1", NO_TABLE_ALIAS),
			eqSQL(column(NO_TABLE_ALIAS, "c1"), parameter(DBType.STRING, "x")),
			columnNames("c2"),
			expressions(function(SQLFun.isTrue, isNull(column(NO_TABLE_ALIAS, "c3"))))));
	}

	public void testCast() {
		assertCopy(cast(column("col", "t1"), DBType.STRING, 255, 0, true));
	}

	public void testInSet() {
		assertCopy(inSet(column("col", "t1"), setParameter("foo", DBType.STRING)));
	}

	public void testSetLiteral() {
		assertCopy(setLiteral(Arrays.asList("a", "b"), DBType.STRING));
	}

	public void testCase() {
		assertCopy(sqlCase(isNull(column("t", "c1")), column("t", "c2"), column("t", "c3")));
	}

	public void testNullParameter() {
		assertCopy(nullParameter(DBType.STRING, "x"));
	}

	public void testTuple() {
		assertCopy(tuple(column("col", "t1"), literalInteger(42)));
	}

	public void testAlter() {
		assertCopy(addColumn(table("table", NO_TABLE_ALIAS), "string", DBType.STRING).setSize(1024L));
		assertCopy(dropColumn(table("table", NO_TABLE_ALIAS), "string"));
	}

	private List<String> columnNames(String... columnNames) {
		return Arrays.asList(columnNames);
	}

	private void assertCopy(SQLPart sql) {
		SQLPart copied = copy(sql);
		assertEquals(sql.toString(), copied.toString());

		SQLInlineTransformation<Collection<SQLPart>> partCollector =
			new SQLInlineTransformation<>() {
				@Override
				protected <P extends SQLPart> P transform(P part, java.util.Collection<SQLPart> arg) {
					arg.add(part);
					return part;
				}
			};

		Set<SQLPart> origParts = new HashSet<>();
		sql.visit(partCollector, origParts);
		Set<SQLPart> copiedParts = new HashSet<>();
		copied.visit(partCollector, copiedParts);
		assertEquals(origParts.size(), copiedParts.size());
		origParts.retainAll(copiedParts);
		assertTrue("Some parts have not been copied: " + origParts, origParts.isEmpty());
	}

}

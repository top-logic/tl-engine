/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.model.visit.DefaultDBSchemaVisitor;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLDelete;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFun;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLInsertSelect;
import com.top_logic.basic.db.sql.SQLLiteral;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLUnion;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLLoader;

/**
 * The class {@link TestCompiledStatement} tests {@link CompiledStatement}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCompiledStatement extends BasicTestCase {

	private static final String TABLE_NAME = "TestComiledStatement";

	private static final String NODE_COLUMN_NAME = "node";

	private static final String KEY_COLUMN_NAME = "key";

	private static final String VALUE_COLUMN_NAME = "value";

	private ConnectionPool _pool;

	private DBHelper _sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_sqlDialect = _pool.getSQLDialect();

		DBSchema schema = createDBSchema();
		DBTable table = createTable(TABLE_NAME);
		DBColumn node = createColumn(NODE_COLUMN_NAME);
		node.setType(DBType.STRING);
		node.setSize(63);
		node.setMandatory(true);
		table.getColumns().add(node);
		DBColumn key = createColumn(KEY_COLUMN_NAME);
		key.setType(DBType.STRING);
		key.setSize(255);
		key.setMandatory(true);
		table.getColumns().add(key);
		DBColumn value = createColumn(VALUE_COLUMN_NAME);
		value.setType(DBType.STRING);
		value.setSize(255);
		value.setMandatory(true);
		table.getColumns().add(value);
		DBPrimary primary = createPrimary();
		primary.getColumnRefs().add(ref(node));
		primary.getColumnRefs().add(ref(key));
		table.setPrimaryKey(primary);
		schema.getTables().add(table);

		DBSchemaUtils.recreateTables(_pool, schema);
	}

	@Override
	protected void tearDown() throws Exception {
		_sqlDialect = null;
		_pool = null;
		super.tearDown();
	}

	public void testTuple() throws SQLException, IOException {
		String tableName = "tcs_tuple";

		// Create test table.
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(tableName);
		defineColumn(table, "c1", DBType.STRING, 100, true);
		defineColumn(table, "c2", DBType.STRING, 100, true);
		schema.getTables().add(table);

		createTables(schema);

		// Fill table.
		SQLQuery<SQLInsert> insert = query(
			parameters(
				parameterDef(DBType.STRING, "x"),
				parameterDef(DBType.STRING, "y")),
			insert(
				table(tableName, "s"),
				columnNames("c1", "c2"),
				expressions(
					parameter(DBType.STRING, "x"),
					parameter(DBType.STRING, "y"))));
		
		SQLQuery<SQLSelect> select = query(
			parameters(
				setParameterDef("tuples", DBType.STRING, DBType.STRING)),
				select(
					columns(
						columnDef(column("t", "c1"), "c1"),
						columnDef(column("t", "c2"), "c2")),
					table(tableName, "t"),
				inSet(tuple(column("t", "c1"), column("t", "c2")),
					setParameter("tuples", DBType.STRING, DBType.STRING))));

		CompiledStatement insertStatement = toSql(insert);
		CompiledStatement selectStatement = toSql(select);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Batch batch = insertStatement.createBatch(connection)) {
				batch.addBatch("foo", "bar");
				batch.addBatch("foo", "baz");
				batch.addBatch("aaa", "bbb");
				batch.executeBatch();
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		PooledConnection readConnection = _pool.borrowReadConnection();
		try {
			Set<Tuple> result = readResult(
				selectStatement.bind(list(TupleFactory.newTuple("foo", "bar"), TupleFactory.newTuple("aaa", "bbb"))), 
				DBType.STRING, DBType.STRING);

			assertEquals(set(TupleFactory.newTuple("foo", "bar"), TupleFactory.newTuple("aaa", "bbb")), result);
		} finally {
			_pool.releaseReadConnection(readConnection);
		}
	}

	private void defineColumn(DBTable table, String name, DBType type, int size, boolean binary) {
		DBColumn c1 = DBSchemaFactory.createColumn(name);
		c1.setType(type);
		c1.setSize(size);
		c1.setBinary(binary);
		table.getColumns().add(c1);
	}

	public void testFailDirectStatement() {
		SQLQuery<SQLSelect> select =
			query(
				parameters(setParameterDef("keySet", DBType.STRING)),
				select(
					columns(columnDef(column("x", NODE_COLUMN_NAME), "c")),
					table(TABLE_NAME, "t"),
					inSet(column("t", KEY_COLUMN_NAME), setParameter("keySet", DBType.STRING))));
		CompiledStatement sql = toSql(select);

		PooledConnection connection = _pool.borrowReadConnection();
		try {
			sql.executeQuery(connection, Arrays.asList("foo", "bar"));
			fail("Expected SQLException.");
		} catch (SQLException ex) {
			assertContains("SELECT", ex.getMessage());
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	public void testFailPreparedStatement() {
		SQLQuery<SQLSelect> select =
			query(
				select(
					columns(columnDef(column("x", NODE_COLUMN_NAME), "c")),
					table(TABLE_NAME, "t"),
					inSet(column("t", KEY_COLUMN_NAME), setLiteral(list("foo, bar"), DBType.STRING))));
		CompiledStatement sql = toSql(select);

		PooledConnection connection = _pool.borrowReadConnection();
		try {
			sql.executeQuery(connection);
			fail("Expected SQLException.");
		} catch (SQLException ex) {
			assertContains("SELECT", ex.getMessage());
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	public void testSelectAll() throws SQLException {
		SQLQuery<SQLSelect> select =
			query(select(columns(columnDef(column("t", NODE_COLUMN_NAME), "c")), table(TABLE_NAME, "t"),
				literalTrueLogical()));
		CompiledStatement sql = toSql(select);

		HashSet<Tuple> result = readResult(sql, DBType.STRING);
		// Just test, that no error occurs.
		assertNotNull(result);
	}

	public void testSelectNone() throws SQLException {
		SQLQuery<SQLSelect> select =
			query(select(columns(columnDef(column("t", NODE_COLUMN_NAME), "c")), table(TABLE_NAME, "t"),
				literalFalseLogical()));
		CompiledStatement sql = toSql(select);

		HashSet<Tuple> result = readResult(sql, DBType.STRING);
		assertTrue(result.isEmpty());
	}

	public void testToStringSet() {
		SQLQuery<SQLSelect> select =
			query(
				parameters(setParameterDef("keySet", DBType.STRING)),
				select(
					columns(columnDef(column("t", NODE_COLUMN_NAME), "c")),
					table(TABLE_NAME, "t"),
					inSet(column("t", KEY_COLUMN_NAME), setParameter("keySet", DBType.STRING))));

		CompiledStatement statement1 = toSql(select);
		assertTrue(statement1.toString(), statement1.toString().contains("?"));

		CompiledStatement statement0 = statement1.bind(Arrays.asList("foo", "bar"));
		assertFalse(statement0.toString(), statement0.toString().contains("?"));
	}
	
	public void testToString() {
		SQLQuery<SQLInsert> insert = query(
			parameters(
				parameterDef(DBType.STRING, "x"),
				parameterDef(DBType.INT, "y")),
			insert(
				table("foo", "s"),
				columnNames("c1", "c2"),
				expressions(
					parameter(DBType.STRING, "x"),
					parameter(DBType.INT, "y"))));

		CompiledStatement insertStatement = toSql(insert);
		assertTrue(insertStatement.toString(), insertStatement.toString().contains("?"));
	}
	
	public void testSetParameter() throws SQLException {
		SQLQuery<SQLSelect> select =
			query(
				parameters(setParameterDef("keySet", DBType.STRING)),
				select(
					columns(columnDef(column("t", NODE_COLUMN_NAME), "c")),
					table(TABLE_NAME, "t"),
					inSet(column("t", KEY_COLUMN_NAME), setParameter("keySet", DBType.STRING))));
		CompiledStatement sql = toSql(select).bind(Arrays.asList("foo", "bar"));

		HashSet<Tuple> result = readResult(sql, DBType.STRING);
		// Just test, that no error occurs.
		assertNotNull(result);
	}

	public void testArithmeticExpressions() throws SQLException {
		SQLQuery<SQLSelect> select =
			query(
			select(
				columns(columnDef(
					add(
						literalInteger(7),
						mul(
							sub(
								div(
									literalInteger(16),
									literalInteger(2)),
								literalInteger(6)),
							literalInteger(3))),
					"c")),
				dual(),
				literalTrueLogical()));
		CompiledStatement sql = toSql(select);

		HashSet<Tuple> result = readResult(sql, DBType.INT);
		assertEquals(set(TupleFactory.newTuple(13)), result);
	}

	private HashSet<Tuple> readResult(CompiledStatement sql, DBType... types) throws SQLException {
		HashSet<Tuple> result = new HashSet<>();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			ResultSet resultSet = sql.executeQuery(connection);
			try {
				while (resultSet.next()) {
					int cnt = types.length;
					Object[] row = new Object[cnt];
					for (int column = 1; column <= cnt; column++) {
						row[column - 1] = _sqlDialect.mapToJava(resultSet, column, types[column - 1]);
					}
					result.add(TupleFactory.newTuple(row));
				}
			} finally {
				resultSet.close();
			}
			connection.commit();
		} catch (SQLException ex) {
			fail("Statement failed: " + sql.toString(), ex);
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		return result;
	}

	public void testInsertSelect() throws SQLException, IOException {
		String sourceTable = "tcs_source";
		String targetTable = "tcs_target";
		
		// Create test tables.
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable source = DBSchemaFactory.createTable(sourceTable);
		DBColumn sc1 = DBSchemaFactory.createColumn("c1");
		sc1.setType(DBType.STRING);
		sc1.setSize(100);
		source.getColumns().add(sc1);
		DBColumn sc2 = DBSchemaFactory.createColumn("c2");
		sc2.setType(DBType.INT);
		source.getColumns().add(sc2);
		schema.getTables().add(source);

		DBTable target = DBSchemaFactory.createTable(targetTable);
		DBColumn tc1 = DBSchemaFactory.createColumn("c1");
		tc1.setType(DBType.INT);
		target.getColumns().add(tc1);
		DBColumn tc2 = DBSchemaFactory.createColumn("c2");
		tc2.setType(DBType.STRING);
		tc2.setSize(100);
		target.getColumns().add(tc2);
		schema.getTables().add(target);

		createTables(schema);
		
		// Fill source table.
		SQLQuery<SQLInsert> insert = query(
			parameters(
				parameterDef(DBType.STRING, "x"),
				parameterDef(DBType.INT, "y")),
			insert(
				table(sourceTable, "s"),
				columnNames("c1", "c2"), 
				expressions(
					parameter(DBType.STRING, "x"),
					parameter(DBType.INT, "y"))));

		CompiledStatement insertStatement = toSql(insert);

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Batch batch = insertStatement.createBatch(connection)) {
				batch.addBatch("foo", 13);
				batch.addBatch("bar", 42);
				batch.addBatch("foobar", 99);
				batch.executeBatch();
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		// Fire copy statement.
		SQLQuery<SQLInsertSelect> copy = query(
			insert(
				table(targetTable, "t"),
				columnNames("c1", "c2"),
				select(
					columns(
						columnDef(column("s", "c2"), "x"),
						columnDef(column("s", "c1"), "y")),
					table(sourceTable, "s"),
					ge(column("s", "c2"), literalInteger(20)))));

		CompiledStatement copyStatement = toSql(copy);
		executeUpdate(copyStatement);

		// Check result.
		SQLQuery<SQLSelect> fetch = query(
			select(
				columns(
					columnDef(column("t", "c1"), "x"),
					columnDef(column("t", "c2"), "y")),
				table(targetTable, "t")));

		HashSet<Tuple> result = readResult(toSql(fetch), DBType.INT, DBType.STRING);
		assertEquals(set(pair(42, "bar"), pair(99, "foobar")), result);
	}

	public void testCastBinaryString() throws SQLException, IOException {
		String tableName = "tcs_castbinary";
		
		// Create test table.
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(tableName);
		defineColumn(table, "c1", DBType.STRING, 100, true);
		schema.getTables().add(table);
		
		createTables(schema);
		
		// Fill source table.
		SQLQuery<SQLInsert> insert = query(
			parameters(
				parameterDef(DBType.STRING, "x")),
			insert(
				table(tableName, "s"),
				columnNames("c1"),
				expressions(
					parameter(DBType.STRING, "x"))));

		CompiledStatement insertStatement = toSql(insert);

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Batch batch = insertStatement.createBatch(connection)) {
				batch.addBatch("foo");
				batch.addBatch("bar");
				batch.addBatch("foobar");
				batch.executeBatch();

				connection.commit();
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		// Cast to char in comparison.
		{
			SQLQuery<SQLSelect> join = query(
				select(
					columns(
						columnDef(column("t1", "name"), "x1"),
						columnDef(column("t2", "c1"), "x2")),
					join(false,
						subQuery(
							select(
								columns(
								columnDef(literalString("foo"), "name")),
								dual()),
							"t1"),
						table(tableName, "t2"),

						// Comparison fails in MySQL without explicit cast. Problem is the
						// difference of
						// character sets in compared columns.
						eqSQL(
							column("t1", "name"),
							cast(column("t2", "c1"), DBType.STRING, 100, 0, false)))));

			// Check result.
			HashSet<Tuple> result = readResult(toSql(join), DBType.STRING, DBType.STRING);
			assertEquals(set(pair("foo", "foo")), result);
		}

		// Cast literal
		{
			SQLQuery<SQLSelect> join = query(
				select(
					columns(
						columnDef(column("t1", "name"), "x1"),
						columnDef(column("t2", "c1"), "x2")),
					join(false,
						subQuery(
							select(
								columns(
								columnDef(
									// Make sure that literal is interpreted as binary type.
									cast(literalString("foo"), DBType.STRING, 100, 0, true), "name")),
								dual()),
							"t1"),
						table(tableName, "t2"),

						eqSQL(
							column("t1", "name"),
							column("t2", "c1")))));

			// Check result.
			HashSet<Tuple> result = readResult(toSql(join), DBType.STRING, DBType.STRING);
			assertEquals(set(pair("foo", "foo")), result);
		}

		// Cast to binary in comparison.
		{
			SQLQuery<SQLSelect> join = query(
				select(
					columns(
						columnDef(column("t1", "name"), "x1"),
						columnDef(column("t2", "c1"), "x2")),
					join(false,
						subQuery(
							select(
								columns(
								columnDef(literalString("foo"), "name")),
								dual()),
							"t1"),
						table(tableName, "t2"),

						// Comparison fails in MySQL without explicit cast. Problem is the
						// difference of
						// character sets in compared columns.
						eqSQL(
							cast(column("t1", "name"), DBType.STRING, 100, 0, true),
							column("t2", "c1")))));

			// Check result.
			HashSet<Tuple> result = readResult(toSql(join), DBType.STRING, DBType.STRING);
			assertEquals(set(pair("foo", "foo")), result);
		}
	}

	private void executeUpdate(CompiledStatement statement) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			statement.executeUpdate(connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	private List<String> columnNames(String... columnNames) {
		return Arrays.asList(columnNames);
	}

	private void createTables(DBSchema schema) throws SQLException, IOException {
		tryDropTables(schema);

		String sql = schema.toSQL(_sqlDialect);

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			new SQLLoader(connection).executeSQL(sql);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	private void tryDropTables(DBSchema schema) {
		schema.visit(new DefaultDBSchemaVisitor<Void, Void>() {
			@Override
			public Void visitTable(DBTable model, Void arg) {
				PooledConnection connection = _pool.borrowWriteConnection();
				try {
					Statement statement = connection.createStatement();
					try {
						statement.execute("DROP TABLE " + _sqlDialect.tableRef(model.getDBName()));
					} finally {
						statement.close();
					}
					connection.commit();
				} catch (SQLException ex) {
					// Ignore.
				} finally {
					_pool.releaseWriteConnection(connection);
				}
				return null;
			}
		}, null);
	}

	public void testInsert() throws SQLException {
		SQLQuery<SQLInsert> query = createInsert("testInsert");

		CompiledStatement sql = toSql(query);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Batch batch = sql.createBatch(connection)) {
				int numberInserts = 15;
				int[] expectedResult = new int[numberInserts];
				for (int i = 0; i < expectedResult.length; i++) {
					String s = String.valueOf(i);
					batch.addBatch(s, s);
				}
				int[] results = batch.executeBatch();
				assertSame(numberInserts, results.length);
				for (int i = 0; i < numberInserts; i++) {
					int result = results[i];
					switch (result) {
						case Statement.SUCCESS_NO_INFO:
							// oracle returns this
							break;
						default:
							assertSame("Not successfull at insert " + i, 1, result);
					}
				}
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

	}

	private CompiledStatement toSql(SQLQuery<?> query) {
		return query.toSql(_sqlDialect);
	}

	private SQLQuery<SQLInsert> createInsert(String nodeNameSuffix) {
		DBType varcharType = DBType.STRING;
		List<String> columnNames = new ArrayList<>();
		columnNames.add(NODE_COLUMN_NAME);
		columnNames.add(KEY_COLUMN_NAME);
		columnNames.add(VALUE_COLUMN_NAME);
		List<SQLExpression> values = new ArrayList<>();
		values.add(nodeName(nodeNameSuffix));
		values.add(parameter(varcharType, KEY_COLUMN_NAME));
		values.add(parameter(varcharType, VALUE_COLUMN_NAME));
		SQLInsert insert = insert(table(TABLE_NAME, null), columnNames, values);
		ArrayList<Parameter> parameter = new ArrayList<>();
		parameter.add(parameterDef(varcharType, KEY_COLUMN_NAME));
		parameter.add(parameterDef(varcharType, VALUE_COLUMN_NAME));
		SQLQuery<SQLInsert> query = query(parameter, insert);
		return query;
	}

	private SQLLiteral nodeName(String nodeNameSuffix) {
		return literalString(TestCompiledStatement.class.getSimpleName() + "_" + nodeNameSuffix);
	}

	public void testUnion() throws SQLException {
		String node1 = "testUnion1";
		unionSetup(node1);
		String node2 = "testUnion2";
		unionSetup(node2);
		
		checkUnion(node1, node2, true);
		checkUnion(node1, node2, false);
	}

	private void checkUnion(String node1, String node2, boolean distinct) throws SQLException {
		String tableAlias = "t";
		SQLSelect selectNode1 = select(false, 
			Arrays.asList(columnDef(column(tableAlias, KEY_COLUMN_NAME), KEY_COLUMN_NAME),
				columnDef(column(tableAlias, VALUE_COLUMN_NAME), VALUE_COLUMN_NAME)),
			table(TABLE_NAME, tableAlias),
			eq(column(tableAlias, NODE_COLUMN_NAME), nodeName(node1)),
			Collections.<SQLOrder> emptyList());
		SQLSelect selectNode2 = select(false,
			Arrays.asList(columnDef(column(tableAlias, KEY_COLUMN_NAME), KEY_COLUMN_NAME),
				columnDef(column(tableAlias, VALUE_COLUMN_NAME), VALUE_COLUMN_NAME)),
			table(TABLE_NAME, tableAlias),
			eq(column(tableAlias, NODE_COLUMN_NAME), nodeName(node2)),
			Collections.<SQLOrder> emptyList());

		SQLUnion union = union(distinct, Arrays.asList(selectNode1, selectNode2));
		CompiledStatement sql = toSql(query(union));
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			ResultSet resultSet = sql.executeQuery(connection);
			try {
				List<Pair<String, String>> result = toSet(resultSet);
				if (distinct) {
					assertEquals(3, result.size());
				} else {
					assertEquals(6, result.size());
				}
				assertEquals(set(pair("a", "b"), pair("b", "c"), pair("c", "d")), toSet(result));
			} finally {
				resultSet.close();
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private List<Pair<String, String>> toSet(ResultSet resultSet) throws SQLException {
		List<Pair<String, String>> result = new ArrayList<>();
		while (resultSet.next()) {
			String key = resultSet.getString(KEY_COLUMN_NAME);
			String value = resultSet.getString(VALUE_COLUMN_NAME);
			result.add(pair(key, value));
		}
		return result;
	}

	private static <A, B> Pair<A, B> pair(A key, B value) {
		return new Pair<>(key, value);
	}

	private void unionSetup(String nodeNameSuffix) throws SQLException {
		simpleSetup(nodeNameSuffix, pair("a", "b"), pair("b", "c"), pair("c", "d"));
	}

	private void simpleSetup(String nodeNameSuffix, Pair... values) throws SQLException {
		SQLQuery<SQLInsert> insert = createInsert(nodeNameSuffix);

		CompiledStatement sql = toSql(insert);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Batch batch = sql.createBatch(connection)) {
				for (Pair p : values) {
					batch.addBatch(p.getFirst(), p.getSecond());
				}
				batch.executeBatch();
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

	}

	public void testDelete() throws SQLException {
		String nodeSuffix = "testDelete";
		simpleSetup(nodeSuffix, pair("a", "a"), pair("b", "a"), pair("c", "b"), pair("d", "c"));

		DBType varcharType = DBType.STRING;
		SQLExpression correctNode = eq(column(null, NODE_COLUMN_NAME), nodeName(nodeSuffix));
		SQLExpression correctValue = eq(parameter(varcharType, "value"), column(null, VALUE_COLUMN_NAME));
		SQLExpression condition = and(correctNode, correctValue);
		SQLDelete delete = delete(table(TABLE_NAME, NO_TABLE_ALIAS), condition);
		SQLQuery<SQLDelete> query = query(list(parameterDef(varcharType, "value")), delete);
		CompiledStatement sql = toSql(query);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			assertEquals(1, sql.executeUpdate(connection, "b"));
			assertEquals(2, sql.executeUpdate(connection, "a"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	public void testLeast() throws SQLException {
		String nodeSuffix = "testSQLFunctionLeast";
		String columnLabel = "result";
		simpleSetup(nodeSuffix, pair("1", "2"), pair("2", "1"), pair("3", "4"), pair("6", "1"));

		String alias = NO_TABLE_ALIAS;
		SQLExpression function =
			function(SQLFun.least, column(null, KEY_COLUMN_NAME), column(null, VALUE_COLUMN_NAME));
		SQLSelect select = select(
			false,
			Collections.singletonList(columnDef(function, columnLabel)), table(TABLE_NAME, alias),
			eq(column(alias, NODE_COLUMN_NAME), nodeName(nodeSuffix)),
			Collections.singletonList(order(false, column(alias, KEY_COLUMN_NAME))));
		CompiledStatement sql = toSql(query(select));
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			ResultSet result;
			try {
				result = sql.executeQuery(connection);
				if (isMSSQL()) {
					fail("Has known bug been fixed? Expected failure with MSSQL.");
				}
			} catch (SQLException ex) {
				if (ex.getErrorCode() == 195 && isMSSQL()) {
					// Known bug: Ticket #21212: Least is build in function.
					return;
				}
				throw ex;
			}
			try {
				assertTrue(result.next());
				assertEquals("1", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("1", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("3", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("1", result.getString(columnLabel));
				assertFalse(result.next());
			} finally {
				result.close();
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	public void testGreatest() throws SQLException {
		String nodeSuffix = "testSQLFunctionGreatest";
		String columnLabel = "result";
		simpleSetup(nodeSuffix, pair("1", "2"), pair("2", "1"), pair("3", "4"), pair("6", "1"));

		String alias = NO_TABLE_ALIAS;
		SQLExpression function =
			function(SQLFun.greatest, column(alias, KEY_COLUMN_NAME), column(null, VALUE_COLUMN_NAME));
		SQLSelect select = select(
			false,
			Collections.singletonList(columnDef(function, columnLabel)), table(TABLE_NAME, alias),
			eq(column(alias, NODE_COLUMN_NAME), nodeName(nodeSuffix)),
			Collections.singletonList(order(false, column(alias, KEY_COLUMN_NAME))));
		CompiledStatement sql = toSql(query(select));
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			ResultSet result;
			try {
				result = sql.executeQuery(connection);
				if (isMSSQL()) {
					fail("Known bug has been fixed? Expected failure with MSSQL.");
				}
			} catch (SQLException ex) {
				if (ex.getErrorCode() == 195 && isMSSQL()) {
					// Known bug: Ticket #21212: Greatest is build in function."
					return;
				}
				throw ex;
			}
			try {
				assertTrue(result.next());
				assertEquals("2", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("2", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("4", result.getString(columnLabel));
				assertTrue(result.next());
				assertEquals("6", result.getString(columnLabel));
				assertFalse(result.next());
			} finally {
				result.close();
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private boolean isMSSQL() throws SQLException {
		return _pool.getSQLDialect() instanceof MSSQLHelper;
	}

	public void testMinimum() throws SQLException {
		String nodeSuffix = "testSQLFunctionMinimum";
		String columnLabel = "result";
		simpleSetup(nodeSuffix, pair("1", "2"), pair("2", "1"), pair("3", "4"), pair("6", "1"));

		String alias = NO_TABLE_ALIAS;
		SQLExpression function = min(column(alias, VALUE_COLUMN_NAME));
		SQLSelect select = select(
			false,
			Collections.singletonList(columnDef(function, columnLabel)), table(TABLE_NAME, alias),
			eq(column(alias, NODE_COLUMN_NAME), nodeName(nodeSuffix)),
			NO_ORDERS);
		CompiledStatement sql = toSql(query(select));
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			ResultSet result = sql.executeQuery(connection);
			try {
				assertTrue(result.next());
				assertEquals("1", result.getString(columnLabel));
				assertFalse(result.next());
			} finally {
				result.close();
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	public void testMaximum() throws SQLException {
		String nodeSuffix = "testSQLFunctionMaximum";
		String columnLabel = "result";
		simpleSetup(nodeSuffix, pair("1", "2"), pair("2", "1"), pair("3", "4"), pair("6", "1"));

		String alias = NO_TABLE_ALIAS;
		SQLExpression function = max(column(alias, VALUE_COLUMN_NAME));
		SQLSelect select = select(
			false,
			Collections.singletonList(columnDef(function, columnLabel)), table(TABLE_NAME, alias),
			eq(column(alias, NODE_COLUMN_NAME), nodeName(nodeSuffix)),
			NO_ORDERS);
		CompiledStatement sql = toSql(query(select));
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			ResultSet result = sql.executeQuery(connection);
			try {
				assertTrue(result.next());
				assertEquals("4", result.getString(columnLabel));
				assertFalse(result.next());
			} finally {
				result.close();
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	@SuppressWarnings("unused")
	public static Test suite() {
		Test t;
		if (true) {
			t = DatabaseTestSetup.getDBTest(TestCompiledStatement.class);
		} else {
			TestFactory factory = new SingleTestFactory("testCastBinaryString");
			t = DatabaseTestSetup.getDBTest(TestCompiledStatement.class, factory);
		}
		t = ModuleTestSetup.setupModule(t);
		return t;
	}
}

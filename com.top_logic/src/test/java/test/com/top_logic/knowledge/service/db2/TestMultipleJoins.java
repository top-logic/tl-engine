/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.visit.DefaultDBSchemaVisitor;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLJoin;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLLoader;

/**
 * Test that a join of the form "A LEFTJOIN B LEFTJOIN C" always returns results for elements of A.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMultipleJoins extends AbstractDBKnowledgeBaseTest {

	String TABLE_NAME = "foo";

	private DBSchema _schema;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_schema = createSchema();
		createTable(_schema);
	}

	@Override
	protected void tearDown() throws Exception {
		tryDropTables(_schema);
		super.tearDown();
	}

	private void tryDropTables(DBSchema schema) {
		schema.visit(new DefaultDBSchemaVisitor<Void, Void>() {
			@Override
			public Void visitTable(DBTable model, Void arg) {
				PooledConnection connection = pool().borrowWriteConnection();
				try {
					Statement statement = connection.createStatement();
					try {
						statement.execute("DROP TABLE " + sqlDialect().tableRef(model.getDBName()));
					} finally {
						statement.close();
					}
					connection.commit();
				} catch (SQLException ex) {
					// Ignore.
				} finally {
					pool().releaseWriteConnection(connection);
				}
				return null;
			}
		}, null);
	}

	private void insertData() throws SQLException {
		// Fill source table.
		SQLQuery<SQLInsert> insert = query(
			parameters(
				parameterDef(DBType.INT, "id"),
				parameterDef(DBType.INT, "inherit"),
				parameterDef(DBType.INT, "parent")),
			insert(
				table(TABLE_NAME, "s"),
				columnNames("id", "inherit", "parentId"),
				expressions(
					parameter(DBType.INT, "id"),
					parameter(DBType.INT, "inherit"),
					parameter(DBType.INT, "parent"))));
		CompiledStatement sql = insert.toSql(sqlDialect());
		PooledConnection connection = pool().borrowWriteConnection();
		try {
			sql.executeUpdate(connection, 1, 0, null);
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
	}

	private DBSchema createSchema() {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(TABLE_NAME);
		DBColumn id = DBSchemaFactory.createColumn("id");
		id.setType(DBType.INT);
		table.getColumns().add(id);
		DBColumn inherit = DBSchemaFactory.createColumn("inherit");
		inherit.setType(DBType.INT);
		table.getColumns().add(inherit);
		DBColumn parent = DBSchemaFactory.createColumn("parentId");
		parent.setType(DBType.INT);
		table.getColumns().add(parent);
		schema.getTables().add(table);
		return schema;
	}

	private void createTable(DBSchema schema) throws SQLException, IOException {
		String sql = schema.toSQL(sqlDialect());
		PooledConnection connection = pool().borrowWriteConnection();
		try {
			new SQLLoader(connection).executeSQL(sql);
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
	}

	ConnectionPool pool() {
		return kb().getConnectionPool();
	}

	DBHelper sqlDialect() throws SQLException {
		return pool().getSQLDialect();
	}

	public void testMultipleJoin() throws SQLException {
		insertData();
		SQLJoin fooParent =
			leftJoin(
				table(TABLE_NAME, "foo"),
				table(TABLE_NAME, "parent"),
				and(eqSQL(column("foo", "parentId"), column("parent", "id")),
					eqSQL(column("foo", "inherit"), literalInteger(1))));
		SQLJoin join =
			leftJoin(
				fooParent,
				table(TABLE_NAME, "grantparent"),
				and(eqSQL(column("parent", "parentId"), column("grantparent", "id")),
					eqSQL(column("parent", "inherit"), literalInteger(1))));
		SQLQuery<SQLSelect> select =
			query(select(
				columns(columnDef(column("foo", "id"), "foo_id"), columnDef(column("parent", "id"), "parent_id"),
					columnDef(column("grantparent", "id"), "grandparent_id")), join));
		CompiledStatement sql = select.toSql(sqlDialect());
		PooledConnection connection = pool().borrowReadConnection();
		try {
			ResultSet result = sql.executeQuery(connection);
			try {
				assertTrue("A Left join without where must always have a result.", result.next());
				assertEquals(1, result.getInt("foo_id"));
				assertEquals(0, result.getInt("parent_id"));
				assertTrue(result.wasNull());
				assertEquals(0, result.getInt("grandparent_id"));
				assertTrue(result.wasNull());
			} finally {
				result.close();
			}
		} finally {
			pool().releaseReadConnection(connection);
		}

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMultipleJoins}.
	 */
	public static Test suite() {
		return suite(TestMultipleJoins.class);
	}
}

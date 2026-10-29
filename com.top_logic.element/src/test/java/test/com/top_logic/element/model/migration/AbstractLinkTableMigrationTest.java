/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;

/**
 * Base class for tests of {@link MigrationProcessor}s working on the link tables of references.
 *
 * <p>
 * The links are written to and read from the tables with SQL, so that the tests can create states
 * the knowledge base itself does not create (e.g. duplicate links). All rows written by a test use
 * identifiers from {@link #FIRST_ID} on and are removed after the test.
 * </p>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractLinkTableMigrationTest extends BasicTestCase {

	/**
	 * First identifier of the rows written by the test.
	 */
	protected static final long FIRST_ID = 900_000_000L;

	/**
	 * The revision of the current state, end of the life time of a link that is not deleted.
	 */
	protected static final long MAX = Long.MAX_VALUE;

	private final List<MOClass> _touchedTables = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		PooledConnection connection = pool().borrowWriteConnection();
		try (Statement statement = connection.createStatement()) {
			DBHelper sqlDialect = connection.getSQLDialect();
			for (MOClass table : _touchedTables) {
				statement.executeUpdate("DELETE FROM " + sqlDialect.tableRef(table.getDBMapping().getDBName())
					+ " WHERE " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " >= " + FIRST_ID);
			}
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
		_touchedTables.clear();
		super.tearDown();
	}

	protected static ConnectionPool pool() {
		return KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
	}

	protected static MORepository types() {
		return PersistencyLayer.getKnowledgeBase().getMORepository();
	}

	protected static MOClass table(String typeName) {
		MOClass result = (MOClass) types().getTypeOrNull(typeName);
		assertNotNull("No table '" + typeName + "'.", result);
		return result;
	}

	/**
	 * The table of the given type, whose rows of the test are removed after the test.
	 */
	private MOClass touch(String typeName) {
		MOClass table = table(typeName);
		if (!_touchedTables.contains(table)) {
			_touchedTables.add(table);
		}
		return table;
	}

	/**
	 * Writes a link row.
	 *
	 * @param valueRev
	 *        The revision of the destination, only stored in a table whose destination has a
	 *        revision column.
	 */
	protected void insertLink(String typeName, long id, long revMin, long revMax, long source, long attribute,
			long dest, long valueRev) throws SQLException {
		MOClass table = touch(typeName);
		MOReference sourceRef = (MOReference) table.getAttribute(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		MOReference destRef = (MOReference) table.getAttribute(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		MOReference attributeRef = (MOReference) table.getAttribute(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);

		Map<String, Object> values = new LinkedHashMap<>();
		values.put(BasicTypes.IDENTIFIER_DB_NAME, id);
		values.put(BasicTypes.REV_MIN_DB_NAME, revMin);
		values.put(BasicTypes.REV_MAX_DB_NAME, revMax);
		values.put(BasicTypes.REV_CREATE_DB_NAME, revMin);
		values.put(BasicTypes.BRANCH_DB_NAME, 1L);
		putReference(values, sourceRef, source, 0);
		putReference(values, destRef, dest, valueRev);
		putReference(values, attributeRef, attribute, 0);

		List<String> columns = new ArrayList<>();
		List<Object> args = new ArrayList<>();
		for (DBAttribute column : table.getDBMapping().getDBAttributes()) {
			if (values.containsKey(column.getDBName())) {
				columns.add(column.getDBName());
				args.add(values.get(column.getDBName()));
			}
		}

		PooledConnection connection = pool().borrowWriteConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ").append(sqlDialect.tableRef(table.getDBMapping().getDBName())).append(" (");
			for (int n = 0; n < columns.size(); n++) {
				sql.append(n == 0 ? "" : ", ").append(sqlDialect.columnRef(columns.get(n)));
			}
			sql.append(") VALUES (");
			for (int n = 0; n < columns.size(); n++) {
				sql.append(n == 0 ? "?" : ", ?");
			}
			sql.append(")");
			try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
				for (int n = 0; n < args.size(); n++) {
					statement.setObject(n + 1, args.get(n));
				}
				statement.executeUpdate();
			}
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
	}

	private static void putReference(Map<String, Object> values, MOReference reference, long id, long revision) {
		values.put(reference.getColumn(ReferencePart.name).getDBName(), id);
		DBAttribute typeColumn = reference.getColumn(ReferencePart.type);
		if (typeColumn != null) {
			values.put(typeColumn.getDBName(), ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE);
		}
		DBAttribute branchColumn = reference.getColumn(ReferencePart.branch);
		if (branchColumn != null) {
			values.put(branchColumn.getDBName(), 1L);
		}
		DBAttribute revisionColumn = reference.getColumn(ReferencePart.revision);
		if (revisionColumn != null) {
			values.put(revisionColumn.getDBName(), revision);
		}
	}

	/**
	 * Reads the life times of the rows of the test in the given table.
	 *
	 * @return Descriptions <code>id:[revMin, revMax]</code> ordered by identifier.
	 */
	protected List<String> rows(String typeName) throws SQLException {
		MOClass table = touch(typeName);
		List<String> result = new ArrayList<>();
		PooledConnection connection = pool().borrowReadConnection();
		try (Statement statement = connection.createStatement()) {
			DBHelper sqlDialect = connection.getSQLDialect();
			String id = sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME);
			try (ResultSet rows = statement.executeQuery("SELECT " + id + ", "
				+ sqlDialect.columnRef(BasicTypes.REV_MIN_DB_NAME) + ", "
				+ sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME) + " FROM "
				+ sqlDialect.tableRef(table.getDBMapping().getDBName()) + " WHERE " + id + " >= " + FIRST_ID
				+ " ORDER BY " + id)) {
				while (rows.next()) {
					result.add(row(rows.getLong(1), rows.getLong(2), rows.getLong(3)));
				}
			}
		} finally {
			pool().releaseReadConnection(connection);
		}
		return result;
	}

	/**
	 * Description of a row as returned by {@link #rows(String)}.
	 */
	protected static String row(long id, long revMin, long revMax) {
		return (id - FIRST_ID) + ":[" + revMin + ", " + (revMax == MAX ? "MAX" : Long.toString(revMax)) + "]";
	}

	/**
	 * Runs the given processor in a {@link MigrationContext} on the tables of the knowledge base.
	 *
	 * @param after
	 *        Further work with the context and connection after the processor, before the commit.
	 */
	protected BufferingProtocol migrate(MigrationProcessor processor, ContextAction after) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool().borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public MORepository getPersistentRepository() {
					return types();
				}
			};
			processor.doMigration(context, log, connection);
			if (after != null) {
				after.run(context, log, connection);
			}
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
		assertFalse("Migration reported errors: " + log.getErrors(), log.hasErrors());
		return log;
	}

	protected static void assertInfo(BufferingProtocol log, String expected) {
		assertTrue("Missing '" + expected + "' in: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains(expected)));
	}

	/**
	 * Work with a {@link MigrationContext}.
	 */
	protected interface ContextAction {

		/**
		 * Performs the work.
		 */
		void run(MigrationContext context, BufferingProtocol log, PooledConnection connection) throws Exception;

	}

	/**
	 * Suite running the given test class on the default knowledge base of the application.
	 */
	protected static Test suite(Class<? extends Test> testClass) {
		return TLTestSetup.createTLTestSetup(KBSetup.getSingleKBTest(testClass));
	}

}

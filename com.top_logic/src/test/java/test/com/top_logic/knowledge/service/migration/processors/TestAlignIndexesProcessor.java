/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import static java.util.Arrays.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.processors.AlignIndexesProcessor;
import com.top_logic.knowledge.service.migration.processors.TableSelection;

/**
 * Test of {@link AlignIndexesProcessor} converting the indexes of existing tables to changed index
 * definitions of the application schema.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}. The application schema is the configured schema
 * extended by {@link #CHANGED_INDEXES_META}, which makes the source index of
 * {@link #MEMBERS_ASSOCIATION} unique over source and destination and adds the index
 * {@link #ADDED_INDEX}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAlignIndexesProcessor extends AbstractDBKnowledgeBaseTest {

	private static final String BRANCH = BasicTypes.BRANCH_DB_NAME;

	private static final String IDENTIFIER = BasicTypes.IDENTIFIER_DB_NAME;

	private static final String REV_MAX = BasicTypes.REV_MAX_DB_NAME;

	private static final String CHANGED_INDEXES_META = "webinf://kbase/TestAlignIndexesMeta.xml";

	/** Type of the objects linked by {@link #MEMBERS_ASSOCIATION}. */
	private static final String GROUP_TYPE = "Group";

	/** Mandatory attribute of {@link #GROUP_TYPE}. */
	private static final String GROUP_NAME = "name";

	/** Association whose indexes are changed by {@link #CHANGED_INDEXES_META}. */
	private static final String MEMBERS_ASSOCIATION = "hasGroupMembers";

	/** Association whose indexes are the same in the stored and the application schema. */
	private static final String UNCHANGED_ASSOCIATION = "definesGroup";

	/** Database name of the index of the source reference of {@link #MEMBERS_ASSOCIATION}. */
	private static final String SOURCE_INDEX = "SOURCE";

	/** Database name of the index declared only in {@link #CHANGED_INDEXES_META}. */
	private static final String ADDED_INDEX = "MEMBERS";

	private static final String SOURCE_COLUMN = "SOURCE_ID";

	private static final String DEST_COLUMN = "DEST_ID";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	private boolean _branchLast;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_branchLast =
			SchemaSetup.effectiveBranchLast(KBUtils.getSchemaConfig(_kbConfig).getConfig(), _pool.getSQLDialect());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

		try (Transaction tx = _kb.beginTransaction()) {
			KnowledgeObject group1 = _kb.createKnowledgeObject(GROUP_TYPE);
			group1.setAttributeValue(GROUP_NAME, "align-index-group-1");
			KnowledgeObject group2 = _kb.createKnowledgeObject(GROUP_TYPE);
			group2.setAttributeValue(GROUP_NAME, "align-index-group-2");
			_kb.createAssociation(group1, group2, MEMBERS_ASSOCIATION);
			tx.commit();
		}

		storeSchema(configuredSchema());
	}

	@Override
	protected void tearDown() throws Exception {
		_setup.tearDown();
		_setup = null;
		_kb = null;
		_pool = null;
		_kbConfig = null;
		super.tearDown();
	}

	/**
	 * Converts the indexes to the changed definitions and back.
	 */
	public void testAlignChangedIndexes() throws Exception {
		String membersTable = tableName(MEMBERS_ASSOCIATION);
		String unchangedTable = tableName(UNCHANGED_ASSOCIATION);
		List<String> unchangedBefore = describeIndexes(extract(unchangedTable));

		assertEquals(key(SOURCE_COLUMN), names(index(extract(membersTable), SOURCE_INDEX)));
		assertDuplicateAccepted(membersTable);

		SchemaConfiguration changed = changedSchema();
		BufferingProtocol log = align(changed, MEMBERS_ASSOCIATION, UNCHANGED_ASSOCIATION);
		assertInfo(log, "Aligned the indexes of 1 of 2 tables.");
		assertInfo(log, "Changed index '" + SOURCE_INDEX + "' of table '" + membersTable + "'");
		assertInfo(log, "Created index '" + ADDED_INDEX + "' of table '" + membersTable + "'");
		assertFalse("Unchanged table touched: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("'" + unchangedTable + "'")));

		DBTable members = extract(membersTable);
		DBIndex sourceIndex = index(members, SOURCE_INDEX);
		assertTrue("Source index not unique.", sourceIndex.isUnique());
		assertEquals(key(SOURCE_COLUMN, DEST_COLUMN), names(sourceIndex));
		assertEquals(key(DEST_COLUMN, SOURCE_COLUMN), names(index(members, ADDED_INDEX)));
		assertDuplicateRejected(membersTable);
		assertEquals("Unchanged table modified.", unchangedBefore, describeIndexes(extract(unchangedTable)));

		// Aligning again finds nothing to do.
		storeSchema(changed);
		log = align(changed, MEMBERS_ASSOCIATION, UNCHANGED_ASSOCIATION);
		assertInfo(log, "Aligned the indexes of 0 of 2 tables.");

		// Back to the configured definitions: the unique index is converted back and the added
		// index is dropped.
		log = align(configuredSchema(), MEMBERS_ASSOCIATION);
		assertInfo(log, "Aligned the indexes of 1 of 1 tables.");
		assertInfo(log, "Dropped index '" + ADDED_INDEX + "' of table '" + membersTable + "'");
		members = extract(membersTable);
		sourceIndex = index(members, SOURCE_INDEX);
		assertFalse("Source index still unique.", sourceIndex.isUnique());
		assertEquals(key(SOURCE_COLUMN), names(sourceIndex));
		assertNull("Index not dropped.", findIndex(members, ADDED_INDEX));
		assertDuplicateAccepted(membersTable);
	}

	/**
	 * The selection of the tables by their super type.
	 */
	public void testSubtypeSelection() {
		TableSelection selection = TypedConfiguration.newConfigItem(AlignIndexesProcessor.Config.class);
		assertTrue(TableSelection.isEmpty(selection));

		selection.setSubtypesOf(asList(BasicTypes.ASSOCIATION_TYPE_NAME));
		selection.setExclude(asList(MEMBERS_ASSOCIATION));
		BufferingProtocol log = new BufferingProtocol();
		List<String> selected = TableSelection.resolve(log, selection, _kb.getMORepository()).stream()
			.map(MOClass::getName).collect(Collectors.toList());
		assertFalse(log.hasErrors());
		assertTrue(selected.contains(UNCHANGED_ASSOCIATION));
		assertFalse("Excluded type selected.", selected.contains(MEMBERS_ASSOCIATION));
		assertFalse("Abstract type selected.", selected.contains(BasicTypes.ASSOCIATION_TYPE_NAME));
		for (String name : selected) {
			MOClass type = (MOClass) _kb.getMORepository().getTypeOrNull(name);
			assertTrue(name, type.isSubtypeOf(BasicTypes.ASSOCIATION_TYPE_NAME));
			assertFalse(name, type.isAbstract());
		}

		selection.setTables(asList(MEMBERS_ASSOCIATION, "noSuchType"));
		selection.setSubtypesOf(asList());
		selection.setExclude(asList());
		List<MOClass> explicit = TableSelection.resolve(log, selection, _kb.getMORepository());
		assertEquals(1, explicit.size());
		assertEquals(MEMBERS_ASSOCIATION, explicit.get(0).getName());
		assertTrue("Unknown type not reported.", log.hasErrors());
	}

	private BufferingProtocol align(SchemaConfiguration target, String... tables) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return target;
				}
			};
			AlignIndexesProcessor.Config config = TypedConfiguration.newConfigItem(AlignIndexesProcessor.Config.class);
			config.setTables(asList(tables));
			AlignIndexesProcessor processor = TypedConfigUtil.createInstance(config);
			processor.doMigration(context, log, connection);
			if (log.hasErrors()) {
				connection.rollback();
			} else {
				connection.commit();
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), 0, log.getErrors().size());
		return log;
	}

	private static void assertInfo(BufferingProtocol log, String expected) {
		assertTrue("Missing '" + expected + "' in: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains(expected)));
	}

	private void assertDuplicateAccepted(String table) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			assertEquals(1, insertDuplicate(connection, table));
		} finally {
			connection.rollback();
			_pool.releaseWriteConnection(connection);
		}
	}

	private void assertDuplicateRejected(String table) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			insertDuplicate(connection, table);
			fail("Duplicate link accepted by table '" + table + "'.");
		} catch (SQLException ex) {
			// Expected: the unique index rejects the duplicate.
		} finally {
			connection.rollback();
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Inserts a copy of the rows of the given table with another identifier.
	 */
	private static int insertDuplicate(PooledConnection connection, String table) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		DBTable definition = DBSchemaUtils.extractTable(connection.getPool(), DBSchemaFactory.createDBSchema(), table);
		List<String> columns = new ArrayList<>();
		List<String> values = new ArrayList<>();
		for (DBColumn column : definition.getColumns()) {
			String ref = sqlDialect.columnRef(column.getDBName());
			columns.add(ref);
			values.add(column.getDBName().equalsIgnoreCase(IDENTIFIER) ? ref + " + 1000000" : ref);
		}
		try (Statement statement = connection.createStatement()) {
			return statement.executeUpdate("INSERT INTO " + sqlDialect.tableRef(table) + " ("
				+ String.join(", ", columns) + ") SELECT " + String.join(", ", values) + " FROM "
				+ sqlDialect.tableRef(table));
		}
	}

	private void storeSchema(SchemaConfiguration schema) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, schema);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * The resolved schema of the configuration.
	 */
	private SchemaConfiguration configuredSchema() throws ConfigurationException, SQLException {
		return resolve(KBUtils.getSchemaConfig(_kbConfig).getConfig());
	}

	/**
	 * The resolved schema of the configuration with the {@link #CHANGED_INDEXES_META}.
	 */
	private SchemaConfiguration changedSchema() throws ConfigurationException, SQLException {
		SchemaConfiguration schema = TypedConfiguration.copy(KBUtils.getSchemaConfig(_kbConfig).getConfig());
		ResourceDeclaration declaration = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
		declaration.setResource(CHANGED_INDEXES_META);
		schema.getDeclarations().add(declaration);
		return resolve(schema);
	}

	private SchemaConfiguration resolve(SchemaConfiguration schema) throws ConfigurationException, SQLException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaConfiguration resolved = context.getInstance(schema).resolve(context).getConfig();
		return SchemaSetup.withExplicitLayout(resolved, _pool.getSQLDialect());
	}

	/**
	 * The expected column order of a versioned index with the given key columns.
	 */
	private List<String> key(String... keyColumns) {
		List<String> result = new ArrayList<>();
		if (!_branchLast) {
			result.add(BRANCH);
		}
		result.addAll(asList(keyColumns));
		if (_branchLast) {
			result.add(BRANCH);
		}
		result.add(REV_MAX);
		return result;
	}

	private String tableName(String typeName) {
		return ((MOClass) _kb.getMORepository().getTypeOrNull(typeName)).getDBMapping().getDBName();
	}

	private DBTable extract(String tableName) throws SQLException {
		DBTable result = DBSchemaUtils.extractTable(_pool, DBSchemaFactory.createDBSchema(), tableName);
		assertNotNull("No table '" + tableName + "'.", result);
		return result;
	}

	private DBIndex index(DBTable table, String indexName) throws SQLException {
		DBIndex result = findIndex(table, indexName);
		assertNotNull("No index '" + indexName + "' in table '" + table.getName() + "', indexes: "
			+ describeIndexes(table), result);
		return result;
	}

	private DBIndex findIndex(DBTable table, String indexName) throws SQLException {
		String dbName = _pool.getSQLDialect().qualifiedName(table.getName(), indexName);
		for (DBIndex index : table.getIndices()) {
			if (index.getName().equalsIgnoreCase(dbName)) {
				return index;
			}
		}
		return null;
	}

	private static List<String> describeIndexes(DBTable table) {
		return table.getIndices().stream()
			.map(index -> index.getName().toUpperCase() + (index.isUnique() ? "(unique)" : "") + names(index))
			.sorted()
			.collect(Collectors.toList());
	}

	private static List<String> names(DBIndex index) {
		List<String> result = new ArrayList<>();
		for (DBColumnRef ref : index.getColumnRefs()) {
			result.add(ref.getName().toUpperCase());
		}
		return result;
	}

	private static KnowledgeBaseConfiguration migrationKBConfig() throws ConfigurationException {
		KnowledgeBaseFactoryConfig factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		KnowledgeBaseConfiguration result = TypedConfiguration.copy(
			factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME));
		result.update(result.descriptor().getProperty(KnowledgeBaseConfiguration.CONNECTION_POOL_PROPERTY),
			DatabaseTestSetup.MIGRATION_POOL_NAME);
		return result;
	}

	/**
	 * Suite of {@link TestAlignIndexesProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAlignIndexesProcessor.class);
	}

}

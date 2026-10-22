/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import static java.util.Arrays.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.db.schema.SchemaTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
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
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.processors.AlignTableLayoutProcessor;
import com.top_logic.util.TLContext;

/**
 * Test of {@link AlignTableLayoutProcessor} converting the tables of an existing database between
 * the layouts of {@link SchemaConfiguration#getBranchLast()} and
 * {@link SchemaConfiguration#hasMultipleBranches()}.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}, created with the configured layout: multiple
 * branches, and the branch position of the SQL dialect of the database.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAlignTableLayoutProcessor extends AbstractDBKnowledgeBaseTest {

	private static final String BRANCH = BasicTypes.BRANCH_DB_NAME;

	private static final String IDENTIFIER = BasicTypes.IDENTIFIER_DB_NAME;

	private static final String REV_MAX = BasicTypes.REV_MAX_DB_NAME;

	/** Type with a reference and a declared unique index. */
	private static final String LABEL_TYPE = "ObjectLabel";

	/** Attribute of {@link #LABEL_TYPE}. */
	private static final String LABEL_ATTRIBUTE = "label";

	/** Reference of {@link #LABEL_TYPE}. */
	private static final String TARGET_REFERENCE = "target";

	/** Database name of the index declared for {@link #LABEL_TYPE}. */
	private static final String LABEL_INDEX = "LABEL_IDX";

	/** Database name of the index of {@link #TARGET_REFERENCE}. */
	private static final String TARGET_INDEX = "TARGET";

	/** Type of the objects linked by {@link #MEMBERS_ASSOCIATION}. */
	private static final String GROUP_TYPE = "Group";

	/** Mandatory attribute of {@link #GROUP_TYPE}. */
	private static final String GROUP_NAME = "name";

	/** Association type between {@link #GROUP_TYPE} objects. */
	private static final String MEMBERS_ASSOCIATION = "hasGroupMembers";

	/** Database name of the index of the source reference of {@link #MEMBERS_ASSOCIATION}. */
	private static final String SOURCE_INDEX = "SOURCE";

	/** Attribute not declared in {@link #GROUP_TYPE}, stored in the flex data table. */
	private static final String FLEX_ATTRIBUTE = "alignFlex";

	private static final String FLEX_VALUE = "flex-value";

	private static final String LABEL_VALUE = "align-label";

	/** Type referenced by {@link #VERSION_TYPE}. */
	private static final String DOCUMENT_TYPE = "Document";

	/** Type with the historic reference {@link #DOC_REFERENCE}, which has a branch column. */
	private static final String VERSION_TYPE = "DocumentVersion";

	/** Historic reference of {@link #VERSION_TYPE}. */
	private static final String DOC_REFERENCE = "doc";

	/** Branch column of {@link #DOC_REFERENCE}. */
	private static final String DOC_BRANCH_COLUMN = "DOC_BRC";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	private ObjectKey _labelKey;

	private ObjectKey _group1Key;

	private ObjectKey _group2Key;

	private ObjectKey _documentKey;

	private ObjectKey _versionKey;

	/**
	 * The branch position of the configured schema on the test database.
	 */
	private boolean _defaultBranchLast;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_defaultBranchLast =
			SchemaSetup.isBranchLast(KBUtils.getSchemaConfig(_kbConfig).getConfig(), _pool.getSQLDialect());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

		KnowledgeObject document;
		Revision documentRevision;
		try (Transaction tx = _kb.beginTransaction()) {
			KnowledgeObject group1 = _kb.createKnowledgeObject(GROUP_TYPE);
			group1.setAttributeValue(GROUP_NAME, "align-group-1");
			group1.setAttributeValue(FLEX_ATTRIBUTE, FLEX_VALUE);
			KnowledgeObject group2 = _kb.createKnowledgeObject(GROUP_TYPE);
			group2.setAttributeValue(GROUP_NAME, "align-group-2");
			_kb.createAssociation(group1, group2, MEMBERS_ASSOCIATION);

			KnowledgeObject label = _kb.createKnowledgeObject(LABEL_TYPE);
			label.setAttributeValue(LABEL_ATTRIBUTE, LABEL_VALUE);
			label.setAttributeValue(TARGET_REFERENCE, group1);

			document = _kb.createKnowledgeObject(DOCUMENT_TYPE);
			document.setAttributeValue("name", "align-document");
			document.setAttributeValue("size", 0L);
			document.setAttributeValue("updateRevisionNumber", 0L);
			tx.commit();
			documentRevision = tx.getCommitRevision();

			_group1Key = group1.tId();
			_group2Key = group2.tId();
			_labelKey = label.tId();
			_documentKey = document.tId();
		}
		try (Transaction tx = _kb.beginTransaction()) {
			KnowledgeObject version = _kb.createKnowledgeObject(VERSION_TYPE);
			version.setAttributeValue("revision", 1);
			version.setAttributeValue(DOC_REFERENCE,
				_kb.getHistoryManager().getKnowledgeItem(documentRevision, document));
			tx.commit();
			_versionKey = version.tId();
		}

		// The database of the application was created with the configured schema.
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME,
				KBUtils.getSchemaConfigResolved(_kbConfig).getConfig());
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertTrue(storedSchema().hasMultipleBranches());
		assertEquals(Boolean.valueOf(_defaultBranchLast), storedSchema().getBranchLast());
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
	 * Converts the tables to the other branch position and back.
	 */
	public void testConvertBranchLast() throws Exception {
		boolean other = !_defaultBranchLast;
		try {
			assertLayout(true, _defaultBranchLast);

			BufferingProtocol log = align(true, other);
			assertTrue("No tables changed: " + log.getInfos(),
				log.getInfos().stream().anyMatch(info -> info.contains("Changed primary key")));
			assertLayout(true, other);
			assertStored(true, other);
			assertData(true, other);

			align(true, _defaultBranchLast);
			assertLayout(true, _defaultBranchLast);
			assertStored(true, _defaultBranchLast);
			assertData(true, _defaultBranchLast);
		} finally {
			restoreDefaultLayout();
		}
	}

	/**
	 * Converts the tables to a single branch and back to multiple branches.
	 */
	public void testConvertSingleBranch() throws Exception {
		try {
			align(false, _defaultBranchLast);
			assertLayout(false, _defaultBranchLast);
			assertStored(false, _defaultBranchLast);
			assertData(false, _defaultBranchLast);

			align(true, _defaultBranchLast);
			assertLayout(true, _defaultBranchLast);
			assertTrunk(tableName(LABEL_TYPE), BRANCH, 1);
			assertTrunk(AbstractFlexDataManager.FLEX_DATA_DB_NAME, BRANCH, 1);
			assertTrunk(tableName(VERSION_TYPE), DOC_BRANCH_COLUMN, 1);
			assertStored(true, _defaultBranchLast);
			assertData(true, _defaultBranchLast);
			assertCreateBranch();
		} finally {
			restoreDefaultLayout();
		}
	}

	/**
	 * Converts both layout options at once.
	 */
	public void testCombinedConversion() throws Exception {
		boolean other = !_defaultBranchLast;
		try {
			align(false, other);
			assertLayout(false, other);
			assertStored(false, other);
			assertData(false, other);

			align(true, other);
			assertLayout(true, other);
			assertStored(true, other);
			assertData(true, other);
		} finally {
			restoreDefaultLayout();
		}
	}

	/**
	 * A database with a branch other than the trunk is not converted to a single branch.
	 */
	public void testNoSingleBranchWithOtherBranches() throws Exception {
		HistoryManager hm = _kb.getHistoryManager();
		Branch branch = hm.createBranch(hm.getTrunk(), hm.getRevision(hm.getLastRevision()), null);
		assertTrue(branch.getBranchId() != TLContext.TRUNK_ID);
		String storedBefore = storedSchemaRaw();

		BufferingProtocol log = align(false, _defaultBranchLast, true);
		assertTrue("Missing error: " + log.getErrors(), log.getErrors().stream()
			.anyMatch(error -> error.contains(Long.toString(branch.getBranchId()))));
		assertEquals("Stored schema changed.", storedBefore, storedSchemaRaw());
		assertLayout(true, _defaultBranchLast);
	}

	/**
	 * Without a change of the layout options, the tables and the stored schema are not touched.
	 */
	public void testNoLayoutChange() throws Exception {
		String storedBefore = storedSchemaRaw();

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(new AssertProtocol(), connection);
			assertFalse(AlignTableLayoutProcessor.hasLayoutChange(context.getPersistentSchema(),
				context.getApplicationSchema()));
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		BufferingProtocol log = align(true, _defaultBranchLast);
		assertFalse("No changes expected: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("layout") || info.contains("Changed")));
		assertEquals(storedBefore, storedSchemaRaw());
		assertLayout(true, _defaultBranchLast);
	}

	/**
	 * The decision whether a conversion is necessary and the derivation of the target schema.
	 */
	public void testTargetSchema() throws ConfigurationException {
		SchemaConfiguration configured = configuredSchema(true, false);
		SchemaConfiguration branchLast = configuredSchema(true, true);
		SchemaConfiguration singleBranch = configuredSchema(false, false);

		assertFalse(AlignTableLayoutProcessor.hasLayoutChange(configured, configured));
		assertTrue(AlignTableLayoutProcessor.hasLayoutChange(configured, branchLast));
		assertTrue(AlignTableLayoutProcessor.hasLayoutChange(branchLast, configured));
		assertTrue(AlignTableLayoutProcessor.hasLayoutChange(configured, singleBranch));

		SchemaConfiguration target = AlignTableLayoutProcessor.withConfiguredLayout(configured, branchLast);
		assertEquals(Boolean.TRUE, target.getBranchLast());
		assertNotSame(configured, target);
		assertEquals("Stored schema not modified.", Boolean.FALSE, configured.getBranchLast());
		assertEquals("Types only change with multiple-branches.", item(configured), item(target));

		SchemaConfiguration single = AlignTableLayoutProcessor.withConfiguredLayout(configured, singleBranch);
		assertFalse(single.hasMultipleBranches());
		assertEquals(item(singleBranch), item(single));
		assertFalse(item(configured).equals(item(single)));

		SchemaConfiguration multi = AlignTableLayoutProcessor.withConfiguredLayout(single, configured);
		assertTrue(multi.hasMultipleBranches());
		assertEquals(item(configured), item(multi));
	}

	private static String item(SchemaConfiguration schema) {
		return TypedConfiguration.toString(schema.getMetaObjects().getTypes().get(BasicTypes.ITEM_TYPE_NAME));
	}

	private void restoreDefaultLayout() throws Exception {
		SchemaConfiguration stored = storedSchema();
		if (!stored.hasMultipleBranches() || stored.getBranchLast().booleanValue() != _defaultBranchLast) {
			align(true, _defaultBranchLast);
		}
	}

	private BufferingProtocol align(boolean multipleBranches, boolean branchLast) throws Exception {
		return align(multipleBranches, branchLast, false);
	}

	private BufferingProtocol align(boolean multipleBranches, boolean branchLast, boolean expectErrors)
			throws Exception {
		SchemaConfiguration configured = configuredSchema(multipleBranches, branchLast);
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return configured;
				}
			};
			AlignTableLayoutProcessor processor =
				TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(AlignTableLayoutProcessor.Config.class));
			processor.doMigration(context, log, connection);
			if (log.hasErrors()) {
				connection.rollback();
			} else {
				connection.commit();
				assertEquals(multipleBranches, context.hasBranchSupport());
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		if (!expectErrors) {
			assertEquals("Errors: " + log.getErrors(), 0, log.getErrors().size());
		}
		return log;
	}

	/**
	 * The resolved application schema with the given layout options.
	 */
	private SchemaConfiguration configuredSchema(boolean multipleBranches, boolean branchLast)
			throws ConfigurationException {
		SchemaConfiguration schema = KBUtils.getSchemaConfig(_kbConfig).getConfig();
		schema = SchemaTestSetup.updateMultipleBranches(schema, decision(multipleBranches));
		schema = SchemaTestSetup.updateBranchLast(schema, decision(branchLast));
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		return context.getInstance(schema).resolve(context).getConfig();
	}

	private static Decision decision(boolean value) {
		return value ? Decision.TRUE : Decision.FALSE;
	}

	private void assertStored(boolean multipleBranches, boolean branchLast) {
		SchemaConfiguration stored = storedSchema();
		assertEquals(multipleBranches, stored.hasMultipleBranches());
		assertEquals(Boolean.valueOf(branchLast), stored.getBranchLast());
	}

	private void assertLayout(boolean multipleBranches, boolean branchLast) throws SQLException {
		DBTable label = extract(tableName(LABEL_TYPE));
		DBTable association = extract(tableName(MEMBERS_ASSOCIATION));
		DBTable flexData = extract(AbstractFlexDataManager.FLEX_DATA_DB_NAME);
		DBTable version = extract(tableName(VERSION_TYPE));

		assertEquals(multipleBranches, label.getColumn(BRANCH) != null);
		assertEquals(multipleBranches, association.getColumn(BRANCH) != null);
		assertEquals(multipleBranches, flexData.getColumn(BRANCH) != null);
		assertEquals(multipleBranches, version.getColumn(BRANCH) != null);
		assertEquals(multipleBranches, version.getColumn(DOC_BRANCH_COLUMN) != null);

		assertEquals(key(multipleBranches, branchLast, IDENTIFIER), primaryKey(label));
		assertEquals(key(multipleBranches, branchLast, IDENTIFIER), primaryKey(association));
		assertEquals(key(multipleBranches, branchLast, "TARGET_ID"), index(label, TARGET_INDEX));
		assertEquals(key(multipleBranches, branchLast, "LABEL"), index(label, LABEL_INDEX));
		assertEquals(key(multipleBranches, branchLast, "SOURCE_ID"), index(association, SOURCE_INDEX));
		assertEquals(key(multipleBranches, branchLast, AbstractFlexDataManager.IDENTIFIER_DBNAME,
			AbstractFlexDataManager.ATTRIBUTE_DBNAME), primaryKey(flexData));
	}

	/**
	 * The expected column order of a versioned index with the given key columns.
	 */
	private static List<String> key(boolean multipleBranches, boolean branchLast, String... keyColumns) {
		List<String> result = new ArrayList<>();
		if (multipleBranches && !branchLast) {
			result.add(BRANCH);
		}
		result.addAll(asList(keyColumns));
		if (multipleBranches && branchLast) {
			result.add(BRANCH);
		}
		result.add(REV_MAX);
		return result;
	}

	private void assertTrunk(String table, String column, int expectedRows) throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try (Statement stmt = connection.createStatement()) {
			DBHelper sqlDialect = connection.getSQLDialect();
			try (ResultSet result = stmt.executeQuery("SELECT " + sqlDialect.columnRef(column) + " FROM "
				+ sqlDialect.tableRef(table))) {
				int rows = 0;
				while (result.next()) {
					assertEquals("Column '" + column + "' of table '" + table + "'.", TLContext.TRUNK_ID,
						result.getLong(1));
					rows++;
				}
				assertTrue("Too few rows in table '" + table + "'.", rows >= expectedRows);
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Starts a knowledge base on the stored schema and checks that it reads the objects created in
	 * {@link #setUp()}.
	 */
	private void assertData(boolean multipleBranches, boolean branchLast) throws Exception {
		DBKnowledgeBase kb = startKB();
		try {
			assertEquals(multipleBranches, kb.getMORepository().multipleBranches());
			assertEquals(branchLast, kb.getMORepository().branchLast());

			KnowledgeItem group1 = resolve(kb, _group1Key);
			KnowledgeItem group2 = resolve(kb, _group2Key);
			KnowledgeItem label = resolve(kb, _labelKey);

			assertEquals(LABEL_VALUE, label.getAttributeValue(LABEL_ATTRIBUTE));
			assertSame(group1, label.getAttributeValue(TARGET_REFERENCE));
			assertEquals(FLEX_VALUE, group1.getAttributeValue(FLEX_ATTRIBUTE));

			Iterator<KnowledgeAssociation> links =
				((KnowledgeObject) group1).getOutgoingAssociations(MEMBERS_ASSOCIATION);
			assertTrue(links.hasNext());
			assertSame(group2, links.next().getDestinationObject());
			assertFalse(links.hasNext());

			KnowledgeItem version = resolve(kb, _versionKey);
			KnowledgeItem document = (KnowledgeItem) version.getAttributeValue(DOC_REFERENCE);
			assertNotNull(document);
			assertEquals(_documentKey.getObjectName(), document.tId().getObjectName());
		} finally {
			kb.shutDown();
		}
	}

	/**
	 * Starts a knowledge base on the stored schema and creates a branch in it.
	 */
	private void assertCreateBranch() throws Exception {
		DBKnowledgeBase kb = startKB();
		try {
			HistoryManager hm = kb.getHistoryManager();
			Branch branch = hm.createBranch(hm.getTrunk(), hm.getRevision(hm.getLastRevision()), null);
			KnowledgeItem label = hm.getKnowledgeItem(branch, Revision.CURRENT, kb.lookupType(LABEL_TYPE),
				_labelKey.getObjectName());
			assertNotNull("Object not branched.", label);
			assertEquals(LABEL_VALUE, label.getAttributeValue(LABEL_ATTRIBUTE));
		} finally {
			kb.shutDown();
		}
	}

	private DBKnowledgeBase startKB() {
		DBKnowledgeBase kb = new DBKnowledgeBase();
		kb.initialize(new AssertProtocol(), _kbConfig);
		kb.startup(new AssertProtocol());
		return kb;
	}

	private static KnowledgeItem resolve(DBKnowledgeBase kb, ObjectKey key) {
		KnowledgeItem result = kb.resolveObjectKey(new DefaultObjectKey(key.getBranchContext(),
			key.getHistoryContext(), kb.lookupType(key.getObjectType().getName()), key.getObjectName()));
		assertNotNull("Object not found: " + key, result);
		return result;
	}

	private SchemaConfiguration storedSchema() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private String storedSchemaRaw() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return KBSchemaUtil.loadSchemaRaw(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private String tableName(String typeName) {
		return ((MOClass) _kb.getMORepository().getTypeOrNull(typeName)).getDBMapping().getDBName();
	}

	private DBTable extract(String tableName) throws SQLException {
		DBTable result = DBSchemaUtils.extractTable(_pool, DBSchemaFactory.createDBSchema(), tableName);
		assertNotNull("No table '" + tableName + "'.", result);
		return result;
	}

	private static List<String> primaryKey(DBTable table) {
		return names(table.getPrimaryKey());
	}

	private List<String> index(DBTable table, String indexName) throws SQLException {
		String dbName = _pool.getSQLDialect().qualifiedName(table.getName(), indexName);
		for (DBIndex index : table.getIndices()) {
			if (index.getName().equalsIgnoreCase(dbName)) {
				return names(index);
			}
		}
		fail("No index '" + dbName + "' in table '" + table.getName() + "', indexes: " + table.getIndices());
		return null;
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
	 * Suite of {@link TestAlignTableLayoutProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAlignTableLayoutProcessor.class);
	}

}

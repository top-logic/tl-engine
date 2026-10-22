/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import static java.util.Arrays.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.processors.AlignTableLayoutProcessor;

/**
 * Test of {@link AlignTableLayoutProcessor} converting the tables of an existing database between
 * the layouts of {@link SchemaConfiguration#isBranchLast()}.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}, created with the default layout.
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

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	private ObjectKey _labelKey;

	private ObjectKey _group1Key;

	private ObjectKey _group2Key;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

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
			tx.commit();

			_group1Key = group1.tId();
			_group2Key = group2.tId();
			_labelKey = label.tId();
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
	 * Converts the tables to the branch-last layout and back.
	 */
	public void testConvertBranchLast() throws Exception {
		try {
			assertLayout(false);

			BufferingProtocol log = align(true);
			assertTrue("No tables changed: " + log.getInfos(),
				log.getInfos().stream().anyMatch(info -> info.contains("Changed primary key")));
			assertLayout(true);
			assertTrue(storedSchema().isBranchLast());
			assertData(true);

			align(false);
			assertLayout(false);
			assertFalse(storedSchema().isBranchLast());
			assertData(false);
		} finally {
			if (storedSchema().isBranchLast()) {
				align(false);
			}
		}
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

		BufferingProtocol log = align(false);
		assertFalse("No changes expected: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("layout") || info.contains("Changed")));
		assertEquals(storedBefore, storedSchemaRaw());
		assertLayout(false);
	}

	/**
	 * The decision whether a conversion is necessary.
	 */
	public void testHasLayoutChange() throws ConfigurationException {
		SchemaConfiguration configured = KBUtils.getSchemaConfigResolved(_kbConfig).getConfig();
		SchemaConfiguration branchLast = withBranchLast(configured, true);

		assertFalse(AlignTableLayoutProcessor.hasLayoutChange(configured, configured));
		assertTrue(AlignTableLayoutProcessor.hasLayoutChange(configured, branchLast));
		assertTrue(AlignTableLayoutProcessor.hasLayoutChange(branchLast, configured));

		SchemaConfiguration target = AlignTableLayoutProcessor.withConfiguredLayout(configured, branchLast);
		assertTrue(target.isBranchLast());
		assertNotSame(configured, target);
		assertFalse("Stored schema not modified.", configured.isBranchLast());
	}

	private BufferingProtocol align(boolean branchLast) throws SQLException {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return withBranchLast(super.getApplicationSchema(), branchLast);
				}
			};
			AlignTableLayoutProcessor processor =
				TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(AlignTableLayoutProcessor.Config.class));
			processor.doMigration(context, log, connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), 0, log.getErrors().size());
		return log;
	}

	private void assertLayout(boolean branchLast) throws SQLException {
		DBTable label = extract(tableName(LABEL_TYPE));
		DBTable association = extract(tableName(MEMBERS_ASSOCIATION));
		DBTable flexData = extract(AbstractFlexDataManager.FLEX_DATA_DB_NAME);
		if (branchLast) {
			assertEquals(asList(IDENTIFIER, BRANCH, REV_MAX), primaryKey(label));
			assertEquals(asList(IDENTIFIER, BRANCH, REV_MAX), primaryKey(association));
			assertEquals(asList("TARGET_ID", BRANCH, REV_MAX), index(label, TARGET_INDEX));
			assertEquals(asList("LABEL", BRANCH, REV_MAX), index(label, LABEL_INDEX));
			assertEquals(asList("SOURCE_ID", BRANCH, REV_MAX), index(association, SOURCE_INDEX));
			assertEquals(asList(AbstractFlexDataManager.IDENTIFIER_DBNAME, AbstractFlexDataManager.ATTRIBUTE_DBNAME,
				BRANCH, REV_MAX), primaryKey(flexData));
		} else {
			assertEquals(asList(BRANCH, IDENTIFIER, REV_MAX), primaryKey(label));
			assertEquals(asList(BRANCH, IDENTIFIER, REV_MAX), primaryKey(association));
			assertEquals(asList(BRANCH, "TARGET_ID", REV_MAX), index(label, TARGET_INDEX));
			assertEquals(asList(BRANCH, "LABEL", REV_MAX), index(label, LABEL_INDEX));
			assertEquals(asList(BRANCH, "SOURCE_ID", REV_MAX), index(association, SOURCE_INDEX));
			assertEquals(asList(BRANCH, AbstractFlexDataManager.IDENTIFIER_DBNAME,
				AbstractFlexDataManager.ATTRIBUTE_DBNAME, REV_MAX), primaryKey(flexData));
		}
	}

	/**
	 * Starts a knowledge base on the stored schema and checks that it reads the objects created in
	 * {@link #setUp()}.
	 */
	private void assertData(boolean branchLast) {
		DBKnowledgeBase kb = new DBKnowledgeBase();
		kb.initialize(new AssertProtocol(), _kbConfig);
		kb.startup(new AssertProtocol());
		try {
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
		} finally {
			kb.shutDown();
		}
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

	private static SchemaConfiguration withBranchLast(SchemaConfiguration schema, boolean branchLast) {
		SchemaConfiguration result = TypedConfiguration.copy(schema);
		result.update(result.descriptor().getProperty(SchemaConfiguration.BRANCH_LAST_ATTRIBUTE), branchLast);
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

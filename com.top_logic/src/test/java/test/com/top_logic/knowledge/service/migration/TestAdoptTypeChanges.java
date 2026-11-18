/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.ComputedMOAttribute;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.dob.attr.storage.ComputedAttributeStorage;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.knowledge.objects.KnowledgeItem;
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
import com.top_logic.knowledge.service.migration.MigrationService;
import com.top_logic.knowledge.service.migration.processors.AdoptTypeChangesProcessor;

/**
 * Test of {@link AdoptTypeChangesProcessor} writing changed type definitions that do not change
 * the tables to the stored schema, as done by the {@link MigrationService} at a start without
 * migrations.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}. The stored schema is the configured one with a
 * versioned {@link #LABEL_TYPE} that has the computed attributes {@link #COMPUTED_ATTRIBUTE} and
 * {@link #REMOVED_ATTRIBUTE}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAdoptTypeChanges extends AbstractDBKnowledgeBaseTest {

	/** Type of the test object. */
	private static final String LABEL_TYPE = "ObjectLabel";

	/** Type of the {@link #TARGET_REFERENCE target} of the test object. */
	private static final String GROUP_TYPE = "Group";

	/** Mandatory attribute of {@link #GROUP_TYPE}. */
	private static final String GROUP_NAME = "name";

	/** Mandatory attribute of {@link #LABEL_TYPE}. */
	private static final String LABEL_ATTRIBUTE = "label";

	private static final String LABEL_VALUE = "adopt-label";

	/** Mandatory reference of {@link #LABEL_TYPE}. */
	private static final String TARGET_REFERENCE = "target";

	/** Computed attribute of {@link #LABEL_TYPE} in the stored schema. */
	private static final String COMPUTED_ATTRIBUTE = "adoptComputed";

	/** Computed attribute of {@link #LABEL_TYPE} only in the stored schema. */
	private static final String REMOVED_ATTRIBUTE = "adoptRemoved";

	/** Computed attribute of {@link #LABEL_TYPE} only in the configured schema. */
	private static final String ADDED_ATTRIBUTE = "adoptAdded";

	/** Attribute with a database column only in the configured schema. */
	private static final String COLUMN_ATTRIBUTE = "adoptColumn";

	/** Database column of {@link #COLUMN_ATTRIBUTE}, derived from its name. */
	private static final String COLUMN_NAME = "ADOPT_COLUMN";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private ObjectKey _labelKey;

	/** The schema stored in the database before the test. */
	private SchemaConfiguration _stored;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		DBKnowledgeBase kb = _setup.setup();

		try (Transaction tx = kb.beginTransaction()) {
			KnowledgeObject group = kb.createKnowledgeObject(GROUP_TYPE);
			group.setAttributeValue(GROUP_NAME, "adopt-group");
			KnowledgeObject label = kb.createKnowledgeObject(LABEL_TYPE);
			label.setAttributeValue(LABEL_ATTRIBUTE, LABEL_VALUE);
			label.setAttributeValue(TARGET_REFERENCE, group);
			tx.commit();
			_labelKey = label.tId();
		}

		_stored = TypedConfiguration.copy(KBUtils.getSchemaConfigResolved(_kbConfig).getConfig());
		setVersioned(_stored, LABEL_TYPE, Decision.TRUE);
		addComputed(_stored, COMPUTED_ATTRIBUTE, ValueA.INSTANCE);
		addComputed(_stored, REMOVED_ATTRIBUTE, ValueA.INSTANCE);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, _stored);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_setup.tearDown();
		_setup = null;
		_pool = null;
		_kbConfig = null;
		_stored = null;
		super.tearDown();
	}

	/**
	 * A type configured as unversioned for a database created with the versioned type becomes
	 * unversioned without migration.
	 */
	public void testAdoptVersioned() throws Exception {
		DBKnowledgeBase before = startKB();
		try {
			assertTrue(labelType(before).isVersioned());
		} finally {
			before.shutDown();
		}

		SchemaConfiguration configured = TypedConfiguration.copy(_stored);
		setVersioned(configured, LABEL_TYPE, Decision.FALSE);

		BufferingProtocol log = adopt(configured);
		assertNoWarning(log);
		assertEquals(Decision.FALSE, storedType(LABEL_TYPE).isVersioned());

		DBKnowledgeBase kb = startKB();
		try {
			assertFalse("Type not unversioned in the knowledge base.", labelType(kb).isVersioned());
			assertEquals(LABEL_VALUE, label(kb).getAttributeValue(LABEL_ATTRIBUTE));
		} finally {
			kb.shutDown();
		}
	}

	/**
	 * A changed computation of a computed attribute, an added and a removed computed attribute are
	 * adopted without migration.
	 */
	public void testAdoptComputedAttributes() throws Exception {
		DBKnowledgeBase before = startKB();
		try {
			assertEquals(ValueA.VALUE, label(before).getAttributeValue(COMPUTED_ATTRIBUTE));
		} finally {
			before.shutDown();
		}

		SchemaConfiguration configured = TypedConfiguration.copy(_stored);
		labelConfig(configured).getAttributes().removeIf(attribute -> attribute.getAttributeName()
			.equals(COMPUTED_ATTRIBUTE) || attribute.getAttributeName().equals(REMOVED_ATTRIBUTE));
		addComputed(configured, COMPUTED_ATTRIBUTE, ValueB.INSTANCE);
		addComputed(configured, ADDED_ATTRIBUTE, ValueA.INSTANCE);

		BufferingProtocol log = adopt(configured);
		assertNoWarning(log);
		assertTrue("Configured type not stored.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(labelConfig(configured), storedType(LABEL_TYPE)));

		DBKnowledgeBase kb = startKB();
		try {
			KnowledgeItem label = label(kb);
			assertEquals(ValueB.VALUE, label.getAttributeValue(COMPUTED_ATTRIBUTE));
			assertEquals(ValueA.VALUE, label.getAttributeValue(ADDED_ATTRIBUTE));
			assertNull(labelType(kb).getAttributeOrNull(REMOVED_ATTRIBUTE));
		} finally {
			kb.shutDown();
		}
	}

	/**
	 * A change that adds a database column is not adopted but reported as warning, also when the
	 * column is added to an abstract type, while a change without table change is adopted.
	 */
	public void testColumnChangeNotAdopted() throws Exception {
		MetaObjectConfig storedLabel = storedType(LABEL_TYPE);
		MetaObjectConfig storedItem = storedType(BasicTypes.ITEM_TYPE_NAME);

		SchemaConfiguration configured = TypedConfiguration.copy(_stored);
		labelConfig(configured).getAttributes().add(columnAttribute());
		typeConfig(configured, BasicTypes.ITEM_TYPE_NAME).getAttributes().add(columnAttribute());
		setVersioned(configured, GROUP_TYPE, Decision.FALSE);

		BufferingProtocol log = adopt(configured);
		assertWarning(log, LABEL_TYPE);
		assertWarning(log, BasicTypes.ITEM_TYPE_NAME);

		assertTrue("Stored type changed.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storedLabel, storedType(LABEL_TYPE)));
		assertTrue("Stored type changed.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storedItem, storedType(BasicTypes.ITEM_TYPE_NAME)));
		assertEquals("Change without table change not adopted.", Decision.FALSE,
			storedType(GROUP_TYPE).isVersioned());

		DBKnowledgeBase kb = startKB();
		try {
			assertNull(labelType(kb).getAttributeOrNull(COLUMN_ATTRIBUTE));
			assertEquals(LABEL_VALUE, label(kb).getAttributeValue(LABEL_ATTRIBUTE));
		} finally {
			kb.shutDown();
		}
	}

	private static void assertNoWarning(BufferingProtocol log) {
		assertFalse("Unexpected warning: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("not adopted")));
	}

	private static void assertWarning(BufferingProtocol log, String typeName) {
		assertTrue("Missing warning for type '" + typeName + "': " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("type '" + typeName + "' is not adopted")
				&& info.contains("column '" + COLUMN_NAME + "' added")));
	}

	/**
	 * Runs the {@link AdoptTypeChangesProcessor} for all types of the given configured schema.
	 */
	private BufferingProtocol adopt(SchemaConfiguration configured) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return configured;
				}
			};
			AdoptTypeChangesProcessor.Config config =
				TypedConfiguration.newConfigItem(AdoptTypeChangesProcessor.Config.class);
			config.setTypes(new ArrayList<>(configured.getMetaObjects().getTypes().keySet()));
			AdoptTypeChangesProcessor processor = TypedConfigUtil.createInstance(config);
			processor.doMigration(context, log, connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), List.of(), log.getErrors());
		return log;
	}

	private static void setVersioned(SchemaConfiguration schema, String typeName, Decision value) {
		MetaObjectConfig type = typeConfig(schema, typeName);
		type.update(type.descriptor().getProperty(MetaObjectConfig.VERSIONED_PROPERTY), value);
	}

	private static void addComputed(SchemaConfiguration schema, String name, CacheValueFactory valueFactory) {
		ComputedAttributeStorage.Config storage = TypedConfiguration.newConfigItem(ComputedAttributeStorage.Config.class);
		storage.setValueFactory(valueFactory);

		PrimitiveAttributeConfig attribute = TypedConfiguration.newConfigItem(PrimitiveAttributeConfig.class);
		attribute.setImplementationClass(ComputedMOAttribute.class);
		attribute.setAttributeName(name);
		attribute.setValueType(MOPrimitive.STRING);
		attribute.setStorage(
			new ComputedAttributeStorage(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, storage));
		labelConfig(schema).getAttributes().add(attribute);
	}

	private static PrimitiveAttributeConfig columnAttribute() {
		PrimitiveAttributeConfig attribute = TypedConfiguration.newConfigItem(PrimitiveAttributeConfig.class);
		attribute.setAttributeName(COLUMN_ATTRIBUTE);
		attribute.setValueType(MOPrimitive.STRING);
		return attribute;
	}

	private static MetaObjectConfig labelConfig(SchemaConfiguration schema) {
		return typeConfig(schema, LABEL_TYPE);
	}

	private static MetaObjectConfig typeConfig(SchemaConfiguration schema, String typeName) {
		MetaObjectName type = schema.getMetaObjects().getTypes().get(typeName);
		assertNotNull("No type '" + typeName + "'.", type);
		return (MetaObjectConfig) type;
	}

	private MetaObjectConfig storedType(String typeName) {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return typeConfig(KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME),
				typeName);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private static MOClass labelType(DBKnowledgeBase kb) {
		return (MOClass) kb.getMORepository().getTypeOrNull(LABEL_TYPE);
	}

	private KnowledgeItem label(DBKnowledgeBase kb) {
		KnowledgeItem result = kb.resolveObjectKey(new DefaultObjectKey(_labelKey.getBranchContext(),
			_labelKey.getHistoryContext(), labelType(kb), _labelKey.getObjectName()));
		assertNotNull("Object not found: " + _labelKey, result);
		return result;
	}

	/**
	 * Starts a knowledge base on the stored schema.
	 */
	private DBKnowledgeBase startKB() {
		DBKnowledgeBase kb = new DBKnowledgeBase();
		kb.initialize(new AssertProtocol(), _kbConfig);
		kb.startup(new AssertProtocol());
		return kb;
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
	 * {@link CacheValueFactory} computing {@link #VALUE}.
	 */
	public static class ValueA implements CacheValueFactory {

		/** Value of the computed attribute. */
		public static final String VALUE = "value-a";

		/** Singleton {@link ValueA} instance. */
		public static final ValueA INSTANCE = new ValueA();

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return VALUE;
		}

		@Override
		public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage,
				MOAttribute changedAttribute) {
			return true;
		}

	}

	/**
	 * {@link CacheValueFactory} computing {@link #VALUE}.
	 */
	public static class ValueB implements CacheValueFactory {

		/** Value of the computed attribute. */
		public static final String VALUE = "value-b";

		/** Singleton {@link ValueB} instance. */
		public static final ValueB INSTANCE = new ValueB();

		@Override
		public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
			return VALUE;
		}

		@Override
		public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage,
				MOAttribute changedAttribute) {
			return true;
		}

	}

	/**
	 * Suite of {@link TestAdoptTypeChanges}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAdoptTypeChanges.class);
	}

}

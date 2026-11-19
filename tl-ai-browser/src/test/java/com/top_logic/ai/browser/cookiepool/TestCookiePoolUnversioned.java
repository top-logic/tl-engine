/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.browser.cookiepool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.base.cluster.ClusterManagerSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.base.cluster.ClusterManager;

import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.HistoryCleanup;

/**
 * Test of the unversioned table {@link #COOKIE_POOL} on a database written while the table was
 * versioned.
 *
 * <p>
 * A cookie pool is created and changed in a knowledge base whose schema declares
 * {@link #COOKIE_POOL} versioned. A knowledge base with the schema of the application, which
 * declares the table unversioned, then starts on the same database, reads the current state of the
 * pool, and {@link HistoryCleanup} removes the rows and the generic attribute values of the former
 * state.
 * </p>
 *
 * <p>
 * The test works on separate knowledge bases in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}. The pool is written on the level of the knowledge
 * base; its type reference points to a minimal model written along with it.
 * </p>
 */
public class TestCookiePoolUnversioned extends BasicTestCase {

	/** The table of the cookie pools. */
	private static final String COOKIE_POOL = "CookiePool";

	/** Attribute of {@link #COOKIE_POOL} with a column of its own. */
	private static final String NAME = "name";

	/** Attribute stored in the generic attribute table. */
	private static final String FLEX_ATTRIBUTE = "testState";

	/** Type reference of a {@link #COOKIE_POOL} object. */
	private static final String TYPE_REFERENCE = "tType";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _versionedKB;

	private DBKnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_setup.addAdditionalTypes(List.of(new VersionedCookiePool()));
		_versionedKB = _setup.setup();
		assertTrue(cookiePoolType(_versionedKB).isVersioned());
	}

	@Override
	protected void tearDown() throws Exception {
		if (_kb != null) {
			_kb.shutDown();
			_kb = null;
		}
		_setup.tearDown();
		_setup = null;
		_versionedKB = null;
		_pool = null;
		_kbConfig = null;
		super.tearDown();
	}

	/**
	 * Reads a cookie pool written to the versioned table, and removes its former state.
	 */
	public void testUnversionedOnVersionedData() throws Exception {
		KnowledgeObject pool;
		try (Transaction tx = _versionedKB.beginTransaction()) {
			pool = _versionedKB.createKnowledgeObject(COOKIE_POOL, Map.<String, Object> of(
				TYPE_REFERENCE, createTypeObject(_versionedKB),
				NAME, "pool-1").entrySet());
			pool.setAttributeValue(FLEX_ATTRIBUTE, "state-1");
			tx.commit();
		}
		try (Transaction tx = _versionedKB.beginTransaction()) {
			pool.setAttributeValue(NAME, "pool-2");
			pool.setAttributeValue(FLEX_ATTRIBUTE, "state-2");
			tx.commit();
		}
		ObjectKey key = pool.tId();
		long id = ((LongID) key.getObjectName()).longValue();
		assertEquals("Rows of the versioned pool.", 2, poolRows(id));
		assertEquals("Generic attribute values of the versioned pool.", 2, flexRows(id));

		_kb = new DBKnowledgeBase();
		_kb.initialize(new AssertProtocol(), _kbConfig);
		_kb.startup(new AssertProtocol());
		MOClass type = cookiePoolType(_kb);
		assertFalse(type.isVersioned());

		KnowledgeItem current = _kb.resolveObjectKey(
			new DefaultObjectKey(key.getBranchContext(), key.getHistoryContext(), type, key.getObjectName()));
		assertNotNull("Pool not found.", current);
		assertEquals("pool-2", current.getAttributeValue(NAME));
		assertEquals("state-2", current.getAttributeValue(FLEX_ATTRIBUTE));

		HistoryCleanup cleanup =
			new HistoryCleanup(new AssertProtocol(), _kb, List.of((DBTableMetaObject) type));
		// The test runs in cluster mode: the cleanup registers its revision, as its scheduler handle
		// does when started.
		ReflectionUtils.executeMethod(cleanup, "initTable", new Class[0], new Object[0]);
		try {
			// The cleanup runs on a revision committed after it was created.
			try (Transaction tx = _kb.beginTransaction()) {
				createTypeObject(_kb);
				tx.commit();
			}
			cleanup.run();
		} finally {
			ReflectionUtils.executeMethod(cleanup, "cleanupTable", new Class[0], new Object[0]);
		}

		assertEquals("Rows after the cleanup.", 1, poolRows(id));
		assertEquals("Generic attribute values after the cleanup.", 1, flexRows(id));
		assertEquals("pool-2", current.getAttributeValue(NAME));
		assertEquals("state-2", current.getAttributeValue(FLEX_ATTRIBUTE));
	}

	/**
	 * Creates the type object a {@link #COOKIE_POOL} object references: a class in a module of a
	 * model.
	 */
	private static KnowledgeObject createTypeObject(DBKnowledgeBase kb) throws Exception {
		KnowledgeObject model = kb.createKnowledgeObject("TLModel");
		KnowledgeObject module = kb.createKnowledgeObject("TLModule", Map.<String, Object> of(
			"name", "test.cookiepool." + model.tId().getObjectName(),
			"model", model).entrySet());
		return kb.createKnowledgeObject("MetaElement", Map.<String, Object> of(
			"name", COOKIE_POOL,
			"impl", "class",
			"module", module,
			"scope", module).entrySet());
	}

	private static MOClass cookiePoolType(DBKnowledgeBase kb) {
		return (MOClass) kb.getMORepository().getTypeOrNull(COOKIE_POOL);
	}

	/**
	 * Number of rows of the pool with the given identifier in its table.
	 */
	private int poolRows(long id) throws SQLException {
		String table = cookiePoolType(_versionedKB).getDBMapping().getDBName();
		return count(table, _pool.getSQLDialect().columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = " + id);
	}

	/**
	 * Number of generic attribute values of the pool with the given identifier.
	 */
	private int flexRows(long id) throws SQLException {
		DBHelper sqlDialect = _pool.getSQLDialect();
		return count(AbstractFlexDataManager.FLEX_DATA_DB_NAME,
			sqlDialect.columnRef(AbstractFlexDataManager.TYPE_DBNAME) + " = '" + COOKIE_POOL + "' AND "
				+ sqlDialect.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = " + id);
	}

	private int count(String table, String where) throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try (Statement stmt = connection.createStatement();
				ResultSet result = stmt.executeQuery(
					"SELECT COUNT(*) FROM " + connection.getSQLDialect().tableRef(table) + " WHERE " + where)) {
			assertTrue(result.next());
			return result.getInt(1);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private static KnowledgeBaseConfiguration migrationKBConfig() throws ConfigurationException {
		KnowledgeBaseFactoryConfig factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		KnowledgeBaseConfiguration result = TypedConfiguration.copy(
			factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME));
		result.update(result.descriptor().getProperty(KnowledgeBaseConfiguration.CONNECTION_POOL_PROPERTY),
			DatabaseTestSetup.MIGRATION_POOL_NAME);
		// The test runs the cleanup itself.
		result.update(
			result.descriptor().getProperty(KnowledgeBaseConfiguration.CLEANUP_UNVERSIONED_TYPES_INTERVAL_PROPERTY),
			Long.valueOf(0L));
		return result;
	}

	/**
	 * {@link TypeProvider} declaring {@link #COOKIE_POOL} versioned, as the schema did before.
	 */
	private static final class VersionedCookiePool implements TypeProvider {

		@Override
		public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
			((MOClass) typeRepository.getTypeOrNull(COOKIE_POOL)).setVersioned(true);
		}

	}

	/**
	 * Suite of {@link TestCookiePoolUnversioned}.
	 */
	public static Test suite() {
		TestFactory factory = ServiceTestSetup.createStarterFactoryForModules(DefaultTestFactory.INSTANCE,
			ConnectionPoolRegistry.Module.INSTANCE,
			DataAccessService.Module.INSTANCE,
			ClusterManager.Module.INSTANCE,
			FlexDataManagerFactory.Module.INSTANCE,
			SchedulerService.Module.INSTANCE);
		factory = ClusterManagerSetup.setupClusterManager(factory);
		Test test = DatabaseTestSetup.getDBTest(TestCookiePoolUnversioned.class, DatabaseTestSetup.DEFAULT_DB, factory);
		return ModuleLicenceTestSetup.setupModuleWithoutAdditionalServices(test);
	}

}

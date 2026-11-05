/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.processors.RecollateStringColumnsProcessor;

/**
 * Test of {@link RecollateStringColumnsProcessor}.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}.
 * </p>
 */
public class TestRecollateStringColumnsProcessor extends AbstractDBKnowledgeBaseTest {

	/** Table with a non-binary string column. */
	private static final String TABLE = "Unit";

	/** Non-binary string column of {@link #TABLE}. */
	private static final String COLUMN = "format";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

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
	 * Re-collates a single column and checks that its table is analyzed afterwards.
	 */
	public void testRecollateColumn() throws Exception {
		RecollateStringColumnsProcessor.Config<?> config = newConfig();
		set(config, RecollateStringColumnsProcessor.Config.TABLE, TABLE);
		set(config, RecollateStringColumnsProcessor.Config.COLUMN, COLUMN);

		List<String> infos = migrate(TypedConfigUtil.createInstance(config)).getInfos();

		DBHelper sqlDialect = _pool.getSQLDialect();
		if (!sqlDialect.supportsColumnCollation()) {
			assertContains(infos, "nothing to re-collate");
			return;
		}

		String dbColumn = ((MOClass) _kb.getMORepository().getTypeOrNull(TABLE)).getAttribute(COLUMN)
			.getDbMapping()[0].getDBName();
		assertContains(infos, "Re-collated column '" + dbColumn + "' of table '" + TABLE + "'.");
		assertAnalyzed(infos, sqlDialect, dbTableName(TABLE));
	}

	/**
	 * Re-collates all string columns of all tables and checks that each altered table is analyzed
	 * exactly once.
	 */
	public void testRecollateAllTables() throws Exception {
		List<String> infos = migrate(TypedConfigUtil.createInstance(newConfig())).getInfos();

		DBHelper sqlDialect = _pool.getSQLDialect();
		if (!sqlDialect.supportsColumnCollation()) {
			assertContains(infos, "nothing to re-collate");
			return;
		}

		assertContains(infos, "Re-collated column ");
		String analyzed = analyzedMessage(dbTableName(TABLE));
		if (sqlDialect.supportsAnalyzeTable()) {
			assertEquals("Table analyzed more than once: " + infos, 1,
				infos.stream().filter(analyzed::equals).count());
		} else {
			assertFalse(infos.contains(analyzed));
		}
	}

	private void assertAnalyzed(List<String> infos, DBHelper sqlDialect, String dbTableName) {
		String analyzed = analyzedMessage(dbTableName);
		if (sqlDialect.supportsAnalyzeTable()) {
			assertTrue("Table not analyzed: " + infos, infos.contains(analyzed));
		} else {
			assertFalse("Analyze without dialect support: " + infos, infos.contains(analyzed));
		}
	}

	private static String analyzedMessage(String dbTableName) {
		return "Analyzed table '" + dbTableName + "'.";
	}

	private static void assertContains(List<String> infos, String part) {
		assertTrue("Missing info '" + part + "': " + infos, infos.stream().anyMatch(info -> info.contains(part)));
	}

	private String dbTableName(String typeName) {
		return ((MOClass) _kb.getMORepository().getTypeOrNull(typeName)).getDBMapping().getDBName();
	}

	private static RecollateStringColumnsProcessor.Config<?> newConfig() {
		return TypedConfiguration.newConfigItem(RecollateStringColumnsProcessor.Config.class);
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private BufferingProtocol migrate(RecollateStringColumnsProcessor processor) throws SQLException {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection);
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
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaConfiguration resolved =
			context.getInstance(KBUtils.getSchemaConfig(_kbConfig).getConfig()).resolve(context).getConfig();
		return SchemaSetup.withExplicitLayout(resolved, _pool.getSQLDialect());
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
	 * Suite of {@link TestRecollateStringColumnsProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestRecollateStringColumnsProcessor.class);
	}

}

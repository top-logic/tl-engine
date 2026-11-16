/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.agent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.Create;
import com.top_logic.element.model.diff.config.Delete;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.layout.view.agent.AccessTokenAuthenticator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test of the migration script {@link #MIGRATION}, which deletes the module {@link #OLD_MODULE}
 * with its access tokens.
 *
 * <p>
 * The test configuration declares {@link #OLD_MODULE} as the application defined it before, and a
 * module {@link #OTHER_MODULE} whose objects are stored in the same generic table. The test creates
 * a token of {@link #OLD_MODULE} with an owner, an object of {@link #OTHER_MODULE} and an access
 * token of the type <code>tl.accounts:AccessToken</code>, runs the processors of the script, and
 * checks that only the old token and its module are gone.
 * </p>
 */
public class TestAgentTokenMigration extends BasicTestCase {

	private static final String MIGRATION_MODULE = "tl-layout-view-agent";

	private static final String MIGRATION = "Ticket_30367_remove_agent_access_tokens";

	private static final String OLD_MODULE = "tl.agent";

	private static final String OLD_TOKEN_TYPE = OLD_MODULE + ":AccessToken";

	private static final String OTHER_MODULE = "test.agentmigration";

	private static final String OTHER_TYPE = OTHER_MODULE + ":Other";

	private static final String OWNER = "owner";

	/**
	 * The module {@link #OLD_MODULE}, its token and everything stored for the token are deleted;
	 * other objects of the generic table and the shared access tokens are not touched, and the
	 * automatic model upgrade has nothing to create or delete afterwards. A second run, without
	 * {@link #OLD_MODULE}, changes nothing.
	 */
	public void testMigration() throws Exception {
		Person owner = PersonManager.getManager().getRoot();
		TLObject oldToken;
		TLObject other;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			oldToken = ModelService.getInstance().getFactory().createObject(type(OLD_TOKEN_TYPE));
			oldToken.tUpdateByName(OWNER, owner);
			oldToken.tUpdateByName("label", "old");
			oldToken.tUpdateByName("tokenId", "old-id");
			oldToken.tUpdateByName("secretHash", "old-hash");

			other = ModelService.getInstance().getFactory().createObject(type(OTHER_TYPE));
			other.tUpdateByName(OWNER, owner);
			other.tUpdateByName("name", "kept");
			tx.commit();
		}
		AccessToken shared = AccessTokens.create(owner, "shared", null,
			List.of(AccessTokenAuthenticator.UI_ACT_PERMISSION), "session").token();
		try {
			long oldId = id(oldToken);
			long otherId = id(other);
			long sharedId = id(shared);

			assertEquals(1, objectRows(TLAnnotations.GENERIC_TABLE_NAME, oldId));
			assertTrue("The old token has attributes.", flexRows(oldId) > 0);
			assertTrue("The old token has an owner link.", linkRows(oldId) > 0);
			int otherFlex = flexRows(otherId);
			int otherLinks = linkRows(otherId);
			int sharedLinks = linkRows(sharedId);
			assertTrue("The other object has attributes.", otherFlex > 0);
			assertTrue("The other object has an owner link.", otherLinks > 0);
			assertNotNull(module(OLD_MODULE));

			runMigration();

			assertNull("The module " + OLD_MODULE + " is deleted.", module(OLD_MODULE));
			assertEquals("The old token is deleted.", 0, objectRows(TLAnnotations.GENERIC_TABLE_NAME, oldId));
			assertEquals("The attributes of the old token are deleted.", 0, flexRows(oldId));
			assertEquals("The owner link of the old token is deleted.", 0, linkRows(oldId));

			assertEquals("Another object of the generic table is kept.", 1,
				objectRows(TLAnnotations.GENERIC_TABLE_NAME, otherId));
			assertEquals(otherFlex, flexRows(otherId));
			assertEquals(otherLinks, linkRows(otherId));
			assertEquals("A shared access token is kept.", 1, objectRows(AccessToken.TABLE, sharedId));
			assertEquals(sharedLinks, linkRows(sharedId));

			assertNoTypeCreatedOrDeletedByModelUpgrade();

			// Without the module, the script finds nothing to do.
			runMigration();
			assertNull(module(OLD_MODULE));
			assertEquals(1, objectRows(TLAnnotations.GENERIC_TABLE_NAME, otherId));
			assertEquals(1, objectRows(AccessToken.TABLE, sharedId));
		} finally {
			AccessTokens.revoke(shared);
		}
	}

	private static TLClass type(String qualifiedName) {
		return (TLClass) TLModelUtil.findType(qualifiedName);
	}

	private static long id(TLObject object) {
		return ((LongID) object.tId().getObjectName()).longValue();
	}

	/**
	 * Runs the processors of the migration script {@link #MIGRATION} as the migration does, and
	 * stores the resulting schema.
	 */
	private static void runMigration() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		MigrationConfig migration =
			MigrationUtil.readMigration(log, MIGRATION_MODULE, MIGRATION + MigrationUtil.MIGRATION_FILE_SUFFIX);
		assertFalse("Migration script not readable: " + log.getErrors(), log.hasErrors());

		PooledConnection connection = pool().borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection);
			for (PolymorphicConfiguration<? extends MigrationProcessor> processorConfig : migration.getProcessors()) {
				MigrationProcessor processor =
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(processorConfig);
				processor.doMigration(context, log, connection);
			}
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME,
				context.getPersistentSchema());
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
		assertFalse("Migration reported errors: " + log.getErrors(), log.hasErrors());
	}

	/**
	 * The automatic model upgrade on startup compares the stored baseline with the configured
	 * model, which in the application no longer contains {@link #OLD_MODULE}: it must neither
	 * create nor delete anything.
	 */
	private static void assertNoTypeCreatedOrDeletedByModelUpgrade() throws Exception {
		String baseline;
		PooledConnection connection = pool().borrowReadConnection();
		try {
			baseline = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY);
		} finally {
			pool().releaseReadConnection(connection);
		}
		assertNotNull("No model baseline stored.", baseline);

		ModelConfig configured = TypedConfiguration.copy(DynamicModelService.getInstance().getModelConfig());
		assertTrue(configured.getModules().removeIf(module -> module.getName().equals(OLD_MODULE)));

		BufferingProtocol log = new BufferingProtocol();
		TLModel stored = DynamicModelService.loadTransientModel(log, baseline);
		TLModel application = DynamicModelService.loadTransientModel(log, configured);
		assertFalse("Models not loadable: " + log.getErrors(), log.hasErrors());

		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(stored, application);
		List<String> changes = new ArrayList<>();
		for (DiffElement element : patchCreator.getPatch()) {
			if (element instanceof Create || element instanceof Delete) {
				changes.add(TypedConfiguration.toString(element));
			}
		}
		assertEquals("The model upgrade creates or deletes model parts.", List.of(), changes);
	}

	/**
	 * Number of rows of the object with the given ID in the given table.
	 */
	private static int objectRows(String tableName, long id) throws SQLException {
		MOClass table = (MOClass) repository().getMetaObject(tableName);
		return count(table.getDBMapping().getDBName(), List.of(BasicTypes.IDENTIFIER_DB_NAME), id);
	}

	/**
	 * Number of flex attribute values of the object with the given ID.
	 */
	private static int flexRows(long id) throws SQLException {
		return count(AbstractFlexDataManager.FLEX_DATA_DB_NAME, List.of(AbstractFlexDataManager.IDENTIFIER_DBNAME),
			id);
	}

	/**
	 * Number of rows of all link tables from or to the object with the given ID.
	 */
	private static int linkRows(long id) throws SQLException {
		MetaObject base = repository().getTypeOrNull(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
		int result = 0;
		for (MetaObject type : repository().getMetaObjects()) {
			if (!(type instanceof MOClass table) || table.isAbstract() || !table.isSubtypeOf(base)) {
				continue;
			}
			List<String> columns = new ArrayList<>();
			for (String end : List.of(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME,
				DBKnowledgeAssociation.REFERENCE_DEST_NAME)) {
				columns.add(((MOReference) table.getAttribute(end)).getColumn(ReferencePart.name).getDBName());
			}
			result += count(table.getDBMapping().getDBName(), columns, id);
		}
		return result;
	}

	/**
	 * Number of rows of the given table, in which one of the given columns holds the given ID.
	 */
	private static int count(String tableDBName, List<String> idColumns, long id) throws SQLException {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ").append(sql.tableRef(tableDBName))
				.append(" WHERE ");
			for (int n = 0; n < idColumns.size(); n++) {
				query.append(n == 0 ? "" : " OR ").append(sql.columnRef(idColumns.get(n))).append(" = ?");
			}
			try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
				for (int n = 0; n < idColumns.size(); n++) {
					statement.setLong(n + 1, id);
				}
				try (ResultSet rows = statement.executeQuery()) {
					rows.next();
					return rows.getInt(1);
				}
			}
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	/**
	 * The module with the given name in the database, or <code>null</code>.
	 */
	private static Module module(String name) throws Exception {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			Util util = new MigrationContext(new BufferingProtocol(), connection).getSQLUtils();
			return util.getTLModule(connection, TLContext.TRUNK_ID, name);
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	private static MORepository repository() {
		return PersistencyLayer.getKnowledgeBase().getMORepository();
	}

	private static ConnectionPool pool() {
		return KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(new TestSuite(TestAgentTokenMigration.class),
			ModelService.Module.INSTANCE, AccessTokenPermissions.Module.INSTANCE);
		innerTest = PersonManagerSetup.createPersonManagerSetup(innerTest);
		String customFile = CustomPropertiesDecorator.createFileName(TestAgentTokenMigration.class);
		innerTest = TestUtils.doNotMerge(new CustomPropertiesSetup(innerTest, customFile, true));
		return TLTestSetup.createTLTestSetup(innerTest);
	}

}

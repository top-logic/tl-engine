/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.access;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.Create;
import com.top_logic.element.model.diff.config.Delete;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLModel;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.Type;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test of the migration script {@link #MIGRATION}, which moves the personal access tokens of the
 * module <code>tl.ai.access</code> to the type <code>tl.accounts:AccessToken</code>.
 *
 * <p>
 * The test creates a token, converts the database and the stored model baseline to the state
 * before the migration (the types in the module <code>tl.ai.access</code>, no columns for the
 * permissions and the session), runs the processors of the migration script, and checks the
 * result in the database and against the automatic model upgrade that follows the migration on
 * startup.
 * </p>
 */
public class TestAccessTokenMigration extends BasicTestCase {

	private static final String MIGRATION_MODULE = "tl-ai-core";

	private static final String MIGRATION = "Ticket_30367_access_tokens_to_tl_accounts";

	private static final String OLD_MODULE = "tl.ai.access";

	/**
	 * Converts the current state to the one before the migration: the types back into
	 * <code>tl.ai.access</code>, no permissions and session key in the model or the table.
	 */
	private static final String TO_OLD_STATE = """
		<migration config:interface="com.top_logic.knowledge.service.migration.MigrationConfig"
			xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
			<version name="TestAccessTokenMigration_old_state" module="tl-ai-core"/>
			<processors>
				<create-module name="tl.ai.access"/>
				<update-class name="tl.accounts:AccessToken" new-name="tl.ai.access:AccessToken"/>
				<update-class name="tl.accounts:AccessTokenRequest" new-name="tl.ai.access:AccessTokenRequest"/>
				<delete-property name="tl.ai.access:AccessToken#permissions" type-tables="PersonalAccessToken"/>
				<delete-property name="tl.ai.access:AccessToken#sessionKey" type-tables="PersonalAccessToken"/>
				<drop-column table="PersonalAccessToken" column="permissions"/>
				<drop-column table="PersonalAccessToken" column="sessionKey"/>
			</processors>
		</migration>
		""";

	/**
	 * A token of the agent endpoints keeps its hash and is the only one of the single type
	 * <code>tl.accounts:AccessToken</code>, with the permission
	 * {@link AccessTokenAuthenticator#AGENT_ENDPOINTS_PERMISSION}.
	 */
	public void testMigration() throws Exception {
		AccessTokens.Created created = AccessTokens.create(PersonManager.getManager().getRoot(), "migrated", null,
			List.of(AccessTokenAuthenticator.AGENT_ENDPOINTS_PERMISSION), null);
		String hash = AccessTokens.hash(created.plain());
		long typeId = ((LongID) AccessTokens.accessTokenType().tId().getObjectName()).longValue();
		try {
			run(oldState());
			assertNull("Not in the old state.", module(AccessToken.MODULE).stream()
				.filter(type -> type.getTypeName().equals(AccessToken.ACCESS_TOKEN_TYPE)).findAny().orElse(null));

			BufferingProtocol readLog = new BufferingProtocol();
			MigrationConfig migration =
				MigrationUtil.readMigration(readLog, MIGRATION_MODULE, MIGRATION + MigrationUtil.MIGRATION_FILE_SUFFIX);
			assertFalse("Migration script not readable: " + readLog.getErrors(), readLog.hasErrors());
			run(migration);

			List<Type> tokenTypes = module(AccessToken.MODULE).stream()
				.filter(type -> type.getTypeName().equals(AccessToken.ACCESS_TOKEN_TYPE)).toList();
			assertEquals("Exactly one type " + AccessToken.MODULE + ":" + AccessToken.ACCESS_TOKEN_TYPE + ".", 1, tokenTypes.size());
			assertEquals("The type is moved, not recreated.", typeId,
				((LongID) tokenTypes.get(0).getID()).longValue());
			assertNull("The module " + OLD_MODULE + " is deleted.", moduleOrNull(OLD_MODULE));

			assertEquals(List.of(List.of(String.valueOf(typeId), AccessTokenAuthenticator.AGENT_ENDPOINTS_PERMISSION)),
				tokenRows(hash));

			assertNoTypeCreatedOrDeletedByModelUpgrade();

			assertEquals("The token is found by its hash.", created.token(), AccessTokens.find(created.plain()));
		} finally {
			AccessTokens.revoke(created.token());
		}
	}

	private static MigrationConfig oldState() throws Exception {
		return (MigrationConfig) TypedConfiguration.parse(CharacterContents.newContent(TO_OLD_STATE));
	}

	/**
	 * Runs the processors of the given migration as the migration does, and stores the resulting
	 * schema.
	 */
	private static void run(MigrationConfig migration) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
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
		assertFalse(migration.getVersion() + " reported errors: " + log.getErrors(), log.hasErrors());
	}

	/**
	 * The automatic model upgrade on startup compares the stored baseline with the configured
	 * model: it must neither create nor delete a type or attribute of the token type.
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

		BufferingProtocol log = new BufferingProtocol();
		TLModel stored = DynamicModelService.loadTransientModel(log, baseline);
		TLModel configured =
			DynamicModelService.loadTransientModel(log, DynamicModelService.getInstance().getModelConfig());
		assertFalse("Models not loadable: " + log.getErrors(), log.hasErrors());

		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(stored, configured);
		List<String> tokenChanges = new ArrayList<>();
		for (DiffElement element : patchCreator.getPatch()) {
			String description = TypedConfiguration.toString(element);
			if ((element instanceof Create || element instanceof Delete)
				&& (description.contains(AccessToken.ACCESS_TOKEN_TYPE) || description.contains(OLD_MODULE))) {
				tokenChanges.add(description);
			}
		}
		assertEquals("The model upgrade creates or deletes token types or attributes.", List.of(), tokenChanges);
	}

	/**
	 * Type ID and permissions of the rows of the token table with the given hash.
	 */
	private static List<List<String>> tokenRows(String hash) throws SQLException {
		MOClass table = (MOClass) PersistencyLayer.getKnowledgeBase().getMORepository().getMetaObject(AccessToken.TABLE);
		PooledConnection connection = pool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String query = "SELECT " + sql.columnRef(Util.refID(PersistentObject.TYPE_REF)) + ", "
				+ sql.columnRef(column(table, AccessToken.PERMISSIONS_ATTR))
				+ " FROM " + sql.tableRef(table.getDBMapping().getDBName())
				+ " WHERE " + sql.columnRef(column(table, AccessToken.SECRET_HASH_ATTR)) + " = ?";
			List<List<String>> result = new ArrayList<>();
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				statement.setString(1, hash);
				try (ResultSet rows = statement.executeQuery()) {
					while (rows.next()) {
						result.add(List.of(String.valueOf(rows.getLong(1)), rows.getString(2)));
					}
				}
			}
			return result;
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	private static String column(MOClass table, String attribute) {
		MOAttribute moAttribute = table.getAttributeOrNull(attribute);
		assertNotNull("No column " + attribute, moAttribute);
		return moAttribute.getDbMapping()[0].getDBName();
	}

	private static List<Type> module(String name) throws Exception {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			Util util = new MigrationContext(new BufferingProtocol(), connection).getSQLUtils();
			return util.getTLTypeIdentifiers(connection, util.getTLModuleOrFail(connection, name));
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	private static Module moduleOrNull(String name) throws Exception {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			Util util = new MigrationContext(new BufferingProtocol(), connection).getSQLUtils();
			return util.getTLModule(connection, TLContext.TRUNK_ID, name);
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	private static ConnectionPool pool() {
		return KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAccessTokenMigration.class),
				ModelService.Module.INSTANCE,
				AccessTokenPermissions.Module.INSTANCE));
	}

}

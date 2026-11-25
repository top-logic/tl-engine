/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.model.migration.model.MigrationUtils;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

/**
 * Test of the migration <code>Ticket_30558_remove_agent_access_tokens</code> of the migration
 * module <code>tl-element</code>, which deletes the model module <code>tl.agent</code> with the
 * access tokens stored in the table <code>GenericObject</code>.
 */
@SuppressWarnings("javadoc")
public class TestRemoveAgentAccessTokens extends AbstractLinkTableMigrationTest {

	private static final String MIGRATION_MODULE = "tl-element";

	private static final String MIGRATION_FILE =
		"Ticket_30558_remove_agent_access_tokens" + MigrationUtil.MIGRATION_FILE_SUFFIX;

	private static final String MODEL_MODULE = "tl.agent";

	private static final String TOKEN_CLASS = "AccessToken";

	private static final String TOKEN_PROPERTY = "description";

	private static final String OBJECT_TABLE = "GenericObject";

	private static final long TRUNK = TLContext.TRUNK_ID;

	public void testDeletesModuleWithTokens() throws Exception {
		long token = FIRST_ID + 1;
		long other = FIRST_ID + 2;
		long otherType = FIRST_ID + 500;

		Type[] tokenType = { null };
		TypePart[] tokenProperty = { null };
		migrate(noProcessor(), (context, log, connection) -> {
			Util util = context.getSQLUtils();
			util.createTLModule(connection, TRUNK, MODEL_MODULE, null);
			tokenType[0] = util.createTLStructuredType(connection, TRUNK, MODEL_MODULE, TOKEN_CLASS,
				Boolean.FALSE, Boolean.FALSE, null, false);
			tokenProperty[0] = util.createTLProperty(log, connection, TRUNK, MODEL_MODULE, TOKEN_CLASS,
				TOKEN_PROPERTY, "tl.core", "String", false, false, false, false, false, null);
		});
		assertTrue(hasModule());
		long tokenTypeId = id(tokenType[0].getID());
		long tokenPropertyId = id(tokenProperty[0].getID());

		insertObject(OBJECT_TABLE, token, tokenTypeId);
		insertObject(OBJECT_TABLE, other, otherType);
		insertFlexData(OBJECT_TABLE, token);
		insertFlexData(OBJECT_TABLE, other);
		String metaElement = ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE;
		String links = ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION;
		insertLink(links, FIRST_ID + 10, 1, MAX, OBJECT_TABLE, token, tokenPropertyId, OBJECT_TABLE, other, 0);
		insertLink(links, FIRST_ID + 11, 1, MAX, OBJECT_TABLE, other, otherType, OBJECT_TABLE, token, 0);
		insertLink(links, FIRST_ID + 12, 1, MAX, OBJECT_TABLE, other, otherType, metaElement, 7, 0);

		String baseline = readBaseline();
		assertNotNull("No model baseline stored.", baseline);
		try {
			storeBaseline(withModule(baseline, MODEL_MODULE));

			BufferingProtocol log = migrate(migrationProcessors(), TestRemoveAgentAccessTokens::revalidate);

			assertFalse("Model module not deleted.", hasModule());
			assertFalse("Class not deleted.", exists(tokenType[0].getTable(), tokenTypeId));
			assertFalse("Property not deleted.", exists(tokenProperty[0].getTable(), tokenPropertyId));
			assertEquals(List.of(row(other, 1, MAX)), rows(OBJECT_TABLE));
			assertEquals(List.of(row(other, 1, MAX)), rows(AbstractFlexDataManager.FLEX_DATA));
			assertEquals(List.of(row(FIRST_ID + 12, 1, MAX)), rows(links));
			assertInfo(log, "Deleted 1 instances of types of module '" + MODEL_MODULE + "' from table "
				+ OBJECT_TABLE + ".");

			Document migratedBaseline = DOMUtil.parse(readBaseline());
			try {
				MigrationUtils.getTLModuleOrFail(migratedBaseline, MODEL_MODULE);
				fail("Module '" + MODEL_MODULE + "' not deleted from the model baseline.");
			} catch (MigrationException ex) {
				// Expected.
			}
		} finally {
			storeBaseline(baseline);
		}
	}

	public void testWithoutModule() throws Exception {
		long other = FIRST_ID + 2;
		long otherType = FIRST_ID + 500;
		insertObject(OBJECT_TABLE, other, otherType);
		insertFlexData(OBJECT_TABLE, other);
		String baseline = readBaseline();
		assertFalse(hasModule());

		BufferingProtocol log = migrate(migrationProcessors(), TestRemoveAgentAccessTokens::revalidate);

		assertInfo(log, "No module with name '" + MODEL_MODULE + "' to delete");
		assertEquals(List.of(row(other, 1, MAX)), rows(OBJECT_TABLE));
		assertEquals(List.of(row(other, 1, MAX)), rows(AbstractFlexDataManager.FLEX_DATA));
		assertEquals(baseline, readBaseline());
	}

	/**
	 * The processors of the migration file of the test, all executed one after another.
	 */
	private static MigrationProcessor migrationProcessors() {
		BufferingProtocol log = new BufferingProtocol();
		MigrationConfig migration = MigrationUtil.readMigration(log, MIGRATION_MODULE, MIGRATION_FILE);
		assertNotNull("Migration '" + MIGRATION_FILE + "' of module '" + MIGRATION_MODULE + "' not found.",
			migration);
		assertFalse("Reading the migration failed: " + log.getErrors(), log.hasErrors());
		return processors(migration);
	}

	private static void revalidate(MigrationContext context, BufferingProtocol log, PooledConnection connection)
			throws SQLException {
		context.revalidateXRef(log, connection);
	}

	private boolean hasModule() throws Exception {
		boolean[] result = { false };
		migrate(noProcessor(), (context, log, connection) -> {
			result[0] = context.getSQLUtils().getTLModule(connection, TRUNK, MODEL_MODULE) != null;
		});
		return result[0];
	}

	private static boolean exists(String typeName, long id) throws SQLException {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			String sql = "SELECT " + sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " FROM "
				+ sqlDialect.tableRef(table(typeName).getDBMapping().getDBName()) + " WHERE "
				+ sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME) + " = ?";
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setLong(1, id);
				try (ResultSet result = statement.executeQuery()) {
					return result.next();
				}
			}
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	private static String withModule(String baseline, String moduleName) {
		Document document = DOMUtil.parse(baseline);
		Element module = document.createElement(ModelConfig.MODULE);
		module.setAttribute("name", moduleName);
		document.getDocumentElement().appendChild(module);
		return DOMUtil.toString(document);
	}

	private static long id(TLID id) {
		return ((LongID) id).longValue();
	}

	public static Test suite() {
		return suite(TestRemoveAgentAccessTokens.class);
	}

}

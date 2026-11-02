/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;
import static java.util.Arrays.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.IndexConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.element.model.migration.model.refactor.RemoveDuplicatesProcessor;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.processors.AlignIndexesProcessor;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of the migration script {@link #MIGRATION} on a database in the layout before the
 * migration: The link tables hold each value of a reference more than once and there are no tables
 * for the links of bag references.
 *
 * <p>
 * The test converts the link tables of the knowledge base to the layout before the migration (no
 * bag tables, the index <code>source</code> is not unique) and runs the processors of the
 * migration script.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestUniqueLinkTablesMigration extends AbstractLinkTableMigrationTest {

	private static final Class<TestUniqueLinkTablesMigration> THIS_CLASS = TestUniqueLinkTablesMigration.class;

	private static final String MIGRATION_MODULE = "tl-element";

	private static final String MIGRATION = "Ticket_30031_unique_link_tables";

	private static final String BAG_REFERENCE = "TestLinkTableUniqueness:Parent#bag";

	private static final String SET_REFERENCE = "TestLinkTableUniqueness:Parent#set";

	private static final String HISTORIC_REFERENCE = "TestLinkTableUniqueness:Parent#historic";

	/**
	 * Name of the index that is unique after the migration.
	 */
	private static final String SOURCE_INDEX = DBKnowledgeAssociation.REFERENCE_SOURCE_NAME;

	/**
	 * The types with the unique index {@link #SOURCE_INDEX}, from which all other link tables of
	 * references inherit it.
	 */
	private static final List<String> UNIQUE_BASE_TYPES = List.of(
		WRAPPER_ATTRIBUTE_ASSOCIATION_BASE,
		HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION,
		MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION);

	/**
	 * The concrete bag tables together with the tables holding each value at most once, in which
	 * their links were stored before the migration.
	 */
	private static final Map<String, String> BAG_TABLES = Map.of(
		WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, WRAPPER_ATTRIBUTE_ASSOCIATION,
		WRAPPER_DELETE_REFERER_BAG_ASSOCIATION, WRAPPER_DELETE_REFERER_ASSOCIATION,
		WRAPPER_VETO_BAG_ASSOCIATION, WRAPPER_VETO_ASSOCIATION,
		HISTORIC_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION,
		MIXED_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION,
		WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED, WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED,
		WRAPPER_DELETE_REFERER_BAG_ASSOCIATION_UNVERSIONED, WRAPPER_DELETE_REFERER_ASSOCIATION_UNVERSIONED,
		WRAPPER_VETO_BAG_ASSOCIATION_UNVERSIONED, WRAPPER_VETO_ASSOCIATION_UNVERSIONED,
		HISTORIC_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED, HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED,
		MIXED_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_UNVERSIONED, MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION_UNVERSIONED);

	/**
	 * All types of the bag tables, including their abstract base.
	 */
	private static final List<String> BAG_TYPES;
	static {
		List<String> bagTypes = new ArrayList<>(BAG_TABLES.keySet());
		bagTypes.add(WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_BASE);
		BAG_TYPES = bagTypes;
	}

	private static final long OWNER = 1;

	private static final long VALUE = 100;

	private static final long OTHER_VALUE = 101;

	private static final long VALUE_REV = 5;

	private static final long OTHER_VALUE_REV = 7;

	/**
	 * Bag links are moved to the bag tables with their duplicates, duplicates of other references
	 * are removed over the history, and the index {@link #SOURCE_INDEX} becomes unique.
	 */
	public void testMigration() throws Exception {
		long bag = attribute(BAG_REFERENCE);
		long set = attribute(SET_REFERENCE);
		long historic = attribute(HISTORIC_REFERENCE);

		convertToOldLayout();

		String links = WRAPPER_ATTRIBUTE_ASSOCIATION;
		insertLink(links, FIRST_ID + 1, 1, MAX, OWNER, bag, VALUE, 0);
		// A duplicate in a bag is a value.
		insertLink(links, FIRST_ID + 2, 1, MAX, OWNER, bag, VALUE, 0);
		insertLink(links, FIRST_ID + 3, 2, MAX, OWNER, bag, OTHER_VALUE, 0);

		insertLink(links, FIRST_ID + 11, 1, 10, OWNER, set, VALUE, 0);
		// Starts during link 11: Cut to start after link 11.
		insertLink(links, FIRST_ID + 12, 5, MAX, OWNER, set, VALUE, 0);
		// Covered completely by link 11.
		insertLink(links, FIRST_ID + 13, 3, 4, OWNER, set, VALUE, 0);
		insertLink(links, FIRST_ID + 14, 1, MAX, OWNER, set, OTHER_VALUE, 0);

		String historicLinks = HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION;
		// The current revision of the value.
		insertLink(historicLinks, FIRST_ID + 21, 1, MAX, OWNER, historic, VALUE, MAX);
		// The same value in a specific revision.
		insertLink(historicLinks, FIRST_ID + 22, 1, MAX, OWNER, historic, VALUE, VALUE_REV);
		// Duplicate of link 22.
		insertLink(historicLinks, FIRST_ID + 23, 2, MAX, OWNER, historic, VALUE, VALUE_REV);

		BufferingProtocol log = runMigration();

		String bagLinks = WRAPPER_ATTRIBUTE_BAG_ASSOCIATION;
		assertEquals(List.of(
			row(FIRST_ID + 1, 1, MAX),
			row(FIRST_ID + 2, 1, MAX),
			row(FIRST_ID + 3, 2, MAX)), rows(bagLinks));
		assertEquals(List.of(
			row(FIRST_ID + 11, 1, 10),
			row(FIRST_ID + 12, 11, MAX),
			row(FIRST_ID + 14, 1, MAX)), rows(links));
		assertEquals(List.of(
			row(FIRST_ID + 21, 1, MAX),
			row(FIRST_ID + 22, 1, MAX)), rows(historicLinks));

		assertInfo(log, "Moved 3 links of '" + BAG_REFERENCE + "' from '" + links + "' to '" + bagLinks + "'.");
		assertInfo(log, "Deleted 1 assignments and adjusted 1 assignments in '" + links + "'.");
		assertInfo(log, "Deleted 1 assignments and adjusted 0 assignments in '" + historicLinks + "'.");
		assertInfo(log, "Changed index '" + SOURCE_INDEX.toUpperCase());
		// A table without duplicates is not read row by row.
		assertInfo(log, "No duplicate assignments in '" + WRAPPER_VETO_ASSOCIATION + "', table not scanned.");
		assertInfo(log, "Found 1 owners with duplicate assignments in '" + links + "'.");

		// The unique index rejects duplicates of references that are no bags.
		assertRejected(links, FIRST_ID + 15, OWNER, set, OTHER_VALUE, 0);
		assertRejected(historicLinks, FIRST_ID + 24, OWNER, historic, VALUE, VALUE_REV);
		// Another revision of the same value is another value.
		insertLink(historicLinks, FIRST_ID + 25, 1, MAX, OWNER, historic, VALUE, OTHER_VALUE_REV);
		// The bag table accepts duplicates.
		insertLink(bagLinks, FIRST_ID + 4, 1, MAX, OWNER, bag, VALUE, 0);
		assertEquals(4, rows(bagLinks).size());
	}

	/**
	 * Links of a bag reference in a table holding each value at most once are reported and not
	 * touched.
	 */
	public void testSkipBags() throws Exception {
		long bag = attribute(BAG_REFERENCE);

		String links = STRUCTURE_CHILD_ASSOCIATION;
		insertLink(links, FIRST_ID + 1, 1, MAX, OWNER, bag, VALUE, 0);

		RemoveDuplicatesProcessor.Config<?> config =
			TypedConfiguration.newConfigItem(RemoveDuplicatesProcessor.Config.class);
		config.setTables(asList(links));
		config.setSkipBags(true);

		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool().borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection);
			TypedConfigUtil.createInstance(config).doMigration(context, log, connection);
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}

		assertEquals(List.of(row(FIRST_ID + 1, 1, MAX)), rows(links));
		assertTrue("Bag links not reported: " + log.getErrors(),
			log.getErrors().stream().anyMatch(error -> error.contains("'" + links + "' holds 1 links of the bag reference '"
				+ BAG_REFERENCE + "'")));
	}

	private static long attribute(String qualifiedName) {
		TLStructuredTypePart part = (TLStructuredTypePart) TLModelUtil.findPart(qualifiedName);
		return ((LongID) part.tId().getObjectName()).longValue();
	}

	/**
	 * Converts the link tables to the layout before the migration.
	 *
	 * <p>
	 * The index {@link #SOURCE_INDEX} of the link tables is converted back to the not unique
	 * definition, the rows of the bag tables are moved to the tables they were stored in, and the
	 * bag tables are dropped. The stored schema is the application schema without the bag tables
	 * and with the not unique index.
	 * </p>
	 */
	private void convertToOldLayout() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool().borrowWriteConnection();
		try {
			SchemaConfiguration current = new MigrationContext(log, connection).getApplicationSchema();
			SchemaConfiguration old = oldSchema(current);
			storeSchema(connection, current);

			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return old;
				}
			};
			AlignIndexesProcessor.Config config = TypedConfiguration.newConfigItem(AlignIndexesProcessor.Config.class);
			config.setSubtypesOf(UNIQUE_BASE_TYPES);
			TypedConfigUtil.createInstance(config).doMigration(context, log, connection);

			DBHelper sqlDialect = connection.getSQLDialect();
			try (Statement statement = connection.createStatement()) {
				for (Map.Entry<String, String> entry : BAG_TABLES.entrySet()) {
					MOClass bagTable = table(entry.getKey());
					MOClass table = table(entry.getValue());
					List<String> columns = new ArrayList<>();
					for (MOAttribute attribute : bagTable.getAttributes()) {
						for (DBAttribute column : attribute.getDbMapping()) {
							columns.add(sqlDialect.columnRef(column.getDBName()));
						}
					}
					String columnList = String.join(", ", columns);
					String bagTableRef = sqlDialect.tableRef(bagTable.getDBMapping().getDBName());
					statement.executeUpdate("INSERT INTO " + sqlDialect.tableRef(table.getDBMapping().getDBName())
						+ " (" + columnList + ") SELECT " + columnList + " FROM " + bagTableRef);
					statement.executeUpdate("DROP TABLE " + bagTableRef);
				}
			}

			storeSchema(connection, old);
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
		assertFalse("Conversion to the old layout failed: " + log.getErrors(), log.hasErrors());
		assertInfo(log, "Changed index '" + SOURCE_INDEX.toUpperCase());
	}

	/**
	 * The given schema without the bag tables and with the index {@link #SOURCE_INDEX} over source
	 * and attribute that is not unique.
	 */
	private static SchemaConfiguration oldSchema(SchemaConfiguration current) {
		SchemaConfiguration result = TypedConfiguration.copy(current);
		Map<String, MetaObjectName> types = result.getMetaObjects().getTypes();
		for (String bagType : BAG_TYPES) {
			assertNotNull("No type '" + bagType + "'.", types.remove(bagType));
		}
		for (MetaObjectName type : types.values()) {
			if (type instanceof MetaObjectConfig config
				&& WRAPPER_ATTRIBUTE_BAG_ASSOCIATION_BASE.equals(config.getSuperClass())) {
				// A table that is a bag table after the migration.
				config.update(config.descriptor().getProperty(MetaObjectConfig.SUPERCLASS_PROPERTY),
					WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
			}
		}
		for (String typeName : UNIQUE_BASE_TYPES) {
			MetaObjectConfig type = (MetaObjectConfig) types.get(typeName);
			IndexConfig index = type.getIndex().stream()
				.filter(candidate -> candidate.getName().equals(SOURCE_INDEX))
				.findFirst()
				.orElseThrow();
			index.update(index.descriptor().getProperty(DOXMLConstants.UNIQUE_ATTRIBUTE), Boolean.FALSE);
			assertTrue(index.getIndexParts()
				.removeIf(part -> part.getName().equals(DBKnowledgeAssociation.REFERENCE_DEST_NAME)));
		}
		return result;
	}

	private static void storeSchema(PooledConnection connection, SchemaConfiguration schema) throws SQLException {
		KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, schema);
	}

	/**
	 * Runs the processors of the migration script {@link #MIGRATION} as the migration does, and
	 * stores the application schema afterwards.
	 */
	private BufferingProtocol runMigration() throws Exception {
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
			context.revalidateXRef(log, connection);
			storeSchema(connection, context.getApplicationSchema());
			connection.commit();
		} finally {
			pool().releaseWriteConnection(connection);
		}
		assertFalse("Migration reported errors: " + log.getErrors(), log.hasErrors());
		return log;
	}

	/**
	 * Asserts that the given link can not be inserted, since the table already holds it.
	 */
	private void assertRejected(String typeName, long id, long source, long attribute, long dest, long valueRev) {
		try {
			insertLink(typeName, id, 1, MAX, source, attribute, dest, valueRev);
			fail("Duplicate link accepted by '" + typeName + "'.");
		} catch (SQLException ex) {
			// Expected: The unique index rejects the duplicate.
		}
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		String configFileName = THIS_CLASS.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(THIS_CLASS, configFileName);
		Test customConfigSetup = TestUtils.doNotMerge(new CustomPropertiesSetup(kbSetup, createFilePath, true));
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

}

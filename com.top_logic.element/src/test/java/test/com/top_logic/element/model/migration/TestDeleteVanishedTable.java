/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.migration.model.DeleteTLClassProcessor;
import com.top_logic.element.model.migration.model.DeleteTLModuleProcessor;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.processors.IfTablesUndeclaredProcessor;
import com.top_logic.model.migration.Util;

/**
 * Test of a migration deleting a class, its table and its module with
 * {@link DeleteTLClassProcessor}, {@link DeleteTableProcessor} and {@link DeleteTLModuleProcessor}
 * inside an {@link IfTablesUndeclaredProcessor}, on a database whose stored schema does not know
 * the table.
 */
@SuppressWarnings("javadoc")
public class TestDeleteVanishedTable extends AbstractLinkTableMigrationTest {

	private static final String MODULE_NAME = "test.vanished";

	private static final String CLASS_NAME = "Vanished";

	private static final String TABLE_NAME = "VanishedTable";

	private static final String OBJECT_TABLE = "GenericObject";

	private static final long TRUNK = 1L;

	private static final String MIGRATION_PROCESSORS = "<if-tables-undeclared>"
		+ "<tables><table object_name=\"" + TABLE_NAME + "\"/></tables>"
		+ "<processors>"
		+ "<delete-class name=\"" + MODULE_NAME + ":" + CLASS_NAME + "\" type-table=\"" + TABLE_NAME + "\"/>"
		+ "<delete-table><table object_name=\"" + TABLE_NAME + "\"/></delete-table>"
		+ "<delete-module name=\"" + MODULE_NAME + "\" fail-on-existing-types=\"true\"/>"
		+ "</processors>"
		+ "</if-tables-undeclared>";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertNull(types().getTypeOrNull(TABLE_NAME));
	}

	@Override
	protected void tearDown() throws Exception {
		if (moduleExists()) {
			// Leftover of a failed test.
			migrate((context, log, connection) -> runMigration(context, log, connection,
				"<delete-module name=\"" + MODULE_NAME + "\"/>"), null);
		}
		super.tearDown();
	}

	/**
	 * Class and module exist: both are deleted together with the values left behind by the objects
	 * of the vanished table.
	 */
	public void testDeletesClassAndModule() throws Exception {
		migrate((context, log, connection) -> {
			// Nothing to migrate, the model elements are created afterwards.
		}, (context, log, connection) -> {
			Util util = context.getSQLUtils();
			util.createTLModule(connection, TRUNK, MODULE_NAME, null);
			util.createTLStructuredType(connection, TRUNK, MODULE_NAME, CLASS_NAME, false, false, null, false);
		});
		assertTrue(moduleExists());

		long orphan = FIRST_ID + 1;
		long other = FIRST_ID + 2;
		long attribute = FIRST_ID + 600;
		insertFlexData(TABLE_NAME, orphan);
		insertFlexData(OBJECT_TABLE, other);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 10, 1, MAX, TABLE_NAME, orphan, attribute,
			OBJECT_TABLE, other, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 11, 1, MAX, OBJECT_TABLE, other, attribute,
			OBJECT_TABLE, orphan, 0);

		BufferingProtocol log = migrate(TestDeleteVanishedTable::runMigration, TestDeleteVanishedTable::revalidate);

		assertFalse("Module not deleted.", moduleExists());
		assertInfo(log, "Deleted type ");
		assertInfo(log, "No table '" + TABLE_NAME + "' in the stored schema, deleted 1 flex attribute values"
			+ " and 1 links of its objects.");
		assertEquals(List.of(row(other, 1, MAX)), rows(AbstractFlexDataManager.FLEX_DATA));
		assertEquals(List.of(row(FIRST_ID + 11, 1, MAX)), rows(WRAPPER_ATTRIBUTE_ASSOCIATION));
	}

	/**
	 * Neither class nor module exist: the migration runs without error and deletes nothing.
	 */
	public void testWithoutModule() throws Exception {
		assertFalse(moduleExists());
		long other = FIRST_ID + 2;
		insertFlexData(OBJECT_TABLE, other);

		BufferingProtocol log = migrate(TestDeleteVanishedTable::runMigration, TestDeleteVanishedTable::revalidate);

		assertFalse(moduleExists());
		assertInfo(log, "No class with name '" + MODULE_NAME + ":" + CLASS_NAME + "' to delete");
		assertInfo(log, "No module with name '" + MODULE_NAME + "' to delete");
		assertEquals(List.of(row(other, 1, MAX)), rows(AbstractFlexDataManager.FLEX_DATA));
	}

	private static void runMigration(MigrationContext context, Log log, PooledConnection connection) {
		runMigration(context, log, connection, MIGRATION_PROCESSORS);
	}

	private static void runMigration(MigrationContext context, Log log, PooledConnection connection,
			String processorXML) {
		String migration = "<" + MigrationUtil.ROOT_TAG_NAME + ">"
			+ "<version name=\"test\" module=\"test\"/>"
			+ "<processors>" + processorXML + "</processors>"
			+ "</" + MigrationUtil.ROOT_TAG_NAME + ">";
		try {
			for (MigrationProcessor processor : processors(migration)) {
				processor.doMigration(context, log, connection);
			}
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static List<MigrationProcessor> processors(String migrationXML) throws ConfigurationException {
		InstantiationContext context = new DefaultInstantiationContext(TestDeleteVanishedTable.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		MigrationConfig migration = (MigrationConfig) new ConfigurationReader(context,
			Collections.singletonMap(MigrationUtil.ROOT_TAG_NAME, descriptor))
				.setSource(CharacterContents.newContent(migrationXML)).read();
		context.checkErrors();

		List<MigrationProcessor> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends MigrationProcessor> config : migration.getProcessors()) {
			result.add(context.getInstance(config));
		}
		context.checkErrors();
		return result;
	}

	private static void revalidate(MigrationContext context, BufferingProtocol log, PooledConnection connection)
			throws SQLException {
		// Rebuilds the revision index of the tables the cleanup invalidated.
		context.revalidateXRef(log, connection);
	}

	private boolean moduleExists() throws Exception {
		PooledConnection connection = pool().borrowReadConnection();
		try {
			MigrationContext context = new MigrationContext(new BufferingProtocol(), connection);
			return context.getSQLUtils().getTLModule(connection, TRUNK, MODULE_NAME) != null;
		} finally {
			pool().releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		return suite(TestDeleteVanishedTable.class);
	}

}

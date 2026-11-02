/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sql.DB2Helper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.Oracle12Helper;
import com.top_logic.basic.sql.PostgreSQLHelper;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;

/**
 * Test of the effective value of {@link SchemaConfiguration#getBranchLast()} for a database.
 *
 * @see SchemaSetup#effectiveBranchLast(SchemaConfiguration, DBHelper)
 * @see SchemaSetup#withExplicitLayout(SchemaConfiguration, DBHelper)
 * @see KBSchemaUtil#parseSchema(String)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchLastResolution extends TestCase {

	/**
	 * Without a configured value, only H2 places the branch column last.
	 */
	public void testDialectDefault() {
		SchemaConfiguration schema = schema(null);
		assertTrue(SchemaSetup.effectiveBranchLast(schema, h2()));
		for (DBHelper dialect : otherDialects()) {
			assertFalse(dialect.getClass().getName(), SchemaSetup.effectiveBranchLast(schema, dialect));
		}
	}

	/**
	 * A configured value overrides the default of the dialect.
	 */
	public void testExplicitValue() {
		assertFalse(SchemaSetup.effectiveBranchLast(schema(Boolean.FALSE), h2()));
		for (DBHelper dialect : otherDialects()) {
			assertTrue(dialect.getClass().getName(), SchemaSetup.effectiveBranchLast(schema(Boolean.TRUE), dialect));
		}
	}

	/**
	 * The schema stored in the database states the effective value explicitly.
	 */
	public void testExplicitLayout() throws ConfigurationException {
		SchemaConfiguration unset = schema(null);
		SchemaConfiguration forH2 = SchemaSetup.withExplicitLayout(unset, h2());
		assertEquals(Boolean.TRUE, forH2.getBranchLast());
		assertNull("Configuration not modified.", unset.getBranchLast());
		assertEquals(Boolean.TRUE, storedAndLoaded(forH2).getBranchLast());

		SchemaConfiguration forPostgres =
			SchemaSetup.withExplicitLayout(unset, DBHelper.createDefaultInstance(PostgreSQLHelper.class));
		assertEquals(Boolean.FALSE, forPostgres.getBranchLast());
		assertEquals(Boolean.FALSE, storedAndLoaded(forPostgres).getBranchLast());

		SchemaConfiguration explicit = schema(Boolean.FALSE);
		assertSame(explicit, SchemaSetup.withExplicitLayout(explicit, h2()));
	}

	/**
	 * A stored schema without a value describes tables with the branch column in front, no matter
	 * which database it is read from.
	 */
	public void testStoredWithoutValue() throws ConfigurationException {
		SchemaConfiguration loaded = storedAndLoaded(schema(null));
		assertEquals(Boolean.valueOf(SchemaConfiguration.DEFAULT_BRANCH_LAST_ATTRIBUTE), loaded.getBranchLast());
		assertFalse(SchemaSetup.effectiveBranchLast(loaded, h2()));
	}

	private static SchemaConfiguration storedAndLoaded(SchemaConfiguration schema) throws ConfigurationException {
		return KBSchemaUtil.parseSchema(KBSchemaUtil.serializeSchema(schema));
	}

	private static SchemaConfiguration schema(Boolean branchLast) {
		SchemaConfiguration result = TypedConfiguration.newConfigItem(SchemaConfiguration.class);
		result.setBranchLast(branchLast);
		return result;
	}

	private static DBHelper h2() {
		return DBHelper.createDefaultInstance(H2Helper.class);
	}

	private static List<DBHelper> otherDialects() {
		return List.of(
			DBHelper.createDefaultInstance(DBHelper.class),
			DBHelper.createDefaultInstance(Oracle12Helper.class),
			DBHelper.createDefaultInstance(PostgreSQLHelper.class),
			DBHelper.createDefaultInstance(MySQLHelper.class),
			DBHelper.createDefaultInstance(MSSQLHelper.class),
			DBHelper.createDefaultInstance(DB2Helper.class));
	}

	/**
	 * Suite of {@link TestBranchLastResolution}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestBranchLastResolution.class, TypeIndex.Module.INSTANCE));
	}

}

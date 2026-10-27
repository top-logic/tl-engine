/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.journal.FlexJournalManager;
import com.top_logic.knowledge.journal.persistancy.FlexDataChangeEntryJournal;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.BasicTypeProvider;

/**
 * Test of the primary key of the table created for {@link AbstractFlexDataManager#FLEX_DATA}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlexDataPrimaryKey extends TestCase {

	/**
	 * The primary key of the flex data table with multiple branches starts with the branch,
	 * followed by identifier, attribute and revision.
	 */
	public void testFlexDataMultipleBranches() {
		DBTable table = flexDataTable(true);

		assertEquals(
			Arrays.asList(AbstractFlexDataManager.BRANCH_DBNAME, AbstractFlexDataManager.IDENTIFIER_DBNAME,
				AbstractFlexDataManager.ATTRIBUTE_DBNAME, BasicTypes.REV_MAX_DB_NAME),
			primaryKeyColumns(table));
		assertEquals("Branch and identifier are compressed.", 2, table.getCompress());
		assertNotNull("Type column is kept as data column.",
			table.getColumn(AbstractFlexDataManager.TYPE_DBNAME));
	}

	/**
	 * The primary key of the flex data table without branches starts with the identifier,
	 * followed by attribute and revision.
	 */
	public void testFlexDataSingleBranch() {
		DBTable table = flexDataTable(false);

		assertEquals(
			Arrays.asList(AbstractFlexDataManager.IDENTIFIER_DBNAME, AbstractFlexDataManager.ATTRIBUTE_DBNAME,
				BasicTypes.REV_MAX_DB_NAME),
			primaryKeyColumns(table));
		assertEquals("Identifier is compressed.", 1, table.getCompress());
		assertNotNull("Type column is kept as data column.",
			table.getColumn(AbstractFlexDataManager.TYPE_DBNAME));
	}

	/**
	 * The journal tables keep the type in their primary key.
	 */
	public void testJournalMultipleBranches() {
		DBTable table = journalTable(true);

		assertEquals(
			Arrays.asList(AbstractFlexDataManager.BRANCH_DBNAME, AbstractFlexDataManager.TYPE_DBNAME,
				AbstractFlexDataManager.IDENTIFIER_DBNAME, BasicTypes.REV_MAX_DB_NAME,
				AbstractFlexDataManager.ATTRIBUTE_DBNAME),
			primaryKeyColumns(table));
		assertEquals(4, table.getCompress());
	}

	/**
	 * The journal tables without branches keep the type in their primary key.
	 */
	public void testJournalSingleBranch() {
		DBTable table = journalTable(false);

		assertEquals(
			Arrays.asList(AbstractFlexDataManager.TYPE_DBNAME, AbstractFlexDataManager.IDENTIFIER_DBNAME,
				BasicTypes.REV_MAX_DB_NAME, AbstractFlexDataManager.ATTRIBUTE_DBNAME),
			primaryKeyColumns(table));
		assertEquals(3, table.getCompress());
	}

	private static DBTable flexDataTable(boolean multipleBranches) {
		MORepository repository = new DefaultMORepository(multipleBranches);
		BasicTypeProvider.INSTANCE.createTypes(new AssertProtocol(), null, repository);
		return table(repository, AbstractFlexDataManager.FLEX_DATA);
	}

	private static DBTable journalTable(boolean multipleBranches) {
		MORepository repository = new DefaultMORepository(multipleBranches);
		new FlexJournalManager.JournalTypes(null, null).createTypes(new AssertProtocol(), null, repository);
		return table(repository, FlexDataChangeEntryJournal.JOURNAL_PRE_VALUE_TYPE_NAME);
	}

	private static DBTable table(MORepository repository, String typeName) {
		MOClass type = (MOClass) repository.getTypeOrNull(typeName);
		assertNotNull("Type '" + typeName + "' not created.", type);
		return SchemaSetup.createTable(type);
	}

	private static List<String> primaryKeyColumns(DBTable table) {
		List<String> result = new ArrayList<>();
		for (DBColumnRef ref : table.getPrimaryKey().getColumnRefs()) {
			result.add(ref.getName());
		}
		return result;
	}

	/**
	 * Suite of {@link TestFlexDataPrimaryKey}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFlexDataPrimaryKey.class, TypeIndex.Module.INSTANCE));
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.db.schema.SchemaTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeTypeFactory;

/**
 * Test of the column order of the primary keys and indexes created for the tables of a type
 * system, depending on {@link SchemaConfiguration#getBranchLast()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchLastIndexes extends TestCase {

	private static final String BRANCH = BasicTypes.BRANCH_DB_NAME;

	private static final String IDENTIFIER = BasicTypes.IDENTIFIER_DB_NAME;

	private static final String REV_MAX = BasicTypes.REV_MAX_DB_NAME;

	/** Type with a reference and a declared non-custom unique index. */
	private static final String OBJECT_LABEL = "ObjectLabel";

	/** Database name of the index declared for {@link #OBJECT_LABEL} without explicit columns. */
	private static final String LABEL_INDEX = "LABEL_IDX";

	/** Database name of the index of the reference attribute of {@link #OBJECT_LABEL}. */
	private static final String TARGET_INDEX = "TARGET";

	/** Database name of the index of the source reference of {@link #ASSOCIATION}. */
	private static final String SOURCE_INDEX = "SOURCE";

	/** Database name of the index of the destination reference of {@link #ASSOCIATION}. */
	private static final String DEST_INDEX = "DEST";

	/** Association type. */
	private static final String ASSOCIATION = "hasGroupMembers";

	/**
	 * Default layout: the branch leads the primary key of an object table.
	 */
	public void testObjectPrimaryKeyDefault() throws ConfigurationException {
		assertEquals(asList(BRANCH, IDENTIFIER, REV_MAX), primaryKey(table(Decision.FALSE, OBJECT_LABEL)));
	}

	/**
	 * Branch last: the branch follows the identifier in the primary key of an object table.
	 */
	public void testObjectPrimaryKeyBranchLast() throws ConfigurationException {
		assertEquals(asList(IDENTIFIER, BRANCH, REV_MAX), primaryKey(table(Decision.TRUE, OBJECT_LABEL)));
	}

	/**
	 * Default layout: the branch leads the indexes of the references.
	 */
	public void testReferenceIndexDefault() throws ConfigurationException {
		DBTable association = table(Decision.FALSE, ASSOCIATION);
		assertEquals(asList(BRANCH, "SOURCE_ID", REV_MAX), index(association, SOURCE_INDEX));
		assertEquals(asList(BRANCH, "DEST_ID", REV_MAX), index(association, DEST_INDEX));

		DBTable label = table(Decision.FALSE, OBJECT_LABEL);
		assertEquals(asList(BRANCH, "TARGET_ID", REV_MAX), index(label, TARGET_INDEX));
	}

	/**
	 * Branch last: the referenced identifier leads the indexes of the references.
	 */
	public void testReferenceIndexBranchLast() throws ConfigurationException {
		DBTable association = table(Decision.TRUE, ASSOCIATION);
		assertEquals(asList("SOURCE_ID", BRANCH, REV_MAX), index(association, SOURCE_INDEX));
		assertEquals(asList("DEST_ID", BRANCH, REV_MAX), index(association, DEST_INDEX));

		DBTable label = table(Decision.TRUE, OBJECT_LABEL);
		assertEquals(asList("TARGET_ID", BRANCH, REV_MAX), index(label, TARGET_INDEX));
	}

	/**
	 * Default layout: the branch leads a declared index without explicit columns.
	 */
	public void testDeclaredIndexDefault() throws ConfigurationException {
		assertEquals(asList(BRANCH, "LABEL", REV_MAX), index(table(Decision.FALSE, OBJECT_LABEL), LABEL_INDEX));
	}

	/**
	 * Branch last: the branch follows the declared columns of an index without explicit columns.
	 */
	public void testDeclaredIndexBranchLast() throws ConfigurationException {
		assertEquals(asList("LABEL", BRANCH, REV_MAX), index(table(Decision.TRUE, OBJECT_LABEL), LABEL_INDEX));
	}

	/**
	 * Custom indexes keep their declared column order.
	 */
	public void testCustomIndexBranchLast() throws ConfigurationException {
		assertEquals(asList(REV_MAX), index(table(Decision.TRUE, OBJECT_LABEL), REV_MAX));
	}

	/**
	 * Default layout: the branch leads the primary key of the flex data table.
	 */
	public void testFlexDataDefault() throws ConfigurationException {
		DBTable table = table(Decision.FALSE, AbstractFlexDataManager.FLEX_DATA);
		assertEquals(asList(BRANCH, AbstractFlexDataManager.IDENTIFIER_DBNAME,
			AbstractFlexDataManager.ATTRIBUTE_DBNAME, REV_MAX), primaryKey(table));
		assertEquals("Branch and identifier are compressed.", 2, table.getCompress());
	}

	/**
	 * Branch last: the branch follows identifier and attribute in the primary key of the flex data
	 * table.
	 */
	public void testFlexDataBranchLast() throws ConfigurationException {
		DBTable table = table(Decision.TRUE, AbstractFlexDataManager.FLEX_DATA);
		assertEquals(asList(AbstractFlexDataManager.IDENTIFIER_DBNAME, AbstractFlexDataManager.ATTRIBUTE_DBNAME,
			BRANCH, REV_MAX), primaryKey(table));
		assertEquals("Identifier is compressed.", 1, table.getCompress());
	}

	private static DBTable table(Decision branchLast, String typeName) throws ConfigurationException {
		MORepository repository = repository(branchLast);
		MOClass type = (MOClass) repository.getTypeOrNull(typeName);
		assertNotNull("Type '" + typeName + "' not created.", type);
		assertEquals(branchLast == Decision.TRUE, type.getDBMapping().branchLast());
		return SchemaSetup.createTable(type);
	}

	private static MORepository repository(Decision branchLast) throws ConfigurationException {
		ApplicationTypes types = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
		SchemaConfiguration schemaConfig = types.getTypeSystem(SchemaConfiguration.DEFAULT_NAME).getConfig();
		schemaConfig = SchemaTestSetup.updateMultipleBranches(schemaConfig, Decision.TRUE);
		schemaConfig = SchemaTestSetup.updateBranchLast(schemaConfig, branchLast);
		assertEquals(Boolean.valueOf(branchLast == Decision.TRUE), schemaConfig.getBranchLast());

		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaSetup setup = context.getInstance(schemaConfig).resolve(context);
		// The explicit value overrides the default of the dialect.
		return setup.createMORepository(DBHelper.createDefaultInstance(H2Helper.class),
			new DBKnowledgeTypeFactory(true));
	}

	private static List<String> primaryKey(DBTable table) {
		return names(table.getPrimaryKey().getColumnRefs());
	}

	private static List<String> index(DBTable table, String indexName) {
		for (DBIndex index : table.getIndices()) {
			if (index.getName().equals(indexName)) {
				return names(index.getColumnRefs());
			}
		}
		fail("No index '" + indexName + "' in table '" + table.getName() + "', indexes: " + table.getIndices());
		return null;
	}

	private static List<String> names(List<DBColumnRef> refs) {
		List<String> result = new ArrayList<>();
		for (DBColumnRef ref : refs) {
			result.add(ref.getName());
		}
		return result;
	}

	/**
	 * Suite of {@link TestBranchLastIndexes}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestBranchLastIndexes.class, TypeIndex.Module.INSTANCE,
				DataAccessService.Module.INSTANCE));
	}

}
